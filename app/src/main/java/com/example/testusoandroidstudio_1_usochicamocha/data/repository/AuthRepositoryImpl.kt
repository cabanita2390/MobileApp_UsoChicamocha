package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.PropietarioColas
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.LoginRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.RefreshTokenRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.SesionInvalidaException
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.UserSession
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.JwtUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val propietarioColas: PropietarioColas
) : AuthRepository {

    // Evita que múltiples workers/interceptores disparen refrescos de token en paralelo
    // (cada uno saturaba las 5 conexiones concurrentes por host de OkHttp y se agotaba
    // el timeout en cola antes de que la red respondiera).
    private val refreshMutex = Mutex()

    override suspend fun login(user: String, pass: String): Result<UserSession> {
        return try {
            val request = LoginRequest(username = user.trim(), password = pass)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                val accessToken = loginResponse.accessToken
                val refreshToken = loginResponse.refreshToken
                val userId = loginResponse.userId
                val username = loginResponse.username

                tokenManager.saveTokens(accessToken, refreshToken)
                tokenManager.saveUserId(userId)
                propietarioColas.alIniciarSesion(userId.toLong())
                loginResponse.archivosToken?.let { tokenManager.saveArchivosToken(it) }
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
        // Antes de borrar la sesión: lo pendiente queda a nombre de quien lo hizo.
        propietarioColas.alCerrarSesion()
        tokenManager.clearSessionData()
    }

    override suspend fun refreshTokenIfNecessary(): Result<String> = refreshMutex.withLock {
        // Si otro caller ya refrescó mientras esperábamos el lock, no repetimos la llamada de red.
        val currentAccessToken = tokenManager.getAccessToken().first()
        if (currentAccessToken != null && !JwtUtils.isTokenExpired(currentAccessToken, "Access Token")) {
            return@withLock Result.success(currentAccessToken)
        }

        val refreshToken = tokenManager.getRefreshToken().first()
        if (refreshToken == null || JwtUtils.isTokenExpired(refreshToken, "Refresh Token")) {
            logout()
            return@withLock Result.failure(SesionInvalidaException())
        }
        refreshTokenWithRetry(refreshToken, maxAttempts = 2, initialDelayMs = 500)
    }

    override suspend fun refrescarSiVencePronto(margenMs: Long): Result<String> = refreshMutex.withLock {
        val currentAccessToken = tokenManager.getAccessToken().first()
        if (currentAccessToken != null && JwtUtils.msHastaVencer(currentAccessToken) > margenMs) {
            return@withLock Result.success(currentAccessToken)
        }
        val refreshToken = tokenManager.getRefreshToken().first()
        if (refreshToken == null || JwtUtils.isTokenExpired(refreshToken, "Refresh Token")) {
            logout()
            return@withLock Result.failure(SesionInvalidaException())
        }
        refreshTokenWithRetry(refreshToken, maxAttempts = 2, initialDelayMs = 500)
    }

    /**
     * Solo un rechazo del servidor (401/403) cierra la sesión. Un fallo de red o un error 5xx
     * NO: el técnico puede estar sin señal en la estación y su sesión sigue siendo válida
     * mientras el refresh token no venza; antes se cerraba la sesión y el login exigía red.
     */
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
                    response.body()!!.archivosToken?.let { tokenManager.saveArchivosToken(it) }
                    Log.d("AuthRepository", "✅ Refresco exitoso en intento $attempt")
                    return Result.success(newAccessToken)
                }
                if (response.code() == 401 || response.code() == 403) {
                    Log.w("AuthRepository", "⛔ El servidor rechazó el refresh token: ${response.code()}")
                    logout()
                    return Result.failure(SesionInvalidaException("Sesión expirada: ${response.code()}"))
                }
                lastException = Exception("Error del servidor al refrescar: ${response.code()}")
                Log.w("AuthRepository", "⚠️ Respuesta fallida en intento $attempt: ${response.code()}")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                Log.e("AuthRepository", "❌ Excepción en intento $attempt", e)
            }
            if (attempt < maxAttempts) {
                Log.d("AuthRepository", "⏳ Esperando ${retryDelay}ms antes de reintentar...")
                delay(retryDelay)
                retryDelay *= 2
            }
        }

        Log.e("AuthRepository", "❌ Todos los $maxAttempts intentos fallaron; se conserva la sesión")
        return Result.failure(lastException ?: java.io.IOException("Sin conexión para refrescar la sesión"))
    }
}
