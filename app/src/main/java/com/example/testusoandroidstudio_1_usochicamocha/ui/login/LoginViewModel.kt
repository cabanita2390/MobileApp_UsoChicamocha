package com.example.testusoandroidstudio_1_usochicamocha.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.LoginUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.SyncMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.TokenRefreshMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI
data class LoginUiState(
    val username: String = "",
    val password:  String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val isPasswordVisible: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val syncMachinesUseCase: SyncMachinesUseCase,
    private val tokenRefreshMonitor: TokenRefreshMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val loginResult = loginUseCase(uiState.value.username, uiState.value.password)

            loginResult.onSuccess {
                // Start proactive token refresh monitoring
                tokenRefreshMonitor.startMonitoring()

                // Intentamos sincronizar máquinas en segundo plano (no bloquea el login)
                viewModelScope.launch {
                    val syncResult = syncMachinesUseCase()
                    syncResult.onFailure { syncException ->
                        // Log error but don't show to user since login was successful
                    }
                }

                // Si el login es exitoso, procedemos inmediatamente
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            }.onFailure { loginException ->
                _uiState.update { it.copy(isLoading = false, error = loginException.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

}