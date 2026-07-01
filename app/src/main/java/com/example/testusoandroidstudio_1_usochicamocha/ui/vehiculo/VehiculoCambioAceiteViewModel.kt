package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoOilChangeRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehiculoCambioAceiteUiState(
    val vehicles: List<VehiculoItem> = emptyList(),
    val selectedVehicle: VehiculoItem? = null,
    val vehicleOilBrands: List<Oil> = emptyList(),
    val selectedOil: Oil? = null,
    val oilType: String = "motor",
    val kmAtChange: String = "",
    val intervalKm: String = "",
    val quantity: String = "",
    val airFilterChanged: Boolean = false,
    val isLoading: Boolean = false,
    val isSyncingOils: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null,
    val isRoleAllowed: Boolean = true
)

@HiltViewModel
class VehiculoCambioAceiteViewModel @Inject constructor(
    private val vehiculoInspectionRepository: VehiculoInspectionRepository,
    private val oilChangeRepository: VehiculoOilChangeRepository,
    private val getLocalOilsUseCase: GetLocalOilsUseCase,
    private val syncOilsUseCase: SyncOilsUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private val ALLOWED_ROLES = setOf("SUPERVISOR_OPERATIVO", "ACEITE", "MECANIC", "ADMIN")
    }

    private val _uiState = MutableStateFlow(VehiculoCambioAceiteUiState())
    val uiState: StateFlow<VehiculoCambioAceiteUiState> = _uiState.asStateFlow()

    init {
        validateRoleAccess()
        loadVehicles()
        loadOils()
        syncOilsIfNeeded()
    }

    private fun validateRoleAccess() {
        viewModelScope.launch {
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

    private fun loadVehicles() {
        vehiculoInspectionRepository.getLocalVehiclesFlow()
            .onEach { list ->
                // Excluir MOTOCICLETA (que tiene su propia sección)
                // Incluye todos los otros tipos de vehículos (AUTOMOVIL, CAMION, BUS, etc)
                val items = list
                    .filter { !it.tipoVehiculo.equals("MOTOCICLETA", ignoreCase = true) }
                    .map { VehiculoItem(it.idVehiculo, it.placa, it.marca, it.tipoVehiculo, it.kilometrajeActual) }
                Log.d("VehiculoCambioAceiteVM", "📋 Vehículos cargados: ${items.size} (excluyendo MOTOCICLETA)")
                items.forEach { item ->
                    Log.d("VehiculoCambioAceiteVM", "   - ${item.placa} (${item.tipoVehiculo})")
                }
                _uiState.update { it.copy(vehicles = items) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadOils() {
        viewModelScope.launch {
            getLocalOilsUseCase().collect { list ->
                val forVehicle = list.filter { o ->
                    val t = o.type.trim()
                    t.equals("OIL_VEHICLE", ignoreCase = true) ||
                        t.contains("VEHICLE", ignoreCase = true) ||
                        t.equals("motor", ignoreCase = true)
                }
                _uiState.update { it.copy(vehicleOilBrands = forVehicle) }
            }
        }
    }

    /** Si al abrir la pantalla todavía no hay aceites en local (p.ej. el sync de fondo del
     * arranque de la app no ha terminado), dispara un sync propio en vez de dejar al usuario
     * atrapado mirando el dropdown vacío hasta que el ciclo automático de 15 min lo resuelva. */
    private fun syncOilsIfNeeded() {
        viewModelScope.launch {
            val currentOils = getLocalOilsUseCase().first()
            if (currentOils.isEmpty()) {
                syncOils()
            }
        }
    }

    /** Sincronización manual/automática de aceites. Expuesta también para el botón de
     * "Reintentar" en la pantalla, por si el sync automático falla. */
    fun syncOils() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingOils = true) }
            try {
                syncOilsUseCase()
            } catch (e: Exception) {
                Log.e("VehiculoCambioAceiteVM", "Error al sincronizar aceites", e)
            } finally {
                _uiState.update { it.copy(isSyncingOils = false) }
            }
        }
    }

    fun onVehicleSelected(vehicle: VehiculoItem) {
        _uiState.update { it.copy(selectedVehicle = vehicle, kmAtChange = vehicle.kilometrajeActual.toString()) }
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
        viewModelScope.launch {
            val state = _uiState.value

            // Validar rol del usuario
            val userRole = tokenManager.getRole().firstOrNull()
            if (userRole == null || !ALLOWED_ROLES.contains(userRole)) {
                _uiState.update {
                    it.copy(error = "No tiene permisos para registrar cambios de aceite. Roles requeridos: SUPERVISOR_OPERATIVO, MECANIC o ADMIN.")
                }
                return@launch
            }

            // Validar datos del formulario
            if (state.selectedVehicle == null) {
                _uiState.update { it.copy(error = "Seleccione un vehículo.") }
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

            // Proceder con el guardado
            _uiState.update { it.copy(isLoading = true) }
            try {
                val entity = VehiculoOilChangeEntity(
                    placa = state.selectedVehicle!!.placa,
                    timestamp = System.currentTimeMillis(),
                    oilType = state.oilType.ifBlank { "motor" },
                    oilBrandId = state.selectedOil!!.id.toLong(),
                    oilBrandName = state.selectedOil!!.name,
                    quantity = state.quantity.toDoubleOrNull(),
                    kmAtChange = km,
                    intervalKm = interval,
                    airFilterChanged = state.airFilterChanged,
                    assetType = "VEHICLE"
                )
                oilChangeRepository.saveLocally(entity)
                oilChangeRepository.syncPending()
                _uiState.update { it.copy(isLoading = false, submissionSuccess = true) }
            } catch (e: Exception) {
                Log.e("VehiculoCambioAcVM", "Error saving oil change", e)
                _uiState.update { it.copy(isLoading = false, error = "Error al guardar: ${e.message}") }
            }
        }
    }
}
