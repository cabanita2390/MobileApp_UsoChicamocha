package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.testusoandroidstudio_1_usochicamocha.data.local.AppDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EjecucionResponseDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.PageDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

/**
 * Las actividades no programadas del año se pedían en una sola página de 500: si había más,
 * las sobrantes no llegaban nunca a "Realizadas".
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NoProgramadasPaginadasTest {

    private lateinit var db: AppDatabase
    private lateinit var api: ApiService
    private lateinit var repo: SubestacionRepositoryImpl

    private fun ejecucion(id: Long) = EjecucionResponseDto(
        id = id, fecha = "2026-03-01", mesEjecucion = 3, semanaEjecucion = 1, estacionId = 1,
        estacionNombre = "Cuche", disciplina = "CIVIL", tipoMantenimiento = "PREVENTIVO",
        tipoActividad = "MANTENIMIENTO", actividadId = null, actividadNombre = null, esProgramada = false,
        motivoNoCatalogado = "NO_PROGRAMADO", resultado = "CONFORME", observaciones = "ok",
        descripcionLibre = "libre", responsable = "op", uuidCliente = "u$id", evidencias = emptyList(),
        evidenciaPendiente = false, ediciones = emptyList()
    )

    private fun pagina(desde: Long, cuantas: Int, ultima: Boolean) =
        Response.success(PageDto((desde until desde + cuantas).map(::ejecucion), last = ultima))

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        api = mockk()
        repo = SubestacionRepositoryImpl(
            context, db.ejecucionDao(), db.estacionCacheDao(), db.actividadCacheDao(), db.imageDao(), api,
            db.cumplimientoCacheDao(), db.ejecucionDetalleCacheDao(), db.ejecucionNoProgramadaCacheDao(),
            mockk(relaxed = true)
        )
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `trae todas las paginas y no corta en 500`() = runTest {
        coEvery { api.getEjecucionesSubestacion(any(), any(), any(), false, 0, 500) } returns pagina(1, 500, ultima = false)
        coEvery { api.getEjecucionesSubestacion(any(), any(), any(), false, 1, 500) } returns pagina(501, 37, ultima = true)

        assertTrue(repo.sincronizarEjecucionesNoProgramadasDelAnio(2026).isSuccess)

        assertEquals(537, db.ejecucionNoProgramadaCacheDao().getDelAnioFlow(2026).first().size)
        coVerify(exactly = 2) { api.getEjecucionesSubestacion(any(), any(), any(), false, any(), 500) }
    }

    @Test
    fun `si una pagina falla no deja el cache a medias`() = runTest {
        coEvery { api.getEjecucionesSubestacion(any(), any(), any(), false, 0, 500) } returns pagina(1, 3, ultima = true)
        assertTrue(repo.sincronizarEjecucionesNoProgramadasDelAnio(2026).isSuccess)
        coEvery { api.getEjecucionesSubestacion(any(), any(), any(), false, 0, 500) } returns pagina(1, 500, ultima = false)
        coEvery { api.getEjecucionesSubestacion(any(), any(), any(), false, 1, 500) } returns
            Response.error(503, "caído".toResponseBody(null))

        assertTrue(repo.sincronizarEjecucionesNoProgramadasDelAnio(2026).isFailure)

        assertEquals(3, db.ejecucionNoProgramadaCacheDao().getDelAnioFlow(2026).first().size)
    }
}
