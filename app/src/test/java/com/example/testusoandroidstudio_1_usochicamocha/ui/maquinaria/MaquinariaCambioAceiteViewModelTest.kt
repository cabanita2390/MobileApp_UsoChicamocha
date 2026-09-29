package com.example.testusoandroidstudio_1_usochicamocha.ui.maquinaria

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.GetLocalMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange.GetMachineOilChangeFormByIdUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange.GetPendingMachineOilChangeFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange.SaveMachineOilChangeFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.Resource
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MaquinariaCambioAceiteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val saveUseCase = mockk<SaveMachineOilChangeFormUseCase>()
    private val getLocalMachinesUseCase = mockk<GetLocalMachinesUseCase>()
    private val getLocalOilsUseCase = mockk<GetLocalOilsUseCase>()
    private val syncOilsUseCase = mockk<SyncOilsUseCase>(relaxed = true)
    private val getPendingUseCase = mockk<GetPendingMachineOilChangeFormsUseCase>()
    private val getByIdUseCase = mockk<GetMachineOilChangeFormByIdUseCase>()
    private val localSyncCoordinator = mockk<LocalSyncCoordinator>()

    private val machine = Machine(7, "Retro 1", "CAT", "416", "E1", "M-07", null, null)
    private val oil = Oil(3, "motor", "Mobil 15W40")

    private lateinit var viewModel: MaquinariaCambioAceiteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

        every { getLocalMachinesUseCase() } returns flowOf(Resource.Success(listOf(machine)))
        every { getLocalOilsUseCase() } returns flowOf(listOf(oil))
        every { getPendingUseCase() } returns flowOf(emptyList())
        coEvery { saveUseCase(any()) } returns Result.success(Unit)
        coEvery { localSyncCoordinator.coordinateSync(any()) } returns Result.success(Unit)

        viewModel = MaquinariaCambioAceiteViewModel(
            saveUseCase,
            getLocalMachinesUseCase,
            getLocalOilsUseCase,
            syncOilsUseCase,
            getPendingUseCase,
            getByIdUseCase,
            localSyncCoordinator,
            SavedStateHandle()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun llenarFormulario(horometro: String, promedioHoras: String = "250") {
        viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.MachineSelected(machine))
        viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.MachineOilChangeFormTypeChanged("motor"))
        viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.OilSelected(oil))
        viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.QuantityChanged("12"))
        viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.AverageHoursChangeChanged(promedioHoras))
        viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.CurrentHourMeterChanged(horometro))
    }

    @Test
    fun `submit con horometro vacio no guarda y muestra error`() = runTest {
        advanceUntilIdle()
        llenarFormulario("")

        viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.Submit)
        advanceUntilIdle()

        coVerify(exactly = 0) { saveUseCase(any()) }
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `submit con horometro no numerico o cero no guarda`() = runTest {
        advanceUntilIdle()
        for (valor in listOf("abc", "1234.5", "0", "-5")) {
            llenarFormulario(valor)
            viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.Submit)
            advanceUntilIdle()
            assertNotNull("Debió rechazar '$valor'", viewModel.uiState.value.error)
            viewModel.clearError()
        }
        coVerify(exactly = 0) { saveUseCase(any()) }
    }

    @Test
    fun `submit con promedio de horas vacio o invalido no guarda`() = runTest {
        advanceUntilIdle()
        for (valor in listOf("", "abc", "0", "-10")) {
            llenarFormulario("1520", promedioHoras = valor)
            viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.Submit)
            advanceUntilIdle()
            assertNotNull("Debió rechazar promedio '$valor'", viewModel.uiState.value.error)
            viewModel.clearError()
        }
        coVerify(exactly = 0) { saveUseCase(any()) }
    }

    @Test
    fun `submit con datos validos guarda horometro y promedio ingresados`() = runTest {
        advanceUntilIdle()
        llenarFormulario(" 1520 ", promedioHoras = " 250 ")

        val guardado = slot<MachineOilChangeForm>()
        coEvery { saveUseCase(capture(guardado)) } returns Result.success(Unit)

        viewModel.onFormEvent(MaquinariaCambioAceiteFormEvent.Submit)
        advanceUntilIdle()

        assertEquals(1520, guardado.captured.currentHourMeter)
        assertEquals(250, guardado.captured.averageHoursChange)
    }
}
