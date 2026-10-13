package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.ImageForSync
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FormRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncPendingImagesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.ImageUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Las fotos pendientes vivían en cacheDir y Android las borraba al faltar espacio
 * (reproducido con `pm trim-caches`): la evidencia obligatoria se perdía sin aviso.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AlmacenFotosPendientesTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var imageDao: ImageDao
    private lateinit var almacen: AlmacenFotosPendientes

    private val dirPendientes get() = ImageUtils.directorioFotosPendientes(context)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        imageDao = db.imageDao()
        almacen = AlmacenFotosPendientes(context, imageDao)
        dirPendientes.listFiles()?.forEach { it.delete() }
    }

    @After
    fun tearDown() = db.close()

    private fun foto(dir: File, nombre: String) = File(dir, nombre).apply { writeBytes(byteArrayOf(1, 2, 3)) }

    @Test
    fun `la foto comprimida se guarda fuera del cache`() = runTest {
        val original = File(context.cacheDir, "camara.png")
        original.outputStream().use { Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, it) }

        val comprimida = ImageUtils.compressAndSaveImage(context, Uri.fromFile(original))

        assertNotNull(comprimida)
        val archivo = File(comprimida!!.path!!)
        assertEquals(dirPendientes.canonicalPath, archivo.parentFile!!.canonicalPath)
        assertFalse(archivo.canonicalPath.startsWith(context.cacheDir.canonicalPath))
    }

    @Test
    fun `las fotos pendientes que quedaron en cache se mueven y siguen apuntadas`() = runTest {
        val enCache = foto(context.cacheDir, "compressed_1791926169061.jpg")
        imageDao.insertImages(listOf(ImageEntity(ejecucionUUID = "u1", localUri = Uri.fromFile(enCache).toString(), tipo = ImageEntity.TIPO_SUBESTACION)))

        almacen.prepararUnaVezPorProceso()
        context.cacheDir.listFiles()?.forEach { it.delete() } // Android vacía el caché

        val fila = imageDao.getNoSincronizadas().single()
        val movida = File(Uri.parse(fila.localUri).path!!)
        assertTrue(movida.exists())
        assertEquals(dirPendientes.canonicalPath, movida.parentFile!!.canonicalPath)
        assertFalse(enCache.exists())
    }

    @Test
    fun `borra huerfanas viejas pero conserva las en uso y las recientes`() = runTest {
        val enUso = foto(dirPendientes, "en_uso.jpg").apply { setLastModified(0) }
        val huerfanaVieja = foto(dirPendientes, "vieja.jpg").apply { setLastModified(0) }
        val huerfanaReciente = foto(dirPendientes, "reciente.jpg") // formulario que se está llenando
        imageDao.insertImages(listOf(ImageEntity(ejecucionUUID = "u1", localUri = Uri.fromFile(enUso).toString(), tipo = ImageEntity.TIPO_SUBESTACION)))

        almacen.prepararUnaVezPorProceso()

        assertTrue(enUso.exists())
        assertTrue(huerfanaReciente.exists())
        assertFalse(huerfanaVieja.exists())
    }

    @Test
    fun `borrarSubida solo borra dentro del directorio de pendientes`() {
        val pendiente = foto(dirPendientes, "subida.jpg")
        val ajena = foto(context.filesDir, "ajena.jpg")

        almacen.borrarSubida(Uri.fromFile(pendiente).toString())
        almacen.borrarSubida(Uri.fromFile(ajena).toString())
        almacen.borrarSubida("content://media/external/images/1")

        assertFalse(pendiente.exists())
        assertTrue(ajena.exists())
    }

    @Test
    fun `la foto se borra del equipo solo cuando el servidor la confirma`() = runTest {
        val ok = foto(dirPendientes, "ok.jpg")
        val falla = foto(dirPendientes, "falla.jpg")
        val formRepository = mockk<FormRepository>(relaxed = true)
        every { formRepository.getPendingImagesForSync() } returns flowOf(listOf(
            ImageForSync(localId = 1, localUri = Uri.fromFile(ok).toString(), serverId = 63, tipo = ImageEntity.TIPO_SUBESTACION),
            ImageForSync(localId = 2, localUri = Uri.fromFile(falla).toString(), serverId = 64, tipo = ImageEntity.TIPO_SUBESTACION)
        ))
        val subestacionRepository = mockk<SubestacionRepository>()
        coEvery { subestacionRepository.syncEvidencia(63, any()) } returns Result.success(Unit)
        coEvery { subestacionRepository.syncEvidencia(64, any()) } returns Result.failure(Exception("sin señal"))

        SyncPendingImagesUseCase(formRepository, subestacionRepository, mockk(relaxed = true), almacen)()

        assertFalse(ok.exists())
        assertTrue(falla.exists())
    }

    private suspend fun ejecucionSincronizada(uuid: String, serverId: Long) {
        db.ejecucionDao().insertEjecucion(
            com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity(
                uuidCliente = uuid, fecha = "2026-10-13", mesEjecucion = 10, semanaEjecucion = 2,
                estacionId = 13, estacionNombre = "Holanda", tipoMantenimiento = "PREVENTIVO",
                tipoActividad = "INSPECCION", esProgramada = false, resultado = "CONFORME", observaciones = "ok"
            )
        )
        db.ejecucionDao().markAsSynced(uuid, serverId)
    }

    @Test
    fun `una foto cuyo archivo ya no existe queda perdida y deja de reintentarse`() = runTest {
        ejecucionSincronizada("holanda", 63)
        val borrada = File(dirPendientes, "borrada.jpg") // nunca existió en disco
        imageDao.insertImages(listOf(ImageEntity(ejecucionUUID = "holanda", localUri = Uri.fromFile(borrada).toString(), tipo = ImageEntity.TIPO_SUBESTACION)))
        val formRepository = mockk<FormRepository>(relaxed = true)
        every { formRepository.getPendingImagesForSync() } returns imageDao.getPendingImagesForSync()
        val subestacionRepository = mockk<SubestacionRepository>(relaxed = true)

        SyncPendingImagesUseCase(formRepository, subestacionRepository, mockk(relaxed = true), almacen)()

        io.mockk.coVerify(exactly = 0) { subestacionRepository.syncEvidencia(any(), any()) }
        assertTrue(imageDao.getPendingImagesForSync().first().isEmpty())
        val cola = db.ejecucionDao().getColaFlow().first().single()
        assertEquals(1, cola.fotosPerdidas)
    }

    @Test
    fun `reponer la foto la devuelve a la cola de subida y descartar la saca de la cola`() = runTest {
        ejecucionSincronizada("holanda", 63)
        imageDao.insertImages(listOf(ImageEntity(ejecucionUUID = "holanda", localUri = "file:///no/existe.jpg", tipo = ImageEntity.TIPO_SUBESTACION)))
        imageDao.marcarPerdida(imageDao.getNoSincronizadas().single().localId)
        val nueva = foto(dirPendientes, "nueva.jpg")

        imageDao.reemplazarFotoPerdida(imageDao.getPerdidasDeEjecucion("holanda").single().localId, Uri.fromFile(nueva).toString())

        val pendiente = imageDao.getPendingImagesForSync().first().single()
        assertEquals(63L, pendiente.serverId)
        assertEquals(Uri.fromFile(nueva).toString(), pendiente.localUri)

        imageDao.marcarPerdida(pendiente.localId)
        imageDao.descartarPerdidas("holanda")
        assertTrue(db.ejecucionDao().getColaFlow().first().isEmpty())
    }
}
