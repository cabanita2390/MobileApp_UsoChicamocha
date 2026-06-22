package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.TokenRefreshMonitor
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenRefreshMonitor: TokenRefreshMonitor
) {
    suspend operator fun invoke() {
        tokenRefreshMonitor.stopMonitoring()
        authRepository.logout()
    }
}
