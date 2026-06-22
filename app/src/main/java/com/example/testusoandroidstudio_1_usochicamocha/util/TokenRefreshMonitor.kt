package com.example.testusoandroidstudio_1_usochicamocha.util

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshMonitor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val appLogger: AppLogger
) {
    companion object {
        private const val REFRESH_INTERVAL_MS = 60_000L  // Check every 60 seconds
        private const val REFRESH_THRESHOLD_MS = 5 * 60 * 1000L  // Refresh if expires in 5 minutes
        private const val TAG = "TokenRefreshMonitor"
    }

    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startMonitoring() {
        if (monitorJob?.isActive == true) {
            appLogger.log("$TAG: Monitor already running, skipping start")
            return
        }

        appLogger.log("$TAG: Starting token refresh monitor")
        monitorJob = scope.launch {
            while (true) {
                try {
                    val accessToken = tokenManager.getAccessToken().first()

                    if (accessToken != null && !JwtUtils.isTokenExpired(accessToken, "Access Token")) {
                        val expiresInMs = getTokenExpirationTimeRemaining(accessToken)

                        if (expiresInMs > 0 && expiresInMs < REFRESH_THRESHOLD_MS) {
                            appLogger.log("$TAG: Token expires in ${expiresInMs / 1000}s, refreshing proactively...")
                            val refreshResult = authRepository.refreshTokenIfNecessary()

                            if (refreshResult.isSuccess) {
                                appLogger.log("$TAG: Token refreshed successfully")
                            } else {
                                appLogger.log("$TAG: Token refresh failed: ${refreshResult.exceptionOrNull()?.message}")
                            }
                        }
                    }

                    delay(REFRESH_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in token refresh monitor", e)
                    delay(REFRESH_INTERVAL_MS)
                }
            }
        }
    }

    fun stopMonitoring() {
        if (monitorJob?.isActive == true) {
            appLogger.log("$TAG: Stopping token refresh monitor")
            monitorJob?.cancel()
            monitorJob = null
        }
    }

    private fun getTokenExpirationTimeRemaining(token: String): Long {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return -1

            val payloadBytes = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE)
            val payloadJson = String(payloadBytes, Charsets.UTF_8)
            val payload = org.json.JSONObject(payloadJson)

            val exp = payload.optLong("exp")
            if (exp == 0L) return -1

            val expirationInMillis = exp * 1000
            val currentTimeInMillis = System.currentTimeMillis()

            expirationInMillis - currentTimeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating token expiration time", e)
            -1
        }
    }
}
