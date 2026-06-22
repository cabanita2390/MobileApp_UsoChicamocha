package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.LoginRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.RefreshTokenRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.UserSession
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.JwtUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(user: String, pass: String): Result<UserSession> {
        return try {
            val request = LoginRequest(username = user, password = pass)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                val accessToken = loginResponse.accessToken
                val refreshToken = loginResponse.refreshToken
                val userId = loginResponse.userId
                val username = loginResponse.username

                tokenManager.saveTokens(accessToken, refreshToken)
                tokenManager.saveUserId(userId)
                tokenManager.saveUsername(username)
                tokenManager.saveRole(loginResponse.role ?: "OPERARIO")

                // Save inspector info: "FullName (ROLE)" or username as fallback
                val displayInfo = if (!loginResponse.fullName.isNullOrBlank()) {
                    "${loginResponse.fullName} (${loginResponse.role ?: "SIN ROL"})"
                } else {
                    loginResponse.username
                }
                tokenManager.saveInspectorInfo(displayInfo)

                Result.success(UserSession(accessToken = accessToken, refreshToken = refreshToken))
            } else {
                Result.failure(Exception("Usuario o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo conectar al servidor."))
        }
    }
    override suspend fun logout() {
        tokenManager.clearSessionData()
    }

    override suspend fun refreshTokenIfNecessary(): Result<String> {
        val refreshToken = tokenManager.getRefreshToken().first()
        if (refreshToken == null || JwtUtils.isTokenExpired(refreshToken, "Refresh Token")) {
            logout()
            return Result.failure(Exception("Sesión expirada."))
        }
        return refreshTokenWithRetry(refreshToken, maxAttempts = 2, initialDelayMs = 500)
    }

    private suspend fun refreshTokenWithRetry(
        refreshToken: String,
        maxAttempts: Int = 2,
        initialDelayMs: Long = 500
    ): Result<String> {
        var lastException: Throwable? = null
        var retryDelay = initialDelayMs

        for (attempt in 1..maxAttempts) {
            try {
                Log.d("AuthRepository", "🔄 Intento $attempt/$maxAttempts de refresco")
                val request = RefreshTokenRequest(refreshToken = refreshToken)
                val response = apiService.refreshToken(request)

                if (response.isSuccessful && response.body() != null) {
                    val newAccessToken = response.body()!!.accessToken
                    tokenManager.saveTokens(accessToken = newAccessToken, refreshToken = refreshToken)
                    Log.d("AuthRepository", "✅ Refresco exitoso en intento $attempt")
                    return Result.success(newAccessToken)
                } else {
                    lastException = Exception("Refresh token inválido: ${response.code()}")
                    Log.w("AuthRepository", "⚠️ Respuesta fallida en intento $attempt: ${response.code()}")

                    if (attempt < maxAttempts) {
                        Log.d("AuthRepository", "⏳ Esperando ${retryDelay}ms antes de reintentar...")
                        delay(retryDelay)
                        retryDelay *= 2
                    }
                }
            } catch (e: Exception) {
                lastException = e
                Log.e("AuthRepository", "❌ Excepción en intento $attempt", e)

                if (attempt < maxAttempts) {
                    Log.d("AuthRepository", "⏳ Esperando ${retryDelay}ms antes de reintentar...")
                    delay(retryDelay)
                    retryDelay *= 2
                }
            }
        }

        Log.e("AuthRepository", "❌ Todos los $maxAttempts intentos fallaron")
        logout()
        return Result.failure(lastException ?: Exception("Error de red al refrescar token después de $maxAttempts intentos"))
    }
}
