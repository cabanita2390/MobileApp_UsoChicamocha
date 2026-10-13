package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.NewAccessTokenResponse
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.SesionInvalidaException
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.SessionStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.ValidateSessionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.NetworkMonitor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response
import java.io.File
import java.net.ConnectException
import java.util.Base64

/**
 * Sin señal en la estación, un refresco fallido por red NO debe cerrar la sesión: antes se
 * borraban los tokens y al reabrir la app el login exigía red, dejando al técnico sin poder
 * registrar. Solo un rechazo del servidor (401/403) o un refresh token vencido la cierran.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthRefreshOfflineTest {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService
    private lateinit var repository: AuthRepositoryImpl

    private fun jwt(expEpochSeconds: Long): String {
        val enc = Base64.getUrlEncoder().withoutPadding()
        val header = enc.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val payload = enc.encodeToString("""{"sub":"op","exp":$expEpochSeconds}""".toByteArray())
        return "$header.$payload.firma"
    }

    private val ahora get() = System.currentTimeMillis() / 1000
    private val accessVencido get() = jwt(ahora - 60)
    private val refreshVigente get() = jwt(ahora + 7 * 24 * 3600)

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.cacheDir, "auth_refresh_offline_${System.nanoTime()}.preferences_pb")
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { file })
        tokenManager = TokenManager(dataStore)
        tokenManager.saveTokens(accessVencido, refreshVigente)
        apiService = mockk()
        repository = AuthRepositoryImpl(apiService, tokenManager)
    }

    @Test
    fun `sin red el refresco falla pero la sesion se conserva`() = runTest {
        coEvery { apiService.refreshToken(any()) } throws ConnectException("Failed to connect")

        val result = repository.refreshTokenIfNecessary()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() !is SesionInvalidaException)
        assertNotNull(tokenManager.getRefreshToken().first())
        assertNotNull(tokenManager.getAccessToken().first())
    }

    @Test
    fun `un error 5xx del servidor tampoco cierra la sesion`() = runTest {
        coEvery { apiService.refreshToken(any()) } returns
            Response.error(503, "caído".toResponseBody(null))

        val result = repository.refreshTokenIfNecessary()

        assertTrue(result.isFailure)
        assertNotNull(tokenManager.getRefreshToken().first())
    }

    @Test
    fun `si el servidor rechaza el refresh token la sesion se cierra sin reintentar`() = runTest {
        coEvery { apiService.refreshToken(any()) } returns
            Response.error(401, "Refresh token inválido o expirado".toResponseBody(null))

        val result = repository.refreshTokenIfNecessary()

        assertTrue(result.exceptionOrNull() is SesionInvalidaException)
        assertNull(tokenManager.getRefreshToken().first())
        coVerify(exactly = 1) { apiService.refreshToken(any()) }
    }

    @Test
    fun `refresh token vencido cierra la sesion sin llamar a la red`() = runTest {
        tokenManager.saveTokens(accessVencido, jwt(ahora - 10))

        val result = repository.refreshTokenIfNecessary()

        assertTrue(result.exceptionOrNull() is SesionInvalidaException)
        assertNull(tokenManager.getRefreshToken().first())
        coVerify(exactly = 0) { apiService.refreshToken(any()) }
    }

    @Test
    fun `refresco exitoso guarda el nuevo access token`() = runTest {
        val nuevo = jwt(ahora + 900)
        coEvery { apiService.refreshToken(any()) } returns Response.success(NewAccessTokenResponse(nuevo))

        val result = repository.refreshTokenIfNecessary()

        assertEquals(nuevo, result.getOrNull())
        assertEquals(nuevo, tokenManager.getAccessToken().first())
    }

    @Test
    fun `al abrir la app con red pero sin llegar al servidor se entra en modo offline`() = runTest {
        coEvery { apiService.refreshToken(any()) } throws ConnectException("Failed to connect")
        val networkMonitor = mockk<NetworkMonitor> { every { networkStatus } returns MutableStateFlow(true) }
        val useCase = ValidateSessionUseCase(tokenManager, repository, networkMonitor)

        assertEquals(SessionStatus.VALID_OFFLINE, useCase())
        assertNotNull(tokenManager.getRefreshToken().first())
    }

    @Test
    fun `al abrir la app con el refresh rechazado por el servidor se pide login`() = runTest {
        coEvery { apiService.refreshToken(any()) } returns
            Response.error(401, "Refresh token inválido o expirado".toResponseBody(null))
        val networkMonitor = mockk<NetworkMonitor> { every { networkStatus } returns MutableStateFlow(true) }
        val useCase = ValidateSessionUseCase(tokenManager, repository, networkMonitor)

        assertEquals(SessionStatus.EXPIRED, useCase())
    }
}
