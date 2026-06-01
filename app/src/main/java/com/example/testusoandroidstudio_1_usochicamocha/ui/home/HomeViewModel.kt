package com.example.testusoandroidstudio_1_usochicamocha.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val showLogoutDialog: Boolean = false,
    val logoutCompleted: Boolean = false,
    val userRole: String = "OPERARIO"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val localSyncCoordinator: LocalSyncCoordinator,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenManager.getRole().collect { role ->
                _uiState.update { it.copy(userRole = role ?: "OPERARIO") }
            }
        }
        triggerAutoSync()
    }

    private fun triggerAutoSync() {
        if (localSyncCoordinator.isAutoSyncDone()) return

        viewModelScope.launch {
            localSyncCoordinator.setAutoSyncDone(true)
            
            // Sincronizar Placas de Motos
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MOTOS_ONLY)
            )
            // Sincronizar Unidades (Ubicaciones)
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.UBICACIONES_ONLY)
            )
            // Sincronizar Catálogo de Vehículos
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_CATALOG)
            )
            // Sincronizar Documentos de Motos
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.DOCUMENTS_ONLY)
            )
            // Sincronizar Documentos de Vehículos
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_DOCUMENTS)
            )
        }
    }

    fun onLogoutClick() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onDismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onConfirmLogout() {
        viewModelScope.launch {
            logoutUseCase()
            localSyncCoordinator.setAutoSyncDone(false) // Reset para la próxima sesión
            _uiState.update { it.copy(showLogoutDialog = false, logoutCompleted = true) }
        }
    }

    fun onLogoutCompleted() {
        _uiState.update { it.copy(logoutCompleted = false) }
    }
}
