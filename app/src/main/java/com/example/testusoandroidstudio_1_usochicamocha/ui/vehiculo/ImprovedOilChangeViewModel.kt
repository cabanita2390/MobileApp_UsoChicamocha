package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehicleOilChangeImprovedDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehicleOilChangeImprovedEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.OilChangeRequirementDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.VehicleOilChangeResponseDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.CreateVehicleOilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImprovedOilChangeUiState(
    val vehicles: List<VehiculoItem> = emptyList(),
    val selectedVehicle: VehiculoItem? = null,
    val requirements: List<OilChangeRequirementDto> = emptyList(),
    val selectedRequirement: OilChangeRequirementDto? = null,
    val oils: List<Oil> = emptyList(),
    val selectedOil: Oil? = null,
    val kmAtChange: String = "",
    val quantity: String = "",
    val airFilterChanged: Boolean = false,
    val isLoading: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null,
    val isRoleAllowed: Boolean = true,
    val oilChanges: List<VehicleOilChangeImprovedEntity> = emptyList()
)

@HiltViewModel
class ImprovedOilChangeViewModel @Inject constructor(
    private val vehiculoInspectionRepository: VehiculoInspectionRepository,
    private val vehicleOilChangeDao: VehicleOilChangeImprovedDao,
    private val getLocalOilsUseCase: GetLocalOilsUseCase,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private val ALLOWED_ROLES = setOf("SUPERVISOR_OPERATIVO", "OPERARIO", "MECHANIC", "ADMIN")
    }

    private val _uiState = MutableStateFlow(ImprovedOilChangeUiState())
    val uiState: StateFlow<ImprovedOilChangeUiState> = _uiState.asStateFlow()

    init {
        validateRoleAccess()
        loadVehicles()
        loadOils()
        loadRequirements()
    }

    private fun validateRoleAccess() {
        viewModelScope.launch {
            val userRole = tokenManager.getRole().firstOrNull()
            val isAllowed = userRole != null && ALLOWED_ROLES.contains(userRole)
            _uiState.update { it.copy(isRoleAllowed = isAllowed) }
            if (!isAllowed) {
                _uiState.update {
                    it.copy(error = "No tiene permisos para registrar cambios de aceite")
                }
            }
        }
    }

    private fun loadVehicles() {
        vehiculoInspectionRepository.getLocalVehiclesFlow()
            .onEach { list ->
                val items = list
                    .filter { !it.tipoVehiculo.equals("MOTOCICLETA", ignoreCase = true) }
                    .map { VehiculoItem(
                        idVehiculo = it.idVehiculo,
                        placa = it.placa,
                        marca = it.marca,
                        tipoVehiculo = it.tipoVehiculo,
                        kilometrajeActual = it.kilometrajeActual
                    ) }
                Log.d("ImprovedOilChangeVM", "📋 Vehículos cargados: ${items.size}")
                _uiState.update { it.copy(vehicles = items) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadOils() {
        viewModelScope.launch {
            getLocalOilsUseCase().collect { list ->
                val forVehicle = list.filter { o ->
                    o.type.contains("VEHICLE", ignoreCase = true) ||
                    o.type.equals("motor", ignoreCase = true)
                }
                _uiState.update { it.copy(oils = forVehicle) }
            }
        }
    }

    private fun loadRequirements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getOilChangeRequirements(assetType = "VEHICLE")
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            requirements = response.body() ?: emptyList(),
                            isLoading = false
                        )
                    }
                    Log.d("ImprovedOilChangeVM", "✅ Requisitos cargados: ${response.body()?.size}")
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Error cargando requisitos",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ImprovedOilChangeVM", "❌ Error: ${e.message}")
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error desconocido",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onVehicleSelected(vehicle: VehiculoItem) {
        _uiState.update {
            it.copy(
                selectedVehicle = vehicle,
                kmAtChange = vehicle.kilometrajeActual.toString(),
                error = null
            )
        }
    }

    fun onRequirementSelected(requirement: OilChangeRequirementDto) {
        _uiState.update { it.copy(selectedRequirement = requirement) }
    }

    fun onOilSelected(oil: Oil) {
        _uiState.update { it.copy(selectedOil = oil) }
    }

    fun onKmAtChangeChanged(km: String) {
        _uiState.update { it.copy(kmAtChange = km, error = null) }
    }

    fun onQuantityChanged(qty: String) {
        _uiState.update { it.copy(quantity = qty) }
    }

    fun onAirFilterChanged(changed: Boolean) {
        _uiState.update { it.copy(airFilterChanged = changed) }
    }

    fun submitOilChange() {
        viewModelScope.launch {
            val state = _uiState.value

            if (!validateForm(state)) return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                val request = CreateVehicleOilChangeRequest(
                    placa = state.selectedVehicle!!.placa,
                    oilType = state.selectedRequirement!!.oilType,
                    brandId = state.selectedOil!!.id.toLong(),
                    quantity = state.quantity.toDouble(),
                    kmAtChange = state.kmAtChange.toInt(),
                    airFilterChanged = state.airFilterChanged,
                    requirementId = state.selectedRequirement.id
                )

                val response = apiService.createVehicleOilChange(request)

                if (response.isSuccessful) {
                    val changeData = response.body()
                    if (changeData != null) {
                        saveLocally(changeData)
                        _uiState.update {
                            it.copy(
                                submissionSuccess = true,
                                isLoading = false,
                                error = null
                            )
                        }
                        Log.i("ImprovedOilChangeVM", "✅ Cambio de aceite registrado: ${changeData.id}")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Error al registrar cambio: ${response.code()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ImprovedOilChangeVM", "❌ Error: ${e.message}")
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error desconocido",
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun saveLocally(changeData: VehicleOilChangeResponseDto) {
        val entity = VehicleOilChangeImprovedEntity(
            placa = changeData.placa,
            oilType = changeData.oilType,
            brandName = changeData.brandName,
            quantity = changeData.quantity,
            kmAtChange = changeData.kmAtChange,
            nextChangeKm = changeData.nextChangeKm,
            percentageUsed = changeData.percentageUsed,
            airFilterChanged = changeData.airFilterChanged ?: false,
            dateStamp = changeData.dateStamp,
            oilDurability = changeData.oilDurability,
            requirementId = 0,
            isSynced = true
        )
        vehicleOilChangeDao.insert(entity)
    }

    private fun validateForm(state: ImprovedOilChangeUiState): Boolean {
        val errors = mutableListOf<String>()

        if (state.selectedVehicle == null) errors.add("Seleccione un vehículo")
        if (state.selectedRequirement == null) errors.add("Seleccione tipo de aceite")
        if (state.selectedOil == null) errors.add("Seleccione marca de aceite")
        if (state.kmAtChange.isBlank()) errors.add("Ingrese kilometraje")
        if (state.quantity.isBlank()) errors.add("Ingrese cantidad")

        return if (errors.isNotEmpty()) {
            _uiState.update { it.copy(error = errors.joinToString("\n")) }
            false
        } else {
            true
        }
    }
}
