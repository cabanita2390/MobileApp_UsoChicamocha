package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoOilChangeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager

data class VehiculoMainUiState(
    val pendingInspections: List<VehiculoInspectionEntity> = emptyList(),
    val pendingOilChanges: List<VehiculoOilChangeEntity> = emptyList(),
    val totalVehicles: Int = 0,
    val isSyncingInspections: Boolean = false,
    val isSyncingOilChanges: Boolean = false,
    val isSyncingCatalog: Boolean = false,
    val isSyncingDocuments: Boolean = false,
    val syncMessage: String? = null,
    val logoutCompleted: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val userRole: String = "OPERARIO"
)

@HiltViewModel
class VehiculoMainViewModel @Inject constructor(
    private val repository: VehiculoInspectionRepository,
    private val oilChangeRepository: VehiculoOilChangeRepository,
    private val localSyncCoordinator: LocalSyncCoordinator,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoMainUiState())
    val uiState: StateFlow<VehiculoMainUiState> = _uiState.asStateFlow()

    init {
        observePendingInspections()
        observePendingOilChanges()
        observeVehiclesCatalog()
        observeSyncStatuses()
        observeUserRole()
    }

    private fun observeUserRole() {
        tokenManager.getRole().onEach { role ->
            _uiState.update { it.copy(userRole = role ?: "OPERARIO") }
        }.launchIn(viewModelScope)
    }

    private fun observePendingInspections() {
        repository.getAllInspectionsFlow()
            .onEach { list ->
                val pending = list.filter { !it.isSynced }
                _uiState.update { it.copy(pendingInspections = pending) }
            }
            .launchIn(viewModelScope)
    }

    private fun observePendingOilChanges() {
        oilChangeRepository.getAllFlow()
            .onEach { list ->
                val pending = list.filter { !it.isSynced }
                _uiState.update { it.copy(pendingOilChanges = pending) }
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

    fun onSyncOilChangesClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingOilChanges = true, syncMessage = "Sincronizando cambios de aceite...") }
            oilChangeRepository.syncPending()
            _uiState.update { it.copy(isSyncingOilChanges = false) }
        }
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
