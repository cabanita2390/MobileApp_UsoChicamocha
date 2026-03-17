package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.content.Context
import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.DocumentoMotoDao
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalUbicacionesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SaveInspeccionMotoLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SyncInspeccionMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.GetPendingInspeccionesMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MotocicletaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var context: Context
    @MockK
    lateinit var tokenManager: TokenManager
    @MockK
    lateinit var apiService: ApiService
    @MockK
    lateinit var getLocalMotosUseCase: GetLocalMotosUseCase
    @MockK
    lateinit var getLocalUbicacionesUseCase: GetLocalUbicacionesUseCase
    @MockK
    lateinit var saveInspeccionMotoLocalUseCase: SaveInspeccionMotoLocalUseCase
    @MockK
    lateinit var syncInspeccionMotoUseCase: SyncInspeccionMotoUseCase
    @MockK
    lateinit var getPendingInspeccionesMotoUseCase: GetPendingInspeccionesMotoUseCase
    @MockK
    lateinit var documentoMotoDao: DocumentoMotoDao
    @MockK
    lateinit var localSyncCoordinator: LocalSyncCoordinator

    private lateinit var viewModel: MotocicletaViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Default mock behaviors
        every { getLocalMotosUseCase() } returns flowOf(emptyList())
        every { getLocalUbicacionesUseCase() } returns flowOf(emptyList())
        every { getPendingInspeccionesMotoUseCase() } returns flowOf(emptyList())
        every { tokenManager.getInspectorInfo() } returns flowOf("Test Inspector")
        
        // Mock Coordinator observations
        every { localSyncCoordinator.observeSyncTrigger(any()) } returns flowOf(false)
        coEvery { localSyncCoordinator.coordinateSync(any()) } returns Result.success(Unit)
    }

    private fun createViewModel() {
        viewModel = MotocicletaViewModel(
            context,
            tokenManager,
            apiService,
            getLocalMotosUseCase,
            getLocalUbicacionesUseCase,
            saveInspeccionMotoLocalUseCase,
            syncInspeccionMotoUseCase,
            getPendingInspeccionesMotoUseCase,
            documentoMotoDao,
            localSyncCoordinator
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `when viewmodel starts, it should load motos and locations`() = runTest {
        val motos = listOf(Moto(1, "XYZ123"))
        val ubicaciones = listOf(Ubicacion(1, "Patio Central"))

        every { getLocalMotosUseCase() } returns flowOf(motos)
        every { getLocalUbicacionesUseCase() } returns flowOf(ubicaciones)

        createViewModel()
        advanceUntilIdle()

        assertEquals(motos, viewModel.uiState.value.motocicletas)
        assertEquals(ubicaciones, viewModel.uiState.value.ubicaciones)
    }

    @Test
    fun `selecting a moto should update ui state`() = runTest {
        createViewModel()
        val moto = Moto(1, "ABC789")
        
        viewModel.onMotoSelected(moto)

        assertEquals(moto, viewModel.uiState.value.selectedMoto)
    }

    @Test
    fun `validation should fail if fields are empty`() = runTest {
        createViewModel()
        assertEquals(false, viewModel.uiState.value.isSaveButtonEnabled)
    }
}
