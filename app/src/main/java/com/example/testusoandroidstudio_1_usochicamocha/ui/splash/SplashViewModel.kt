package com.example.testusoandroidstudio_1_usochicamocha.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.SessionStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.ValidateSessionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.TokenRefreshMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val tokenRefreshMonitor: TokenRefreshMonitor
) : ViewModel() {
    private val _destination = MutableStateFlow<String?>(null)
    val destination = _destination.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val sessionStatus = withTimeoutOrNull(15000) { // 15 segundos
                validateSessionUseCase()
            }

            if (sessionStatus == null) {
                _destination.value = "login"
                return@launch
            }

            when (sessionStatus) {
                SessionStatus.VALID, SessionStatus.REFRESHED, SessionStatus.VALID_OFFLINE -> {
                    // Start token refresh monitoring for valid sessions
                    tokenRefreshMonitor.startMonitoring()
                    _destination.value = "home"
                }
                SessionStatus.EXPIRED -> {
                    _destination.value = "login"
                }
            }
        }
    }
}