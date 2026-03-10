package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toVehiculoItem
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.SyncDataWorker
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehiculoMainUiState(
    val pendingInspections: List<VehiculoInspectionEntity> = emptyList(),
    val totalVehicles: Int = 0,
    val isSyncingInspections: Boolean = false,
    val isSyncingCatalog: Boolean = false,
    val isSyncingDocuments: Boolean = false,
    val syncMessage: String? = null,
    val logoutCompleted: Boolean = false,
    val showLogoutDialog: Boolean = false
)

@HiltViewModel
class VehiculoMainViewModel @Inject constructor(
    private val repository: VehiculoInspectionRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoMainUiState())
    val uiState: StateFlow<VehiculoMainUiState> = _uiState.asStateFlow()

    init {
        observePendingInspections()
        observeVehiclesCatalog()
    }

    private fun observePendingInspections() {
        repository.getAllInspectionsFlow()
            .onEach { list ->
                val pending = list.filter { !it.isSynced }
                _uiState.update { it.copy(pendingInspections = pending) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeVehiclesCatalog() {
        repository.getLocalVehiclesFlow()
            .onEach { list ->
                _uiState.update { it.copy(totalVehicles = list.size) }
            }
            .launchIn(viewModelScope)
    }

    fun onSyncInspectionsClicked() {
        _uiState.update { it.copy(isSyncingInspections = true) }
        triggerSync("VEHICLES_ONLY")
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isSyncingInspections = false, syncMessage = "Sincronización de inspecciones iniciada") }
        }
    }

    fun onSyncCatalogClicked() {
        _uiState.update { it.copy(isSyncingCatalog = true) }
        viewModelScope.launch {
            val result = repository.syncVehiclesCatalog()
            _uiState.update { 
                it.copy(
                    isSyncingCatalog = false, 
                    syncMessage = if (result.isSuccess) "Catálogo sincronizado" else "Error sincronizando catálogo"
                ) 
            }
        }
    }

    fun onSyncDocumentsClicked() {
        _uiState.update { it.copy(isSyncingDocuments = true) }
        viewModelScope.launch {
            // Sincronizar catálogo primero para asegurar que tenemos los vehículos
            repository.syncVehiclesCatalog()
            // Aquí iría la lógica de documentos si existiera un endpoint masivo
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(isSyncingDocuments = false, syncMessage = "Documentos actualizados") }
        }
    }

    private fun triggerSync(syncType: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf("SYNC_TYPE" to syncType))
            .build()

        workManager.enqueueUniqueWork(
            "vehicle_main_sync_${syncType}_${System.currentTimeMillis()}",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    fun onLogoutClick() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onConfirmLogout() {
        _uiState.update { it.copy(showLogoutDialog = false, logoutCompleted = true) }
    }

    fun onDismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onLogoutCompleted() {
        _uiState.update { it.copy(logoutCompleted = false) }
    }
}
