package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Auditoría QA 2026-10-13: lo que el técnico A dejaba sin enviar se enviaba con el token de B
 * si B iniciaba sesión en el mismo teléfono, y quedaba a nombre de B.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PropietarioColasTest {

    private lateinit var db: AppDatabase
    private lateinit var tokenManager: TokenManager
    private lateinit var colas: PropietarioColas
    private lateinit var dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val file = File(context.cacheDir, "propietario_${System.nanoTime()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(produceFile = { file })
        tokenManager = TokenManager(dataStore)
        colas = PropietarioColas(db.propietarioColasDao(), tokenManager)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun entrar(id: Int) {
        tokenManager.saveTokens("a", "r"); tokenManager.saveUserId(id)
        colas.alIniciarSesion(id.toLong())
    }

    private suspend fun salir() {
        colas.alCerrarSesion()
        tokenManager.clearSessionData()
    }

    private fun ejecucion(uuid: String) = EjecucionEntity(
        uuidCliente = uuid, fecha = "2026-10-13", mesEjecucion = 10, semanaEjecucion = 2, estacionId = 1,
        estacionNombre = "Cuche", tipoMantenimiento = "PREVENTIVO", tipoActividad = "INSPECCION",
        esProgramada = false, resultado = "CONFORME", observaciones = "ok"
    )

    @Test
    fun `lo pendiente de A no se envia con la sesion de B y espera a que A vuelva`() = runTest {
        entrar(10)
        db.ejecucionDao().insertEjecucion(ejecucion("de-A"))
        salir() // A cierra sesión sin señal

        entrar(20)
        db.ejecucionDao().insertEjecucion(ejecucion("de-B"))
        assertEquals(listOf("de-B"), db.ejecucionDao().getPendingFlow().first().map { it.uuidCliente })
        val cola = db.ejecucionDao().getColaFlow().first().associateBy { it.entity.uuidCliente }
        assertTrue(cola.getValue("de-A").deOtroUsuario)
        assertFalse(cola.getValue("de-B").deOtroUsuario)
        salir()

        entrar(10)
        assertEquals(listOf("de-A"), db.ejecucionDao().getPendingFlow().first().map { it.uuidCliente })
    }

    @Test
    fun `aplica a todas las colas, no solo a subestaciones`() = runTest {
        entrar(10)
        db.vehiculoOilChangeDao().insert(
            VehiculoOilChangeEntity(
                placa = "ABC123", timestamp = 0L, oilType = "MOTOR", oilBrandId = 1, oilBrandName = "X",
                quantity = 4.0, kmAtChange = 1000, intervalKm = 5000
            )
        )
        salir()
        entrar(20)
        assertTrue(db.vehiculoOilChangeDao().getPending().isEmpty())
        salir()
        entrar(10)
        assertEquals(1, db.vehiculoOilChangeDao().getPending().size)
    }

    @Test
    fun `los registros de antes del cambio siguen saliendo con la sesion actual`() = runTest {
        db.ejecucionDao().insertEjecucion(ejecucion("legado")) // propietarioId null, sin sesion_actual
        colas.sincronizarConSesionGuardada()
        entrar(10)
        assertEquals(listOf("legado"), db.ejecucionDao().getPendingFlow().first().map { it.uuidCliente })
    }

    @Test
    fun `cerrar sesion borra solo la sesion, no el resto del DataStore`() = runTest {
        val obs = stringPreferencesKey("subestacion_obs_frecuentes")
        dataStore.edit { it[obs] = "{}" }
        entrar(10)
        tokenManager.clearSessionData()
        assertNull(tokenManager.getAccessToken().first())
        assertEquals("{}", dataStore.data.first()[obs])
    }
}
