package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Migraciones contra los esquemas exportados en app/schemas. La base vieja se arma con el
 * createSql del JSON de esa versión; al abrirla, Room corre la migración y valida que cada tabla
 * quede idéntica al esquema actual (si no, lanza IllegalStateException con la diferencia).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val nombreDb = "migracion_test.db"

    @Before
    @After
    fun limpiar() {
        context.deleteDatabase(nombreDb)
    }

    private fun crearBaseEnVersion(version: Int): SQLiteDatabase {
        val esquema = File("schemas/${AppDatabase::class.java.name}/$version.json")
        val db = JSONObject(esquema.readText()).getJSONObject("database")
        val sqlite = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(nombreDb).apply { parentFile?.mkdirs() }, null)
        val entidades = db.getJSONArray("entities")
        for (i in 0 until entidades.length()) {
            val e = entidades.getJSONObject(i)
            val tabla = e.getString("tableName")
            sqlite.execSQL(e.getString("createSql").replace("\${TABLE_NAME}", tabla))
            e.optJSONArray("indices")?.let { indices ->
                for (j in 0 until indices.length()) {
                    sqlite.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", tabla))
                }
            }
        }
        val setup = db.getJSONArray("setupQueries")
        for (i in 0 until setup.length()) sqlite.execSQL(setup.getString(i))
        sqlite.version = version
        return sqlite
    }

    @Test
    fun migracion_53_a_54_conserva_las_inspecciones_pendientes_sin_lecturas() {
        crearBaseEnVersion(53).apply {
            execSQL(
                "INSERT INTO pending_inspecciones_moto (uuid, timestamp, idVehiculo, kilometrajeReportado, estadoVehiculo, " +
                    "observacionesFinales, checkSoat, checkTecno, checkLicencia, checkExtintor, idUbicacion, isSynced, isSyncing, " +
                    "fechaSoat, fechaTecno, fechaLicencia) " +
                    "VALUES ('u-1', 1, 4, 150, 'BUENO', '', 'Vigente', 'Vigente', 'Vigente', 'N/A', 2, 0, 0, '', '', '')"
            )
            close()
        }

        val db = Room.databaseBuilder(context, AppDatabase::class.java, nombreDb)
            .addMigrations(AppDatabase.MIGRATION_53_54)
            .allowMainThreadQueries()
            .build()
        try {
            val sqlite = db.openHelper.writableDatabase // aquí corre la migración y la validación del esquema
            assertEquals(54, sqlite.version)
            sqlite.query("SELECT presionesLlantasJson FROM pending_inspecciones_moto WHERE uuid = 'u-1'").use { c ->
                c.moveToFirst()
                assertEquals("", c.getString(0))
            }
            sqlite.query("SELECT COUNT(*) FROM llanta_config_cache").use { c ->
                c.moveToFirst()
                assertEquals(0, c.getInt(0))
            }
        } finally {
            db.close()
        }
    }

    @Test
    fun migracion_54_a_55_deja_lo_pendiente_en_civil_y_crea_el_cache_de_conjuntos_y_elementos() {
        crearBaseEnVersion(54).apply {
            execSQL(
                "INSERT INTO pending_mant_ejecucion (uuidCliente, fecha, mesEjecucion, semanaEjecucion, estacionId, estacionNombre, " +
                    "tipoMantenimiento, tipoActividad, esProgramada, resultado, observaciones, isSynced, isSyncing, syncFallido) " +
                    "VALUES ('e-1', '2026-10-27', 10, 4, 1, 'Surba', 'PREVENTIVO', 'INSPECCION', 0, 'CONFORME', 'ok', 0, 0, 0)"
            )
            execSQL("INSERT INTO mant_actividad_cache (id, nombre) VALUES (11, 'Pintura muros')")
            close()
        }

        val db = Room.databaseBuilder(context, AppDatabase::class.java, nombreDb)
            .addMigrations(AppDatabase.MIGRATION_54_55)
            .allowMainThreadQueries()
            .build()
        try {
            val sqlite = db.openHelper.writableDatabase // aquí corre la migración y la validación del esquema
            assertEquals(55, sqlite.version)
            sqlite.query("SELECT disciplina, conjuntoIds, elementoIds, conjuntoOtro FROM pending_mant_ejecucion WHERE uuidCliente = 'e-1'").use { c ->
                c.moveToFirst()
                assertEquals("CIVIL", c.getString(0))
                assertEquals("", c.getString(1))
                assertEquals("", c.getString(2))
                assertTrue(c.isNull(3))
            }
            sqlite.query("SELECT disciplina FROM mant_actividad_cache WHERE id = 11").use { c ->
                c.moveToFirst()
                assertEquals("CIVIL", c.getString(0))
            }
            sqlite.query("SELECT COUNT(*) FROM mant_intervenido_cache").use { c ->
                c.moveToFirst()
                assertEquals(0, c.getInt(0))
            }
        } finally {
            db.close()
        }
    }

    @Test
    fun migracion_55_a_56_agrega_el_icono_de_los_conjuntos_sin_perder_el_cache() {
        crearBaseEnVersion(55).apply {
            execSQL("INSERT INTO mant_intervenido_cache (tipo, id, nombre, disciplina) VALUES ('CONJUNTO', 101, 'Motores', 'ELECTRICO')")
            close()
        }

        val db = Room.databaseBuilder(context, AppDatabase::class.java, nombreDb)
            .addMigrations(AppDatabase.MIGRATION_55_56)
            .allowMainThreadQueries()
            .build()
        try {
            val sqlite = db.openHelper.writableDatabase // aquí corre la migración y la validación del esquema
            assertEquals(56, sqlite.version)
            sqlite.query("SELECT nombre, icono FROM mant_intervenido_cache WHERE id = 101").use { c ->
                c.moveToFirst()
                assertEquals("Motores", c.getString(0))
                assertTrue(c.isNull(1))
            }
        } finally {
            db.close()
        }
    }
}
