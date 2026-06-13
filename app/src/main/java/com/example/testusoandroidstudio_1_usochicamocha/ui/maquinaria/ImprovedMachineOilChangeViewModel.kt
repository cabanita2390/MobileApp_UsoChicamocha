package com.example.testusoandroidstudio_1_usochicamocha.ui.maquinaria

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MachineOilChangeImprovedDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.OilAnalysisSosDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineOilChangeImprovedEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilAnalysisSosEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.OilChangeRequirementDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.MachineOilChangeResponseDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.OilAnalysisSosDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.CreateMachineOilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.CreateOilAnalysisSosRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImprovedMachineOilChangeUiState(
    val machines: List<MachineItem> = emptyList(),
    val selectedMachine: MachineItem? = null,
    val requirements: List<OilChangeRequirementDto> = emptyList(),
    val selectedRequirement: OilChangeRequirementDto? = null,
    val oils: List<Oil> = emptyList(),
    val selectedOil: Oil? = null,
    val hourStamp: String = "",
    val quantity: String = "",
    val motorOil: Boolean = true,
    val hydraulicOil: Boolean = false,
    val isLoading: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null,
    val isRoleAllowed: Boolean = true,
    val oilChanges: List<MachineOilChangeImprovedEntity> = emptyList(),
    val sosAnalyses: List<OilAnalysisSosEntity> = emptyList(),
    val sosPending: List<OilAnalysisSosEntity> = emptyList()
)

data class MachineItem(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val tipo: String,
    val horasOperacion: Int
)

@HiltViewModel
class ImprovedMachineOilChangeViewModel @Inject constructor(
    private val machineOilChangeDao: MachineOilChangeImprovedDao,
    private val sosAnalysisDao: OilAnalysisSosDao,
    private val getLocalOilsUseCase: GetLocalOilsUseCase,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private val ALLOWED_ROLES = setOf("SUPERVISOR_OPERATIVO", "OPERARIO", "MECHANIC", "ADMIN")
    }

    private val _uiState = MutableStateFlow(ImprovedMachineOilChangeUiState())
    val uiState: StateFlow<ImprovedMachineOilChangeUiState> = _uiState.asStateFlow()

    init {
        validateRoleAccess()
        loadOils()
        loadRequirements()
    }

    private fun validateRoleAccess() {
        viewModelScope.launch {
            val userRole = tokenManager.getRole().firstOrNull()
            val isAllowed = userRole != null && ALLOWED_ROLES.contains(userRole)
            _uiState.update { it.copy(isRoleAllowed = isAllowed) }
        }
    }

    private fun loadOils() {
        viewModelScope.launch {
            getLocalOilsUseCase().collect { list ->
                val forMachinery = list.filter { o ->
                    o.type.contains("MACHINERY", ignoreCase = true) ||
                    o.type.contains("MACHINE", ignoreCase = true)
                }
                _uiState.update { it.copy(oils = forMachinery) }
            }
        }
    }

    private fun loadRequirements() {
        viewModelScope.launch {
            try {
                val response = apiService.getOilChangeRequirements(assetType = "MACHINERY")
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(requirements = response.body() ?: emptyList())
                    }
                    Log.d("MachineOilVM", "✅ Requisitos cargados: ${response.body()?.size}")
                }
            } catch (e: Exception) {
                Log.e("MachineOilVM", "❌ Error: ${e.message}")
            }
        }
    }

    fun onMachineSelected(machine: MachineItem) {
        _uiState.update { it.copy(selectedMachine = machine, error = null) }
        loadMachineHistory(machine.id.toLong())
        loadSosAnalyses(machine.id.toLong())
    }

    fun onRequirementSelected(requirement: OilChangeRequirementDto) {
        _uiState.update { it.copy(selectedRequirement = requirement) }
    }

    fun onOilSelected(oil: Oil) {
        _uiState.update { it.copy(selectedOil = oil) }
    }

    fun onHourStampChanged(hours: String) {
        _uiState.update { it.copy(hourStamp = hours, error = null) }
    }

    fun onQuantityChanged(qty: String) {
        _uiState.update { it.copy(quantity = qty) }
    }

    fun onMotorOilChanged(value: Boolean) {
        _uiState.update { it.copy(motorOil = value) }
    }

    fun onHydraulicOilChanged(value: Boolean) {
        _uiState.update { it.copy(hydraulicOil = value) }
    }

    fun submitMachineOilChange() {
        viewModelScope.launch {
            val state = _uiState.value

            if (!validateForm(state)) return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                val request = CreateMachineOilChangeRequest(
                    machineId = state.selectedMachine!!.id.toLong(),
                    oilType = state.selectedRequirement!!.oilType,
                    motorOil = state.motorOil,
                    hydraulicOil = state.hydraulicOil,
                    brandId = state.selectedOil!!.id.toLong(),
                    quantity = state.quantity.toDouble(),
                    hourStamp = state.hourStamp.toInt(),
                    requirementId = state.selectedRequirement.id
                )

                val response = apiService.createMachineOilChange(request)

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
                        Log.i("MachineOilVM", "✅ Cambio registrado: ${changeData.id}")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Error: ${response.code()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MachineOilVM", "❌ Error: ${e.message}")
                _uiState.update {
                    it.copy(error = e.message ?: "Error desconocido", isLoading = false)
                }
            }
        }
    }

    fun submitSosAnalysis(
        hoursAtAnalysis: Int,
        nextChangeHours: Int,
        sosReportUrl: String,
        approvedByMechanic: String,
        observations: String
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedMachine == null) {
                _uiState.update { it.copy(error = "Seleccione máquina") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            try {
                val request = CreateOilAnalysisSosRequest(
                    machineId = state.selectedMachine.id.toLong(),
                    machineName = state.selectedMachine.nombre,
                    oilType = state.selectedRequirement?.oilType ?: "SYNTHETIC",
                    hoursAtAnalysis = hoursAtAnalysis,
                    nextChangeHours = nextChangeHours,
                    sosReportUrl = sosReportUrl,
                    approvedByMechanic = approvedByMechanic,
                    observations = observations
                )

                val response = apiService.createOilAnalysisSos(request)

                if (response.isSuccessful) {
                    val sosData = response.body()
                    if (sosData != null) {
                        saveSosLocally(sosData)
                        _uiState.update {
                            it.copy(submissionSuccess = true, isLoading = false)
                        }
                        Log.i("MachineOilVM", "✅ Análisis SOS creado: ${sosData.id}")
                    }
                } else {
                    _uiState.update {
                        it.copy(error = "Error: ${response.code()}", isLoading = false)
                    }
                }
            } catch (e: Exception) {
                Log.e("MachineOilVM", "❌ Error: ${e.message}")
                _uiState.update {
                    it.copy(error = e.message ?: "Error desconocido", isLoading = false)
                }
            }
        }
    }

    private fun loadMachineHistory(machineId: Long) {
        machineOilChangeDao.getByMachineId(machineId)
            .onEach { changes ->
                _uiState.update { it.copy(oilChanges = changes) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadSosAnalyses(machineId: Long) {
        sosAnalysisDao.getByMachineId(machineId)
            .onEach { analyses ->
                _uiState.update { it.copy(sosAnalyses = analyses) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun saveLocally(changeData: MachineOilChangeResponseDto) {
        val entity = MachineOilChangeImprovedEntity(
            machineId = changeData.machineId,
            oilType = changeData.oilType,
            brandName = changeData.brandName,
            quantity = changeData.quantity,
            hourStamp = changeData.hourStamp,
            nextChangeHours = changeData.nextChangeHours,
            percentageUsed = changeData.percentageUsed,
            motorOil = changeData.motorOil ?: true,
            hydraulicOil = changeData.hydraulicOil ?: false,
            dateStamp = changeData.dateStamp,
            oilDurability = changeData.oilDurability,
            requirementId = 0,
            isSynced = true
        )
        machineOilChangeDao.insert(entity)
    }

    private suspend fun saveSosLocally(sosData: OilAnalysisSosDto) {
        val entity = OilAnalysisSosEntity(
            machineId = sosData.machineId,
            machineName = sosData.machineName,
            analysisDate = sosData.analysisDate,
            oilType = sosData.oilType,
            hoursAtAnalysis = sosData.hoursAtAnalysis,
            nextChangeHours = sosData.nextChangeHours,
            sosReportUrl = sosData.sosReportUrl,
            approvedByMechanic = sosData.approvedByMechanic,
            observations = sosData.observations,
            isApproved = sosData.isApproved,
            extendedHours = sosData.extendedHours,
            authorizesExtension = sosData.authorizesExtension,
            createdAt = sosData.createdAt,
            isSynced = true
        )
        sosAnalysisDao.insert(entity)
    }

    fun onSubmissionSuccessHandled() {
        _uiState.update { it.copy(submissionSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateForm(state: ImprovedMachineOilChangeUiState): Boolean {
        val errors = mutableListOf<String>()

        if (state.selectedMachine == null) errors.add("Seleccione máquina")
        if (state.selectedRequirement == null) errors.add("Seleccione tipo de aceite")
        if (state.selectedOil == null) errors.add("Seleccione marca")
        if (state.hourStamp.isBlank()) errors.add("Ingrese horometro")
        if (state.quantity.isBlank()) errors.add("Ingrese cantidad")

        return if (errors.isNotEmpty()) {
            _uiState.update { it.copy(error = errors.joinToString("\n")) }
            false
        } else {
            true
        }
    }
}
