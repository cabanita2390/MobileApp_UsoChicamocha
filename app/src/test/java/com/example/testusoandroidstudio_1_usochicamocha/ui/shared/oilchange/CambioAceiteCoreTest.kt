package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange

import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * CambioAceiteCore ahora es compartido por VehiculoCambioAceiteViewModel y
 * MotoCambioAceiteViewModel (etapa de unificación de 3.3) — antes de esta unificación
 * ninguno de los dos tenía tests propios. Se cubre acá la validación del formulario y
 * el happy path del submit, que es exactamente donde un bug afectaría a ambos flujos
 * a la vez.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CambioAceiteCoreTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var tokenManager: TokenManager
    @MockK
    lateinit var getLocalOilsUseCase: GetLocalOilsUseCase
    @MockK
    lateinit var syncOilsUseCase: SyncOilsUseCase
    @MockK(relaxed = true)
    lateinit var strategy: CambioAceiteStrategy

    private val oil = Oil(id = 1, type = "OIL_VEHICLE", name = "Mobil 15W40")
    private val asset = AssetOilChangeItem(id = 1, placa = "ABC123", marca = "Chevrolet", kilometrajeActual = 5000)

    private lateinit var core: CambioAceiteCore

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { tokenManager.getRole() } returns flowOf("ADMIN")
        every { getLocalOilsUseCase() } returns flowOf(listOf(oil))
        every { strategy.assetsFlow() } returns flowOf(listOf(asset))
        every { strategy.kmPrefillOnSelect(any()) } returns "5000"
        coEvery { strategy.onInit() } just Runs
        coEvery { strategy.save(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs

        core = CambioAceiteCore(strategy, getLocalOilsUseCase, syncOilsUseCase, tokenManager, logTag = "Test")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `al iniciar carga los activos y aceites de la strategy`() = runTest {
        core.start(this)
        advanceUntilIdle()

        assertEquals(listOf(asset), core.uiState.value.assets)
        assertEquals(listOf(oil), core.uiState.value.oilBrands)
    }

    @Test
    fun `submit sin activo seleccionado falla con mensaje claro`() = runTest {
        core.start(this)
        advanceUntilIdle()

        core.onOilSelected(oil)
        core.onKmAtChangeChange("5000")
        core.onIntervalKmChange("3000")
        core.submit()
        advanceUntilIdle()

        assertEquals("Seleccione un activo.", core.uiState.value.error)
        coVerify(exactly = 0) { strategy.save(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `submit con km invalido falla sin llamar a la strategy`() = runTest {
        core.start(this)
        advanceUntilIdle()

        core.onAssetSelected(asset)
        core.onOilSelected(oil)
        core.onKmAtChangeChange("0")
        core.onIntervalKmChange("3000")
        core.submit()
        advanceUntilIdle()

        assertEquals("Ingrese el kilometraje actual.", core.uiState.value.error)
        coVerify(exactly = 0) { strategy.save(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `submit con datos validos guarda via la strategy`() = runTest {
        core.start(this)
        advanceUntilIdle()

        core.onAssetSelected(asset)
        core.onOilSelected(oil)
        core.onKmAtChangeChange("5200")
        core.onIntervalKmChange("3000")
        core.onQuantityChange("3.5")
        core.submit()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            strategy.save(asset, "motor", oil, 3.5, 5200, 3000, false, any())
        }
        assertNull(core.uiState.value.error)
        assertEquals(true, core.uiState.value.submissionSuccess)
    }

    @Test
    fun `sin rol permitido bloquea el submit`() = runTest {
        every { tokenManager.getRole() } returns flowOf("OPERARIO")
        core.start(this)
        advanceUntilIdle()

        core.onAssetSelected(asset)
        core.onOilSelected(oil)
        core.onKmAtChangeChange("5200")
        core.onIntervalKmChange("3000")
        core.submit()
        advanceUntilIdle()

        assertEquals(false, core.uiState.value.isRoleAllowed)
        coVerify(exactly = 0) { strategy.save(any(), any(), any(), any(), any(), any(), any(), any()) }
    }
}
