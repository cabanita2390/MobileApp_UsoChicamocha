package com.example.testusoandroidstudio_1_usochicamocha.ui.mantenimiento

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.GetLocalMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.GetPendingMaintenanceFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.SaveMaintenanceFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MantenimientoViewModel @Inject constructor(
    private val saveMaintenanceFormUseCase: SaveMaintenanceFormUseCase,
    private val getLocalMachinesUseCase: GetLocalMachinesUseCase,
    private val getLocalOilsUseCase: GetLocalOilsUseCase,
    private val syncOilsUseCase: SyncOilsUseCase,
    private val getPendingMaintenanceFormsUseCase: GetPendingMaintenanceFormsUseCase,
    private val getMaintenanceByIdUseCase: com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.GetMaintenanceByIdUseCase,
    private val localSyncCoordinator: com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MantenimientoUiState())
    val uiState: StateFlow<MantenimientoUiState> = _uiState.asStateFlow()


    init {
        loadMachines()
        loadOils()
        syncOilsIfNeeded()
        loadPendingForms()
        checkIfEditing()
    }

    private fun checkIfEditing() {
        val maintenanceId = savedStateHandle.get<Int>("maintenanceId")
        if (maintenanceId != null && maintenanceId != -1) {
            viewModelScope.launch {
                val maintenance = getMaintenanceByIdUseCase(maintenanceId)
                maintenance?.let { form ->
                    _uiState.update {
                        it.copy(
                            editingFormId = form.id,
                            selectedMachine = it.machines.find { machine -> machine.id == form.machineId }, // Esto puede fallar si machines no ha cargado aun.
                            // Mejor estrategia: esperar a que machines y oils carguen, o setear IDs y buscar luego.
                            // Por simplicidad, asumimos que cargan rápido o re-intentamos.
                            // Una mejor opción es guardar el ID y cuando carguen las listas, seleccionar.
                            // Pero para MVP:
                            quantity = form.quantity.toString(),
                            currentHourMeter = form.currentHourMeter.toString(),
                            averageHoursChange = form.averageHoursChange.toString(),
                            maintenanceType = form.type
                        )
                    }
                    // Post-procesamiento para seleccionar aceite y maquina si ya cargaron
                    // OJO: selectedMachine y selectedOil dependen de las listas.
                    // Si las listas no estan listas, esto será null.
                    // Vamos a manejar esto en loadMachines y loadOils tambien.
                }
            }
        }
    }

    // ... (loadPendingForms, loadOils, loadMachines se mantienen igual, pero podrian necesitar ajuste para seleccionar si editingFormId existe)
    // Para simplificar, vamos a hacer que loadMachines y loadOils intenten re-seleccionar si hay un editingFormId y el objeto es null.
    // Pero dado que loadMachines y loadOils son asincronos, lo mejor es disparar un evento de "intentar prellenar" cuando terminen.
    // O simplemente, en checkIfEditing, esperar un poco o reaccionar a los cambios de machines/oils.
    // Vamos a hacerlo simple: En loadMachines, si editingFormId != null, buscamos.

    private fun loadPendingForms() {
        viewModelScope.launch {
            getPendingMaintenanceFormsUseCase().collect { forms ->
                _uiState.update { it.copy(pendingForms = forms) }
            }
        }
    }
    private fun loadOils() {
        viewModelScope.launch {
            getLocalOilsUseCase().collect { oils ->
                _uiState.update {
                    it.copy(
                        allOils = oils,
                        motorOils = oils.filter { oil -> oil.type.equals("motor", ignoreCase = true) },
                        hydraulicOils = oils.filter { oil -> oil.type.equals("hidraulico", ignoreCase = true) }
                    )
                }
                // Intentar seleccionar el aceite si estamos editando
                val maintenanceId = savedStateHandle.get<Int>("maintenanceId")
                if (maintenanceId != null && maintenanceId != -1) {
                     val maintenance = getMaintenanceByIdUseCase(maintenanceId)
                     maintenance?.let { form ->
                         _uiState.update { state ->
                             state.copy(selectedOil = oils.find { it.id == form.brandId })
                         }
                     }
                }
            }
        }
    }
    /** Si al abrir la pantalla todavía no hay aceites en local (p.ej. el sync de fondo del
     * arranque de la app no ha terminado), dispara un sync propio en vez de dejar al usuario
     * atrapado mirando la lista de aceites vacía. */
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
                Log.e("MantenimientoVM", "Error al sincronizar aceites", e)
            } finally {
                _uiState.update { it.copy(isSyncingOils = false) }
            }
        }
    }

    private fun loadMachines() {
        viewModelScope.launch {
            getLocalMachinesUseCase().collect { resource -> // El flow emite un Resource
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                // Extraemos la data del estado Success
                                machines = resource.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                        // Intentar seleccionar la maquina si estamos editando
                        val maintenanceId = savedStateHandle.get<Int>("maintenanceId")
                        if (maintenanceId != null && maintenanceId != -1) {
                            val maintenance = getMaintenanceByIdUseCase(maintenanceId)
                            maintenance?.let { form ->
                                _uiState.update { state ->
                                    state.copy(selectedMachine = state.machines.find { it.id == form.machineId })
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = resource.message ?: "Error al cargar máquinas",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun onFormEvent(event: MantenimientoFormEvent) {
        when (event) {
            is MantenimientoFormEvent.MachineSelected -> {
                _uiState.update { it.copy(selectedMachine = event.machine) }
            }
            is MantenimientoFormEvent.OilSelected -> { // <-- CAMBIA EL HANDLER
                _uiState.update { it.copy(selectedOil = event.oil) }
            }
            is MantenimientoFormEvent.QuantityChanged -> {
                // Sanitize input: replace comma with dot to support both formats
                val sanitizedQuantity = event.quantity.replace(',', '.')
                // Allow update only if there is at most one dot
                if (sanitizedQuantity.count { it == '.' } <= 1) {
                    _uiState.update { it.copy(quantity = sanitizedQuantity) }
                }
            }
            is MantenimientoFormEvent.CurrentHourMeterChanged -> {
                _uiState.update { it.copy(currentHourMeter = event.hourMeter) }
            }
            is MantenimientoFormEvent.AverageHoursChangeChanged -> {
                _uiState.update { it.copy(averageHoursChange = event.hours) }
            }
            is MantenimientoFormEvent.MaintenanceTypeChanged -> {
                _uiState.update { it.copy(maintenanceType = event.type) }
            }
            MantenimientoFormEvent.Submit -> {
                submitForm()
            }
        }
    }

    private fun submitForm() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedMachine == null || state.maintenanceType == null || state.selectedOil == null) {
                _uiState.update { it.copy(error = "Debe seleccionar una máquina y un tipo de mantenimiento.") }
                return@launch
            }

            val form = Maintenance(
                id = state.editingFormId ?: 0, // Usar ID existente si se edita, o 0 para nuevo (Room autogenera)
                machineId = state.selectedMachine.id,
                dateTime = Date().time, // Actualizamos la fecha al momento de la edición/creación
                brand = state.selectedOil.name,
                brandId = state.selectedOil.id,
                quantity = state.quantity.toDoubleOrNull() ?: 0.0,
                currentHourMeter = state.currentHourMeter.toIntOrNull() ?: 0,
                averageHoursChange = state.averageHoursChange.toIntOrNull() ?: 0,
                type = state.maintenanceType,
                isSynced = false,
                isSyncing = false,
                syncError = null // Limpiamos el error previo al guardar
            )

            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = saveMaintenanceFormUseCase(form)

            result.onSuccess {
                // 2. Usar el coordinador para iniciar la sincronización
                localSyncCoordinator.coordinateSync(
                    LocalSyncCoordinator.SyncTrigger.MaintenanceSaved(state.maintenanceType ?: "Unknown")
                )

                // Esto solo limpia los campos del formulario, pero mantiene
                // las listas de máquinas y aceites ya cargadas.
                _uiState.update { currentState ->
                    currentState.copy(
                        submissionSuccess = true,
                        isLoading = false,
                        selectedOil = null,
                        quantity = "",
                        currentHourMeter = "",
                        averageHoursChange = "",
                        selectedMachine = null,
                        maintenanceType = null,
                        editingFormId = null // Resetear modo edición
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        error = exception.message ?: "Error al guardar el formulario.",
                        isLoading = false
                    )
                }
            }
        }
    }

    // La función triggerImmediateSync ya no es necesaria
    // La eliminamos para limpiar.


    fun onSubmissionSuccessHandled() {
        _uiState.update { it.copy(submissionSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class MantenimientoUiState(
    val machines: List<Machine> = emptyList(),
    val selectedMachine: Machine? = null,
    val selectedOil: Oil? = null,
    val quantity: String = "",
    val currentHourMeter: String = "",
    val averageHoursChange: String = "",
    val maintenanceType: String? = null,
    val isLoading: Boolean = false,
    val isSyncingOils: Boolean = false,
    val error: String? = null,
    val submissionSuccess: Boolean = false,
    val motorOils: List<Oil> = emptyList(),
    val hydraulicOils: List<Oil> = emptyList(),
    val allOils: List<Oil> = emptyList(),
    val pendingForms: List<Maintenance> = emptyList(),
    val editingFormId: Int? = null // ID del formulario que se está editando
)

sealed class MantenimientoFormEvent {
    data class MachineSelected(val machine: Machine) : MantenimientoFormEvent()
    data class OilSelected(val oil: Oil) : MantenimientoFormEvent()
    data class QuantityChanged(val quantity: String) : MantenimientoFormEvent()
    data class CurrentHourMeterChanged(val hourMeter: String) : MantenimientoFormEvent()
    data class AverageHoursChangeChanged(val hours: String) : MantenimientoFormEvent()
    data class MaintenanceTypeChanged(val type: String) : MantenimientoFormEvent()
    object Submit : MantenimientoFormEvent()
}
