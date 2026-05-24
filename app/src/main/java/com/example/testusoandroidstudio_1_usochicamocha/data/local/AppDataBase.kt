package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.LogDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MachineDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MaintenanceDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.DocumentoMotoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.InspeccionMotoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MotoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.OilDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.UbicacionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoInspectionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.DocumentoVehiculoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoOilChangeDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.InspeccionMotoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LogEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MaintenanceEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.UbicacionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity

@Database(
    entities = [
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LogEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MaintenanceEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.UbicacionEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.InspeccionMotoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity::class
    ],
    version = 29,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        /** Migración 23 → 24: agrega campos mecánicos de inspección de moto */
        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pending_inspecciones_moto ADD COLUMN checkNivelAceite TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE pending_inspecciones_moto ADD COLUMN checkEstadoLlantas TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE pending_inspecciones_moto ADD COLUMN checkEstadoLuces TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Migración 24 → 25: agrega placa y tipo para visualización en hub */
        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pending_inspecciones_moto ADD COLUMN placaVehiculo TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE pending_inspecciones_moto ADD COLUMN tipoVehiculo TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Migración 25 → 26: agrega conscienteResponsabilidad y aprobadoRuta */
        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pending_inspecciones_moto ADD COLUMN conscienteResponsabilidad TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE pending_inspecciones_moto ADD COLUMN aprobadoRuta TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Migración 27 → 28: agrega ubicación base a la tabla de motos */
        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE motos ADD COLUMN idUbicacionBase INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE motos ADD COLUMN ubicacionBase TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Migración 28 → 29: crea tabla de cambios de aceite vehicular independiente */
        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vehiculo_oil_changes` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `placa` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `oilType` TEXT NOT NULL,
                        `oilBrandId` INTEGER NOT NULL,
                        `oilBrandName` TEXT NOT NULL,
                        `quantity` REAL,
                        `kmAtChange` INTEGER NOT NULL,
                        `intervalKm` INTEGER NOT NULL,
                        `airFilterChanged` INTEGER NOT NULL DEFAULT 0,
                        `isSynced` INTEGER NOT NULL DEFAULT 0,
                        `isSyncing` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        /** Migración 26 → 27: URLs documentos + opcional cambio aceite en inspección vehículo */
        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN urlImagenSoat TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN urlImagenTecno TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN urlImagenLicencia TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN urlImagenExtintor TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN registrarCambioAceite INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN oilType TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN oilBrandId INTEGER")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN oilIntervalKm INTEGER")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN oilQuantity REAL")
                database.execSQL("ALTER TABLE vehiculo_inspections ADD COLUMN oilAirFilterChanged INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
    abstract fun formDao(): FormDao
    abstract fun machineDao(): MachineDao
    abstract fun logDao(): LogDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun oilDao(): OilDao
    abstract fun imageDao(): ImageDao
    abstract fun motoDao(): MotoDao
    abstract fun ubicacionDao(): UbicacionDao
    abstract fun inspeccionMotoDao(): InspeccionMotoDao
    abstract fun documentoMotoDao(): DocumentoMotoDao
    abstract fun vehiculoInspectionDao(): VehiculoInspectionDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun documentoVehiculoDao(): DocumentoVehiculoDao
    abstract fun vehiculoOilChangeDao(): VehiculoOilChangeDao
}
