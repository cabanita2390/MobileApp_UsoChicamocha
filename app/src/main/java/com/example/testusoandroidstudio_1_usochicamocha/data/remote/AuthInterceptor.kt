package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.SesionInvalidaException
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.AppLogger
import com.example.testusoandroidstudio_1_usochicamocha.util.JwtUtils
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: Lazy<AuthRepository>,
    private val appLogger: AppLogger
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()

        // Excluimos tanto el endpoint de login como el de refresh token de este interceptor.
        if (requestUrl.contains("v1/auth/login") || requestUrl.contains("v1/auth/token/refresh")) {
            appLogger.log("Petición pública (login/refresh), omitiendo interceptor de Auth.")
            return chain.proceed(originalRequest)
        }

        appLogger.log("Verificando tokens para petición protegida")
        val accessToken = runBlocking { tokenManager.getAccessToken().first() }

        // Proactive token refresh logic
        if (accessToken != null && JwtUtils.isTokenExpired(accessToken, "Access Token")) {
            appLogger.log("Access token vencido. Verificando refresh token...")
            val refreshToken = runBlocking { tokenManager.getRefreshToken().first() }

            // --- NUEVA VALIDACIÓN ---
            // Verificamos si el refresh token es nulo o si ya está vencido POR FECHA.
            if (refreshToken == null || JwtUtils.isTokenExpired(refreshToken, "Refresh Token")) {
                // Si está vencido, forzamos logout y detenemos la petición original.
                appLogger.log("Refresh token vencido o nulo. Forzando logout sin llamada de red.")
                runBlocking { authRepository.get().logout() }
                // Devolvemos una respuesta de error para detener la cadena de peticiones.
                return Response.Builder()
                    .request(originalRequest)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(401)
                    .message("Refresh token expired")
                    .body("{\"error\":\"Refresh token expired\"}".toResponseBody(null))
                    .build()
            }

            // Si llegamos aquí, el refresh token es válido. Procedemos a usarlo.
            appLogger.log("Refresh token válido. Intentando obtener nuevo access token.")
            val refreshResult = runBlocking { authRepository.get().refreshTokenIfNecessary() }
            if (refreshResult.isSuccess) {
                appLogger.log("Access token obtenido")
                val newAccessToken = runBlocking { tokenManager.getAccessToken().first() }
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                appLogger.log("Realizando petición (${originalRequest.url.encodedPath}) con nuevo token.")
                return chain.proceed(newRequest)
            } else {
                val error = refreshResult.exceptionOrNull()
                if (error !is SesionInvalidaException) {
                    // Sin señal o backend caído: la sesión sigue siendo válida. La petición
                    // falla como cualquier otra sin red y el registro queda en la cola local.
                    appLogger.log("No se pudo refrescar el token (sin conexión). Se conserva la sesión.")
                    throw IOException("Sin conexión para refrescar la sesión", error)
                }
                // El servidor rechazó el refresh token: AuthRepository ya cerró la sesión.
                appLogger.log("El servidor rechazó el refresh token. Sesión cerrada.")
                return Response.Builder()
                    .request(originalRequest)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(401)
                    .message("Token refresh failed")
                    .body("{\"error\":\"Token refresh failed\"}".toResponseBody(null))
                    .build()
            }
        }

        // Si el access token no está vencido, simplemente lo añadimos y continuamos.
        val requestBuilder = originalRequest.newBuilder()
        if (accessToken != null) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }
        appLogger.log("Realizando petición (${originalRequest.url.encodedPath})")
        return chain.proceed(requestBuilder.build())
    }
}

