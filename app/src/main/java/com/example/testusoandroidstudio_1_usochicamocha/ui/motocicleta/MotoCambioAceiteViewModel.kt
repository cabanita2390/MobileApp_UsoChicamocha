package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MotoRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MotoOilChangeRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncMotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MotoItem(
    val id: Int,
    val placa: String,
    val marca: String? = null
)

data class MotoCambioAceiteUiState(
    val motos: List<MotoItem> = emptyList(),
    val selectedMoto: MotoItem? = null,
    val motoOilBrands: List<Oil> = emptyList(),
    val selectedOil: Oil? = null,
    val oilType: String = "motor",
    val kmAtChange: String = "",
    val intervalKm: String = "",
    val quantity: String = "",
    val airFilterChanged: Boolean = false,
    val isLoading: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null,
    val isRoleAllowed: Boolean = true
)

@HiltViewModel
class MotoCambioAceiteViewModel @Inject constructor(
    private val motoRepository: MotoRepository,
    private val oilChangeRepository: MotoOilChangeRepository,
    private val getLocalOilsUseCase: GetLocalOilsUseCase,
    private val syncMotosUseCase: SyncMotosUseCase,
    private val syncOilsUseCase: SyncOilsUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private val ALLOWED_ROLES = setOf("SUPERVISOR_OPERATIVO", "ACEITE", "MECANIC", "ADMIN")
    }

    private val _uiState = MutableStateFlow(MotoCambioAceiteUiState())
    val uiState: StateFlow<MotoCambioAceiteUiState> = _uiState.asStateFlow()

    init {
        validateRoleAccess()
        syncMotosAndOils()
        loadMotos()
        loadOils()
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

    private fun syncMotosAndOils() {
        viewModelScope.launch {
            try {
                syncMotosUseCase()
                syncOilsUseCase()
            } catch (e: Exception) {
                Log.e("MotoCambioAcVM", "Error syncing data: ${e.message}")
            }
        }
    }

    private fun loadMotos() {
        motoRepository.getLocalMotos()
            .onEach { list ->
                val items = list.map { MotoItem(it.id, it.placa, it.marca) }
                _uiState.update { it.copy(motos = items) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadOils() {
        viewModelScope.launch {
            getLocalOilsUseCase().collect { list ->
                val forMoto = list.filter { o ->
                    val t = o.type.trim()
                    t.equals("OIL_VEHICLE", ignoreCase = true) ||
                        t.contains("VEHICLE", ignoreCase = true) ||
                        t.equals("motor", ignoreCase = true)
                }
                _uiState.update { it.copy(motoOilBrands = forMoto) }
            }
        }
    }

    fun onMotoSelected(moto: MotoItem) {
        _uiState.update { it.copy(selectedMoto = moto, kmAtChange = "") }
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
            if (state.selectedMoto == null) {
                _uiState.update { it.copy(error = "Seleccione una motocicleta.") }
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
                val entity = MotoOilChangeEntity(
                    placa = state.selectedMoto!!.placa,
                    timestamp = System.currentTimeMillis(),
                    oilType = state.oilType.ifBlank { "motor" },
                    oilBrandId = state.selectedOil!!.id.toLong(),
                    oilBrandName = state.selectedOil!!.name,
                    quantity = state.quantity.toDoubleOrNull(),
                    kmAtChange = km,
                    intervalKm = interval,
                    airFilterChanged = state.airFilterChanged
                )
                oilChangeRepository.saveLocally(entity)
                oilChangeRepository.syncPending()
                _uiState.update { it.copy(isLoading = false, submissionSuccess = true) }
            } catch (e: Exception) {
                Log.e("MotoCambioAcVM", "Error saving oil change", e)
                _uiState.update { it.copy(isLoading = false, error = "Error al guardar: ${e.message}") }
            }
        }
    }
}
