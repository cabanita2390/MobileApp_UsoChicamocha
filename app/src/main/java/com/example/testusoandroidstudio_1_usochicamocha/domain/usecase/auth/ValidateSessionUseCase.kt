package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.JwtUtils
import com.example.testusoandroidstudio_1_usochicamocha.util.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class SessionStatus {
    object VALID : SessionStatus()
    object REFRESHED : SessionStatus()
    object VALID_OFFLINE : SessionStatus()
    object EXPIRED : SessionStatus()
}


class ValidateSessionUseCase @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) {
    private val TAG = "ValidateSession"
    companion object {
        private const val MAX_REFRESH_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY_MS = 1000L
    }

    suspend operator fun invoke(): SessionStatus {
        val accessToken = tokenManager.getAccessToken().first()
        val refreshToken = tokenManager.getRefreshToken().first()

        Log.d(TAG, "Iniciando validación de sesión...")
        val isAccessTokenExpired = accessToken == null || JwtUtils.isTokenExpired(accessToken, "Access Token")
        val isRefreshTokenExpired = refreshToken == null || JwtUtils.isTokenExpired(refreshToken, "Refresh Token")

        if (isAccessTokenExpired) {
            // Si el Access Token ha expirado, comprobamos el Refresh Token.
            if (isRefreshTokenExpired) {
                Log.d(TAG, "Decisión: Sesión EXPIRADA (ambos tokens inválidos).")
                return SessionStatus.EXPIRED
            } else {
                // El Refresh Token es válido, ahora comprobamos si hay internet.
                val hasInternet = networkMonitor.networkStatus.first()
                if (hasInternet) {
                    Log.d(TAG, "Intentando refrescar token con reintentos inteligentes...")
                    val refreshResult = refreshTokenWithRetry()
                    return if (refreshResult.isSuccess) {
                        Log.d(TAG, "Token refrescado con éxito.")
                        SessionStatus.REFRESHED
                    } else {
                        Log.d(TAG, "Fallo al refrescar token después de $MAX_REFRESH_ATTEMPTS intentos: ${refreshResult.exceptionOrNull()?.message}")
                        SessionStatus.EXPIRED
                    }
                } else {
                    // Si no hay internet pero el refresh token era válido, permitimos el acceso offline.
                    Log.d(TAG, "Acceso permitido en modo offline (refresh token válido).")
                    return SessionStatus.VALID_OFFLINE
                }
            }
        } else {
            // Si el Access Token no ha expirado, la sesión es válida.
            Log.d(TAG, "Decisión: Sesión VÁLIDA (access token vigente).")
            return SessionStatus.VALID
        }
    }

    private suspend fun refreshTokenWithRetry(): Result<String> {
        var lastException: Throwable? = null
        var retryDelay = INITIAL_RETRY_DELAY_MS

        for (attempt in 1..MAX_REFRESH_ATTEMPTS) {
            try {
                Log.d(TAG, "🔄 Intento $attempt/$MAX_REFRESH_ATTEMPTS de refresco de token")
                val result = authRepository.refreshTokenIfNecessary()

                if (result.isSuccess) {
                    Log.d(TAG, "✅ Refresco de token exitoso en intento $attempt")
                    return result
                } else {
                    lastException = result.exceptionOrNull()
                    Log.w(TAG, "⚠️ Refresco falló en intento $attempt: ${lastException?.message}")

                    // Si es el último intento, no esperar
                    if (attempt < MAX_REFRESH_ATTEMPTS) {
                        Log.d(TAG, "⏳ Esperando ${retryDelay}ms antes de reintentar...")
                        delay(retryDelay)
                        // Backoff exponencial: 1s, 2s, 4s
                        retryDelay *= 2
                    }
                }
            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "❌ Excepción en intento $attempt de refresco de token", e)

                if (attempt < MAX_REFRESH_ATTEMPTS) {
                    Log.d(TAG, "⏳ Esperando ${retryDelay}ms antes de reintentar...")
                    delay(retryDelay)
                    retryDelay *= 2
                }
            }
        }

        Log.e(TAG, "❌ Todos los $MAX_REFRESH_ATTEMPTS intentos de refresco fallaron")
        return Result.failure(lastException ?: Exception("Token refresh failed after $MAX_REFRESH_ATTEMPTS attempts"))
    }
}