package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.AppLogger
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject


class TokenAuthenticator @Inject constructor(
    // Usamos 'dagger.Lazy' para evitar una dependencia circular con AuthRepository.
    private val authRepository: dagger.Lazy<AuthRepository>,
    private val appLogger: AppLogger
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Usamos runBlocking porque la interfaz de Authenticator es síncrona.
        return runBlocking {
            appLogger.log("Respuesta 401 recibida. Iniciando autenticador...")
            // Intentamos refrescar el token.
            val tokenRefreshResult = authRepository.get().refreshTokenIfNecessary()

            if (tokenRefreshResult.isSuccess) {
                appLogger.log("Refresh token válido. Reintentando petición original.")
                // Si el refresco fue exitoso, obtenemos el nuevo token de acceso.
                val newAccessToken = tokenRefreshResult.getOrNull()
                // Reintentamos la petición que falló, pero ahora con el nuevo token.
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                // No se reintenta. Si el servidor rechazó el refresh token, AuthRepository ya
                // cerró la sesión; si fue un fallo de red, la sesión se conserva (sin señal en
                // campo el técnico debe poder seguir registrando offline).
                appLogger.log("No se pudo refrescar el token: ${tokenRefreshResult.exceptionOrNull()?.message}. No se reintentará la petición.")
                null
            }
        }
    }
}
