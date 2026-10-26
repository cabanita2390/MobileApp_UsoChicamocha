package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
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
}
