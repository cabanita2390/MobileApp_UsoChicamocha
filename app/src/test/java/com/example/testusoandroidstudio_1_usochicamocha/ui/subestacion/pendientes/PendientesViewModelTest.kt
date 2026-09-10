package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.pendientes

import androidx.lifecycle.SavedStateHandle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEjecucionPorProgramacionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerPendientesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.EstadoCita
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * `PendientesViewModel` — filtro inicial leído del `SavedStateHandle` (arreglo de esta
 * sesión: antes dependía de un efecto de pantalla que podía pisar un cambio manual de
 * chip; ahora se lee una sola vez al construir), agrupación/orden por mes más reciente
 * primero, y el sufijo "MES EN CURSO"/"MES CERRADO" agregado esta sesión.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PendientesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var obtenerPendientesUseCase: ObtenerPendientesUseCase
    @MockK
    lateinit var obtenerEjecucionPorProgramacionUseCase: ObtenerEjecucionPorProgramacionUseCase
    @MockK
    lateinit var savedStateHandle: SavedStateHandle

    private val hoy = LocalDate.now()

    private fun cita(anio: Int, mes: Int, estacionId: Long = 1L, actividadId: Long = 1L, cumple: Boolean = false, ejecutado: Int = 0) =
        CitaProgramada(
            programacionId = (anio * 100 + mes).toLong(),
            anio = anio,
            mes = mes,
            estacionId = estacionId,
            estacionNombre = "Duitama",
            estacionTipo = "BOMBEO",
            actividadId = actividadId,
            actividadNombre = "Inspección",
            ejecutado = ejecutado,
            cumple = cumple
        )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<String>("filtroInicial") } returns null
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel() = PendientesViewModel(obtenerPendientesUseCase, obtenerEjecucionPorProgramacionUseCase, savedStateHandle)

    @Test
    fun `sin argumento de navegacion el filtro inicial es Por hacer`() = runTest {
        val vm = createViewModel()
        assertEquals(FiltroPendientes.POR_HACER, vm.uiState.value.filtro)
    }

    @Test
    fun `con filtroInicial Vencidas en la navegacion arranca en ese filtro`() = runTest {
        every { savedStateHandle.get<String>("filtroInicial") } returns "Vencidas"
        val vm = createViewModel()
        assertEquals(FiltroPendientes.VENCIDAS, vm.uiState.value.filtro)
    }

    @Test
    fun `con filtroInicial Realizadas en la navegacion arranca en ese filtro`() = runTest {
        every { savedStateHandle.get<String>("filtroInicial") } returns "Realizadas"
        val vm = createViewModel()
        assertEquals(FiltroPendientes.REALIZADAS, vm.uiState.value.filtro)
    }

    @Test
    fun `Por hacer agrupa pendientes y vencidas pero no ejecutadas`() = runTest {
        val citaVencida = cita(anio = hoy.minusMonths(2).year, mes = hoy.minusMonths(2).monthValue)
        val citaPendiente = cita(anio = hoy.year, mes = hoy.monthValue)
        val citaEjecutada = cita(anio = hoy.year, mes = hoy.monthValue, actividadId = 2L, cumple = true)
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaVencida, citaPendiente, citaEjecutada))

        val vm = createViewModel()
        advanceUntilIdle()

        val todasLasCitas = vm.uiState.value.grupos.flatMap { it.citas }
        assertEquals(2, todasLasCitas.size)
        assertTrue(todasLasCitas.none { it.estado == EstadoCita.EJECUTADA })
    }

    @Test
    fun `onFiltroChange re-agrupa sin volver a pedir datos al backend`() = runTest {
        val citaVencida = cita(anio = hoy.minusMonths(3).year, mes = hoy.minusMonths(3).monthValue)
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaVencida))

        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.grupos.flatMap { it.citas }.size)

        vm.onFiltroChange(FiltroPendientes.REALIZADAS)

        assertEquals(FiltroPendientes.REALIZADAS, vm.uiState.value.filtro)
        assertTrue(vm.uiState.value.grupos.isEmpty())
    }

    @Test
    fun `los grupos quedan ordenados del mes mas reciente al mas antiguo`() = runTest {
        val haceDosMeses = hoy.minusMonths(2)
        val haceUnMes = hoy.minusMonths(1)
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(
            listOf(
                cita(anio = haceDosMeses.year, mes = haceDosMeses.monthValue),
                cita(anio = haceUnMes.year, mes = haceUnMes.monthValue, actividadId = 2L)
            )
        )

        val vm = createViewModel()
        advanceUntilIdle()

        val grupos = vm.uiState.value.grupos
        assertEquals(2, grupos.size)
        assertEquals(haceUnMes.year, grupos[0].anio)
        assertEquals(haceUnMes.monthValue, grupos[0].mes)
        assertEquals(haceDosMeses.year, grupos[1].anio)
        assertEquals(haceDosMeses.monthValue, grupos[1].mes)
    }

    @Test
    fun `MesGrupo del mes actual dice MES EN CURSO`() {
        val grupo = MesGrupo(anio = hoy.year, mes = hoy.monthValue, citas = emptyList())
        assertTrue(grupo.label.endsWith("MES EN CURSO"))
    }

    @Test
    fun `MesGrupo de un mes pasado dice MES CERRADO`() {
        val pasado = hoy.minusMonths(1)
        val grupo = MesGrupo(anio = pasado.year, mes = pasado.monthValue, citas = emptyList())
        assertTrue(grupo.label.endsWith("MES CERRADO"))
    }
}
