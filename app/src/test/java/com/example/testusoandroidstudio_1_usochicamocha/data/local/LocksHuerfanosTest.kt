package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.EjecucionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EjecucionResponseDto
import com.example.testusoandroidstudio_1_usochicamocha.data.repository.SubestacionRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

/**
 * Un registro cuyo envío quedó a medias porque el proceso murió (isSyncing = 1 persistido)
 * quedaba en "ENVIANDO" para siempre y su foto nunca subía. Reproducido en emulador
 * (force-stop a 2 s de iniciado el POST): el servidor sí lo había guardado.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LocksHuerfanosTest {

    private lateinit var db: AppDatabase
    private lateinit var ejecucionDao: EjecucionDao
    private lateinit var imageDao: ImageDao

    private val uuid = "79b80f3c-8cd3-4c81-b20f-51c4bca64fa8"

    private fun ejecucion(isSyncing: Boolean) = EjecucionEntity(
        uuidCliente = uuid,
        fecha = "2026-10-13",
        mesEjecucion = 10,
        semanaEjecucion = 2,
        estacionId = 13,
        estacionNombre = "Holanda",
        tipoMantenimiento = "PREVENTIVO",
        tipoActividad = "INSPECCION",
        actividadId = 5,
        programacionId = 20,
        esProgramada = true,
        resultado = "CON_HALLAZGOS",
        observaciones = "Se inspecciona y no se evidencian novedades",
        isSyncing = isSyncing
    )

    private fun respuestaServidor(id: Long) = EjecucionResponseDto(
        id = id, fecha = "2026-10-13", mesEjecucion = 10, semanaEjecucion = 2, estacionId = 13,
        estacionNombre = "Holanda", disciplina = "CIVIL", tipoMantenimiento = "PREVENTIVO",
        tipoActividad = "INSPECCION", actividadId = 5, actividadNombre = "Compuertas", esProgramada = true,
        motivoNoCatalogado = null, resultado = "CON_HALLAZGOS", observaciones = "ok", descripcionLibre = null,
        responsable = "qa_operario", uuidCliente = uuid, evidencias = emptyList(), evidenciaPendiente = true,
        ediciones = emptyList()
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        ejecucionDao = db.ejecucionDao()
        imageDao = db.imageDao()
    }

    @After
    fun tearDown() = db.close()

    private fun repositorio(api: ApiService) = SubestacionRepositoryImpl(
        ApplicationProvider.getApplicationContext(), ejecucionDao, db.estacionCacheDao(), db.actividadCacheDao(),
        imageDao, api, db.cumplimientoCacheDao(), db.ejecucionDetalleCacheDao(), db.ejecucionNoProgramadaCacheDao(),
        mockk(relaxed = true)
    )

    @Test
    fun `un registro con lock huerfano vuelve a la cola al arrancar el proceso`() = runTest {
        ejecucionDao.insertEjecucion(ejecucion(isSyncing = true))
        assertTrue(ejecucionDao.getPendingFlow().first().isEmpty())

        LiberadorLocksHuerfanos(ejecucionDao, imageDao).liberarUnaVezPorProceso()

        assertEquals(uuid, ejecucionDao.getPendingFlow().first().single().uuidCliente)
    }

    @Test
    fun `tambien libera las fotos que quedaron subiendo`() = runTest {
        ejecucionDao.insertEjecucion(ejecucion(isSyncing = false))
        ejecucionDao.markAsSynced(uuid, 63)
        imageDao.insertImages(listOf(ImageEntity(ejecucionUUID = uuid, localUri = "file:///x.jpg", isSyncing = true, tipo = ImageEntity.TIPO_SUBESTACION)))
        assertTrue(imageDao.getPendingImagesForSync().first().isEmpty())

        LiberadorLocksHuerfanos(ejecucionDao, imageDao).liberarUnaVezPorProceso()

        assertEquals(63L, imageDao.getPendingImagesForSync().first().single().serverId)
    }

    @Test
    fun `solo libera una vez por proceso y no pisa un envio en curso`() = runTest {
        val liberador = LiberadorLocksHuerfanos(ejecucionDao, imageDao)
        liberador.liberarUnaVezPorProceso()
        ejecucionDao.insertEjecucion(ejecucion(isSyncing = false))
        assertEquals(1, ejecucionDao.acquireLock(uuid)) // un envío en curso en este proceso

        liberador.liberarUnaVezPorProceso()

        assertEquals(0, ejecucionDao.acquireLock(uuid))
    }

    @Test
    fun `el reenvio tras liberar recibe el registro ya guardado y habilita la foto`() = runTest {
        ejecucionDao.insertEjecucion(ejecucion(isSyncing = true))
        imageDao.insertImages(listOf(ImageEntity(ejecucionUUID = uuid, localUri = "file:///x.jpg", tipo = ImageEntity.TIPO_SUBESTACION)))
        val api = mockk<ApiService>()
        // El backend es idempotente por uuidCliente: devuelve el registro que ya había guardado.
        coEvery { api.registrarEjecucionSubestacion(any()) } returns Response.success(respuestaServidor(63))
        LiberadorLocksHuerfanos(ejecucionDao, imageDao).liberarUnaVezPorProceso()

        val pendiente = ejecucionDao.getPendingFlow().first().single().toDomain()
        assertTrue(repositorio(api).syncEjecucion(pendiente).isSuccess)

        assertEquals(63L, ejecucionDao.getByUuid(uuid)!!.serverId)
        assertEquals(63L, imageDao.getPendingImagesForSync().first().single().serverId)
    }

    @Test
    fun `si el envio se cancela a mitad el lock se suelta`() = runTest {
        ejecucionDao.insertEjecucion(ejecucion(isSyncing = false))
        val api = mockk<ApiService>()
        coEvery { api.registrarEjecucionSubestacion(any()) } coAnswers { awaitCancellation() }
        val pendiente = ejecucionDao.getByUuid(uuid)!!.toDomain()

        // Tiempo real (no el virtual de runTest): Room corre en sus propios hilos.
        withContext(Dispatchers.Default) { withTimeoutOrNull(1_000) { repositorio(api).syncEjecucion(pendiente) } }

        val fila = ejecucionDao.getByUuid(uuid)!!
        assertFalse(fila.isSyncing)
        assertEquals(uuid, ejecucionDao.getPendingFlow().first().single().uuidCliente)
        coVerify(exactly = 1) { api.registrarEjecucionSubestacion(any()) }
    }
}
