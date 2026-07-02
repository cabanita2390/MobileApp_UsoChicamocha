package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Lógica compartida entre VehiculoCambioAceiteViewModel y MotoCambioAceiteViewModel.
 * No es un ViewModel en sí — cada ViewModel lo instancia, le pasa su propio
 * [CambioAceiteStrategy] y llama [start] desde su propio init con su viewModelScope
 * (evita depender de viewModelScope antes de que el ViewModel termine de construirse).
 */
class CambioAceiteCore(
    private val strategy: CambioAceiteStrategy,
    private val getLocalOilsUseCase: GetLocalOilsUseCase,
    private val syncOilsUseCase: SyncOilsUseCase,
    private val tokenManager: TokenManager,
    private val logTag: String
) {
    companion object {
        val ALLOWED_ROLES = setOf("SUPERVISOR_OPERATIVO", "ACEITE", "MECANIC", "ADMIN")
    }

    private lateinit var scope: CoroutineScope

    private val _uiState = MutableStateFlow(CambioAceiteUiState())
    val uiState: StateFlow<CambioAceiteUiState> = _uiState.asStateFlow()

    fun start(scope: CoroutineScope) {
        this.scope = scope
        validateRoleAccess()
        scope.launch { strategy.onInit() }
        loadAssets()
        loadOils()
        syncOilsIfNeeded()
    }

    private fun validateRoleAccess() {
        scope.launch {
            val userRole = tokenManager.getRole().firstOrNull()
            val isAllowed = userRole != null && ALLOWED_ROLES.contains(userRole)
            _uiState.update { it.copy(isRoleAllowed = isAllowed) }
            if (!isAllowed) {
                _uiState.update {
                    it.copy(error = "No tiene permisos para registrar cambios de aceite. Roles requeridos: SUPERVISOR_OPERATIVO, MECANIC o ADMIN.")
                }
            }
        }
    }

    private fun loadAssets() {
        strategy.assetsFlow()
            .onEach { items -> _uiState.update { it.copy(assets = items) } }
            .launchIn(scope)
    }

    private fun loadOils() {
        scope.launch {
            getLocalOilsUseCase().collect { list ->
                val filtered = list.filter { o ->
                    val t = o.type.trim()
                    t.equals("OIL_VEHICLE", ignoreCase = true) ||
                        t.contains("VEHICLE", ignoreCase = true) ||
                        t.equals("motor", ignoreCase = true)
                }
                _uiState.update { it.copy(oilBrands = filtered) }
            }
        }
    }

    /** Si al abrir la pantalla todavía no hay aceites en local (p.ej. el sync de fondo del
     * arranque de la app no ha terminado), dispara un sync propio en vez de dejar al usuario
     * atrapado mirando el dropdown vacío hasta que el ciclo automático de 15 min lo resuelva. */
    private fun syncOilsIfNeeded() {
        scope.launch {
            val currentOils = getLocalOilsUseCase().first()
            if (currentOils.isEmpty()) {
                syncOils()
            }
        }
    }

    /** Sincronización manual/automática de aceites. Expuesta también para el botón de
     * "Reintentar" en la pantalla, por si el sync automático falla. */
    fun syncOils() {
        scope.launch {
            _uiState.update { it.copy(isSyncingOils = true) }
            try {
                syncOilsUseCase()
            } catch (e: Exception) {
                Log.e(logTag, "Error al sincronizar aceites", e)
            } finally {
                _uiState.update { it.copy(isSyncingOils = false) }
            }
        }
    }

    fun onAssetSelected(asset: AssetOilChangeItem) {
        _uiState.update { it.copy(selectedAsset = asset, kmAtChange = strategy.kmPrefillOnSelect(asset)) }
    }

    fun onOilTypeChange(type: String) {
        _uiState.update { it.copy(oilType = type, selectedOil = null) }
    }

    fun onOilSelected(oil: Oil) {
        _uiState.update { it.copy(selectedOil = oil) }
    }

    fun onKmAtChangeChange(km: String) {
        _uiState.update { it.copy(kmAtChange = km) }
    }

    fun onIntervalKmChange(km: String) {
        _uiState.update { it.copy(intervalKm = km) }
    }

    fun onQuantityChange(q: String) {
        val sanitized = q.replace(',', '.')
        if (sanitized.count { it == '.' } <= 1) {
            _uiState.update { it.copy(quantity = sanitized) }
        }
    }

    fun onAirFilterChanged(value: Boolean) {
        _uiState.update { it.copy(airFilterChanged = value) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onSubmissionSuccessHandled() {
        _uiState.update { it.copy(submissionSuccess = false) }
    }

    fun submit() {
        scope.launch {
            val state = _uiState.value

            val userRole = tokenManager.getRole().firstOrNull()
            if (userRole == null || !ALLOWED_ROLES.contains(userRole)) {
                _uiState.update {
                    it.copy(error = "No tiene permisos para registrar cambios de aceite. Roles requeridos: SUPERVISOR_OPERATIVO, MECANIC o ADMIN.")
                }
                return@launch
            }

            if (state.selectedAsset == null) {
                _uiState.update { it.copy(error = "Seleccione un activo.") }
                return@launch
            }
            if (state.selectedOil == null) {
                _uiState.update { it.copy(error = "Seleccione una marca de aceite.") }
                return@launch
            }
            val km = state.kmAtChange.toIntOrNull()
            if (km == null || km <= 0) {
                _uiState.update { it.copy(error = "Ingrese el kilometraje actual.") }
                return@launch
            }
            val interval = state.intervalKm.toIntOrNull()
            if (interval == null || interval <= 0) {
                _uiState.update { it.copy(error = "Ingrese el intervalo del próximo cambio.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                strategy.save(
                    asset = state.selectedAsset,
                    oilType = state.oilType.ifBlank { "motor" },
                    oil = state.selectedOil,
                    quantity = state.quantity.toDoubleOrNull(),
                    km = km,
                    interval = interval,
                    airFilterChanged = state.airFilterChanged
                )
                _uiState.update { it.copy(isLoading = false, submissionSuccess = true) }
            } catch (e: Exception) {
                Log.e(logTag, "Error saving oil change", e)
                _uiState.update { it.copy(isLoading = false, error = "Error al guardar: ${e.message}") }
            }
        }
    }
}
