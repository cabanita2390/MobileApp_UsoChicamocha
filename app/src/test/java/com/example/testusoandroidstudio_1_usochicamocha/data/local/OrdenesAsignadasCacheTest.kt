package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OrdenAsignadaCacheEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.SesionActualEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Teléfono compartido: cada operario ve solo sus órdenes y las que son para cualquiera, aunque
 * no haya señal. Una orden general le llega a varios con el mismo id: la de uno no puede pisar
 * la del otro en la caché.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OrdenesAsignadasCacheTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    private fun orden(id: Long, usuarioId: Long, general: Boolean = false) = OrdenAsignadaCacheEntity(
        id = id, usuarioId = usuarioId, consecutivo = "OT-EB-0000$id", prioridad = "ALTA", fechaLimite = "2026-10-25",
        vencida = false, estacionId = 1, estacionNombre = "Duitama", actividadId = 1, actividadNombre = "Corrección",
        instrucciones = "Sellar", hallazgoFecha = "2026-10-12", hallazgoResultado = "CON_HALLAZGOS",
        hallazgoObservaciones = "Fisura", motivoDevolucion = null, fotosHallazgo = "", paraCualquiera = general
    )

    private suspend fun sesion(usuarioId: Long) = db.propietarioColasDao().fijarSesion(SesionActualEntity(usuarioId = usuarioId))

    private suspend fun visibles() = db.ordenAsignadaCacheDao().delUsuarioFlow().first().map { it.id }

    @Test
    fun cadaOperarioVeLasSuyasYLaGeneral_aunqueElOtroSincroniceDespues() = runTest {
        val dao = db.ordenAsignadaCacheDao()
        // Sincroniza Orlando (3): la suya (1) y la general (9).
        dao.reemplazar(3, listOf(orden(1, 3), orden(9, 3, general = true)), emptySet())
        // Luego Ana (4) en el mismo teléfono: la suya (2) y la misma general (9).
        dao.reemplazar(4, listOf(orden(2, 4), orden(9, 4, general = true)), emptySet())

        sesion(3)
        assertEquals(listOf(1L, 9L), visibles())
        sesion(4)
        assertEquals(listOf(2L, 9L), visibles())
        // La orden se busca para el usuario en sesión.
        assertEquals(4L, dao.getById(9)?.usuarioId)
    }

    @Test
    fun unaOrdenYaRegistradaEnLaColaNoVuelveAlSincronizar_yRegistrarlaLaQuitaParaTodos() = runTest {
        val dao = db.ordenAsignadaCacheDao()
        sesion(3)
        dao.reemplazar(3, listOf(orden(9, 3, general = true)), emptySet())
        dao.reemplazar(4, listOf(orden(9, 4, general = true)), emptySet())
        db.ejecucionDao().insertEjecucion(
            EjecucionEntity(
                uuidCliente = "u-1", fecha = "2026-10-18", mesEjecucion = 10, semanaEjecucion = 3, estacionId = 1,
                estacionNombre = "Duitama", tipoMantenimiento = "CORRECTIVO", tipoActividad = "MANTENIMIENTO",
                actividadId = 1, esProgramada = false, resultado = "CONFORME", observaciones = "ok", ordenId = 9
            )
        )
        dao.deleteById(9) // lo que hace el repositorio al guardar el registro

        sesion(4)
        assertEquals(emptyList<Long>(), visibles())
        // El servidor todavía la ve asignada (la visita no ha subido): no debe reaparecer.
        dao.reemplazar(4, listOf(orden(9, 4, general = true)), db.ejecucionDao().ordenesEnCola().toSet())
        assertEquals(emptyList<Long>(), visibles())
    }
}
