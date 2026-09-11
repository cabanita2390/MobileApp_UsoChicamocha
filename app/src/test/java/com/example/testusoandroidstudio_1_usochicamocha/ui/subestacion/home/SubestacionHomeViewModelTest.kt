package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.home

import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EstacionCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerCumplimientoAnioLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEstacionesCacheUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.SincronizarCumplimientoAnioUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * `SubestacionHomeViewModel` — los conteos del hero y de los subtítulos de las 4
 * tarjetas de navegación (agregados esta sesión: antes eran texto fijo, el mock
 * siempre muestra números reales). `pendientes` mezcla vencidas + pendientes del mes
 * (backlog completo), `realizadasCount` cuenta TODO el año hasta el mes actual (no
 * solo el mes en curso), `estacionesTotal` viene del catálogo cacheado. Offline-first
 * (agregado esta sesión): los KPIs se alimentan del caché local en Room
 * (`ObtenerCumplimientoAnioLocalUseCase`), el backend solo refresca ese caché en segundo
 * plano (`SincronizarCumplimientoAnioUseCase`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubestacionHomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var obtenerCumplimientoAnioLocalUseCase: ObtenerCumplimientoAnioLocalUseCase
    @MockK
    lateinit var sincronizarCumplimientoAnioUseCase: SincronizarCumplimientoAnioUseCase
    @MockK
    lateinit var obtenerEstacionesCacheUseCase: ObtenerEstacionesCacheUseCase
    @MockK
    lateinit var tokenManager: TokenManager

    private val hoy = LocalDate.now()

    private fun cita(anio: Int, mes: Int, actividadId: Long, cumple: Boolean) =
        CitaProgramada(
            programacionId = (anio * 1000 + mes * 10 + actividadId).toLong(),
            anio = anio, mes = mes,
            estacionId = 1L, estacionNombre = "Duitama", estacionTipo = "BOMBEO",
            actividadId = actividadId, actividadNombre = "Inspección",
            ejecutado = 0, cumple = cumple
        )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { obtenerEstacionesCacheUseCase() } returns flowOf(emptyList())
        every { tokenManager.getUsername() } returns flowOf("tecnico.test")
        every { obtenerCumplimientoAnioLocalUseCase(any(), any()) } returns flowOf(emptyList())
        coEvery { sincronizarCumplimientoAnioUseCase(any(), any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel() = SubestacionHomeViewModel(
        obtenerCumplimientoAnioLocalUseCase,
        sincronizarCumplimientoAnioUseCase,
        obtenerEstacionesCacheUseCase,
        tokenManager
    )

    @Test
    fun `estacionesTotal refleja el tamano del catalogo cacheado`() = runTest {
        every { obtenerEstacionesCacheUseCase() } returns flowOf(
            listOf(
                EstacionCatalogo(1L, "Duitama", "BOMBEO", "TRIMESTRAL"),
                EstacionCatalogo(2L, "Paipa", "COMPLEMENTARIA", "ANUAL")
            )
        )
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.estacionesTotal)
    }

    @Test
    fun `pendientes del hero suma vencidas y pendientes del mes, no solo el mes actual`() = runTest {
        val mesPasado = hoy.minusMonths(1)
        every { obtenerCumplimientoAnioLocalUseCase(any(), any()) } returns flowOf(
            listOf(
                cita(hoy.year, hoy.monthValue, actividadId = 1L, cumple = false),   // PENDIENTE (mes actual)
                cita(mesPasado.year, mesPasado.monthValue, actividadId = 2L, cumple = false) // VENCIDA (mes cerrado)
            )
        )
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.pendientesDelMes)
        assertEquals(1, vm.uiState.value.vencidas)
        assertEquals(2, vm.uiState.value.pendientes)
    }

    @Test
    fun `realizadasCount cuenta ejecutadas de todo el rango, no solo el mes actual`() = runTest {
        val mesPasado = hoy.minusMonths(1)
        every { obtenerCumplimientoAnioLocalUseCase(any(), any()) } returns flowOf(
            listOf(
                cita(hoy.year, hoy.monthValue, actividadId = 1L, cumple = true),        // EJECUTADA este mes
                cita(mesPasado.year, mesPasado.monthValue, actividadId = 2L, cumple = true) // EJECUTADA mes pasado
            )
        )
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.realizadasCount)
    }

    @Test
    fun `porcentaje del hero es ejecutadas sobre programadas del mes actual`() = runTest {
        every { obtenerCumplimientoAnioLocalUseCase(any(), any()) } returns flowOf(
            listOf(
                cita(hoy.year, hoy.monthValue, actividadId = 1L, cumple = true),
                cita(hoy.year, hoy.monthValue, actividadId = 2L, cumple = false)
            )
        )
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.programadasDelMes)
        assertEquals(1, vm.uiState.value.ejecutadasDelMes)
        assertEquals(50, vm.uiState.value.porcentaje)
    }

    @Test
    fun `sin citas el porcentaje no divide por cero`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(0, vm.uiState.value.porcentaje)
    }
}
