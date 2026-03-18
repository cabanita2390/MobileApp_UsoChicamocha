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
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator

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
    private val localSyncCoordinator: LocalSyncCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoMainUiState())
    val uiState: StateFlow<VehiculoMainUiState> = _uiState.asStateFlow()

    init {
        observePendingInspections()
        observeVehiclesCatalog()
        observeSyncStatuses()
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

    private fun observeSyncStatuses() {
        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_CATALOG)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingCatalog = isRunning) }
        }.launchIn(viewModelScope)

        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_ONLY)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingInspections = isRunning) }
        }.launchIn(viewModelScope)

        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_DOCUMENTS)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingDocuments = isRunning) }
        }.launchIn(viewModelScope)
    }

    fun onSyncInspectionsClicked() {
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_ONLY)
            )
            _uiState.update { it.copy(syncMessage = "Sincronizando inspecciones...") }
        }
    }

    fun onSyncCatalogClicked() {
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_CATALOG)
            )
            _uiState.update { it.copy(syncMessage = "Sincronizando catálogo...") }
        }
    }

    fun onSyncDocumentsClicked() {
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_DOCUMENTS)
            )
            _uiState.update { it.copy(syncMessage = "Sincronizando documentos...") }
        }
    }

    /* triggerSync movido a LocalSyncCoordinator */

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
