package com.example.testusoandroidstudio_1_usochicamocha.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val logoutCompleted: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onLogoutClick() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onDismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onConfirmLogout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(showLogoutDialog = false, logoutCompleted = true) }
        }
    }

    fun onLogoutCompleted() {
        _uiState.update { it.copy(logoutCompleted = false) }
    }
}
