package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.local.converter.DateTimeConverters
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.LogDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MachineDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MachineOilChangeDao
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
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.UbicacionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MotoOilChangeDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.EjecucionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.EstacionCacheDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ActividadCacheDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EstacionCacheEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ActividadCacheEntity

@TypeConverters(DateTimeConverters::class)
@Database(
    entities = [
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LogEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineOilChangeEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.UbicacionEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.InspeccionMotoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EstacionCacheEntity::class,
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ActividadCacheEntity::class
    ],
    version = 45,
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
                        `airFilterChanged` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /** Migración 29 → 30: crea tabla de registros de combustible offline (esquema inicial) */
        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fuel_logs_local` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `remoteId` INTEGER,
                        `syncId` TEXT NOT NULL,
                        `assetType` TEXT NOT NULL,
                        `assetId` INTEGER NOT NULL,
                        `assetPlate` TEXT,
                        `fuelDate` TEXT NOT NULL,
                        `odometerKm` REAL,
                        `hourMeter` REAL,
                        `litersLoaded` REAL NOT NULL,
                        `costPerLiter` REAL NOT NULL,
                        `fuelType` TEXT NOT NULL,
                        `serviceStation` TEXT,
                        `isFullTank` INTEGER NOT NULL,
                        `notes` TEXT,
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /** Migración 30 → 31: reestructura fuel_logs_local con todos los campos del módulo de combustibles */
        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fuel_logs_local_new` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `remoteId` INTEGER,
                        `syncId` TEXT NOT NULL,
                        `assetType` TEXT NOT NULL,
                        `assetId` INTEGER NOT NULL,
                        `assetPlate` TEXT,
                        `fuelDateTime` TEXT NOT NULL,
                        `odometerKm` REAL,
                        `hourMeter` REAL,
                        `quantity` REAL NOT NULL,
                        `quantityUnit` TEXT NOT NULL,
                        `quantityLiters` REAL NOT NULL,
                        `pricePerUnit` REAL NOT NULL,
                        `totalCostCalculated` REAL NOT NULL,
                        `totalCostActual` REAL,
                        `totalCostMismatch` INTEGER NOT NULL,
                        `fuelType` TEXT NOT NULL,
                        `serviceStation` TEXT,
                        `isFullTank` INTEGER NOT NULL,
                        `discountAmount` REAL,
                        `invoicePhotoPath` TEXT,
                        `invoicePhotoUrl` TEXT,
                        `invoiceStatus` TEXT NOT NULL,
                        `voucherNumber` TEXT,
                        `notes` TEXT,
                        `efficiencyValue` REAL,
                        `efficiencyUnit` TEXT,
                        `isAnomaly` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO `fuel_logs_local_new`
                        (`localId`, `remoteId`, `syncId`, `assetType`, `assetId`, `assetPlate`,
                         `fuelDateTime`, `odometerKm`, `hourMeter`, `quantity`, `quantityUnit`, `quantityLiters`,
                         `pricePerUnit`, `totalCostCalculated`, `fuelType`, `serviceStation`, `isFullTank`,
                         `notes`, `isSynced`, `isSyncing`, `createdAt`,
                         `totalCostMismatch`, `isAnomaly`, `invoiceStatus`)
                    SELECT `localId`, `remoteId`, `syncId`, `assetType`, `assetId`, `assetPlate`,
                           `fuelDate`, `odometerKm`, `hourMeter`, `litersLoaded`, 'LITERS', `litersLoaded`,
                           `costPerLiter`, (`litersLoaded` * `costPerLiter`), `fuelType`, `serviceStation`, `isFullTank`,
                           `notes`, `isSynced`, `isSyncing`, `createdAt`,
                           0, 0, 'PENDING_REVIEW'
                    FROM `fuel_logs_local`
                """.trimIndent())

                database.execSQL("DROP TABLE `fuel_logs_local`")
                database.execSQL("ALTER TABLE `fuel_logs_local_new` RENAME TO `fuel_logs_local`")
            }
        }

        /** Migración 31 → 32: crea tabla de estaciones de combustible (catálogo del admin) */
        val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fuel_stations_local` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `name` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        /** Migración 32 → 33: elimina campo isFullTank de fuel_logs_local */
        val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fuel_logs_local_new` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `remoteId` INTEGER,
                        `syncId` TEXT NOT NULL,
                        `assetType` TEXT NOT NULL,
                        `assetId` INTEGER NOT NULL,
                        `assetPlate` TEXT,
                        `fuelDateTime` TEXT NOT NULL,
                        `odometerKm` REAL,
                        `hourMeter` REAL,
                        `quantity` REAL NOT NULL,
                        `quantityUnit` TEXT NOT NULL,
                        `quantityLiters` REAL NOT NULL,
                        `pricePerUnit` REAL NOT NULL,
                        `totalCostCalculated` REAL NOT NULL,
                        `totalCostActual` REAL,
                        `totalCostMismatch` INTEGER NOT NULL,
                        `fuelType` TEXT NOT NULL,
                        `serviceStation` TEXT,
                        `discountAmount` REAL,
                        `invoicePhotoPath` TEXT,
                        `invoicePhotoUrl` TEXT,
                        `invoiceStatus` TEXT NOT NULL,
                        `voucherNumber` TEXT,
                        `notes` TEXT,
                        `efficiencyValue` REAL,
                        `efficiencyUnit` TEXT,
                        `isAnomaly` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO `fuel_logs_local_new`
                        (`localId`, `remoteId`, `syncId`, `assetType`, `assetId`, `assetPlate`,
                         `fuelDateTime`, `odometerKm`, `hourMeter`, `quantity`, `quantityUnit`, `quantityLiters`,
                         `pricePerUnit`, `totalCostCalculated`, `totalCostActual`, `totalCostMismatch`,
                         `fuelType`, `serviceStation`, `discountAmount`, `invoicePhotoPath`, `invoicePhotoUrl`,
                         `invoiceStatus`, `voucherNumber`, `notes`, `efficiencyValue`, `efficiencyUnit`,
                         `isAnomaly`, `isSynced`, `isSyncing`, `createdAt`)
                    SELECT `localId`, `remoteId`, `syncId`, `assetType`, `assetId`, `assetPlate`,
                           `fuelDateTime`, `odometerKm`, `hourMeter`, `quantity`, `quantityUnit`, `quantityLiters`,
                           `pricePerUnit`, `totalCostCalculated`, `totalCostActual`, `totalCostMismatch`,
                           `fuelType`, `serviceStation`, `discountAmount`, `invoicePhotoPath`, `invoicePhotoUrl`,
                           `invoiceStatus`, `voucherNumber`, `notes`, `efficiencyValue`, `efficiencyUnit`,
                           `isAnomaly`, `isSynced`, `isSyncing`, `createdAt`
                    FROM `fuel_logs_local`
                """.trimIndent())

                database.execSQL("DROP TABLE `fuel_logs_local`")
                database.execSQL("ALTER TABLE `fuel_logs_local_new` RENAME TO `fuel_logs_local`")
            }
        }

        val MIGRATION_33_34 = object : Migration(33, 34) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE vehiculo_oil_changes ADD COLUMN assetType TEXT NOT NULL DEFAULT 'VEHICLE'")
            }
        }

        val MIGRATION_34_35 = object : Migration(34, 35) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla vehicle_oil_changes_improved
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vehicle_oil_changes_improved` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `placa` TEXT NOT NULL,
                        `oil_type` TEXT NOT NULL,
                        `brand_name` TEXT,
                        `quantity` REAL NOT NULL,
                        `km_at_change` INTEGER NOT NULL,
                        `next_change_km` INTEGER NOT NULL,
                        `percentage_used` INTEGER NOT NULL,
                        `air_filter_changed` INTEGER NOT NULL,
                        `date_stamp` TEXT NOT NULL,
                        `oil_durability` TEXT NOT NULL,
                        `requirement_id` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `is_synced` INTEGER NOT NULL,
                        `sync_error` TEXT
                    )
                """.trimIndent())

                // Crear tabla machine_oil_changes_improved
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `machine_oil_changes_improved` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `machine_id` INTEGER NOT NULL,
                        `oil_type` TEXT NOT NULL,
                        `brand_name` TEXT,
                        `quantity` REAL NOT NULL,
                        `hour_stamp` INTEGER NOT NULL,
                        `next_change_hours` INTEGER NOT NULL,
                        `percentage_used` INTEGER NOT NULL,
                        `motor_oil` INTEGER NOT NULL,
                        `hydraulic_oil` INTEGER NOT NULL,
                        `date_stamp` TEXT NOT NULL,
                        `oil_durability` TEXT NOT NULL,
                        `requirement_id` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `is_synced` INTEGER NOT NULL,
                        `sync_error` TEXT
                    )
                """.trimIndent())

                // Crear tabla oil_analysis_sos
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `oil_analysis_sos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `machine_id` INTEGER NOT NULL,
                        `machine_name` TEXT NOT NULL,
                        `analysis_date` TEXT NOT NULL,
                        `oil_type` TEXT NOT NULL,
                        `hours_at_analysis` INTEGER NOT NULL,
                        `next_change_hours` INTEGER NOT NULL,
                        `sos_report_url` TEXT NOT NULL,
                        `approved_by_mechanic` TEXT NOT NULL,
                        `observations` TEXT,
                        `is_approved` INTEGER NOT NULL,
                        `extended_hours` INTEGER NOT NULL,
                        `authorizes_extension` INTEGER NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `local_created_at` INTEGER NOT NULL,
                        `is_synced` INTEGER NOT NULL,
                        `sync_error` TEXT
                    )
                """.trimIndent())

                // Crear índices
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_vehicle_oil_changes_improved_placa ON vehicle_oil_changes_improved(placa)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_machine_oil_changes_improved_machine_id ON machine_oil_changes_improved(machine_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_oil_analysis_sos_machine_id ON oil_analysis_sos(machine_id)")
            }
        }

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

        val MIGRATION_35_36 = object : Migration(35, 36) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE motos ADD COLUMN marca TEXT DEFAULT ''")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `moto_oil_changes` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `placa` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `oilType` TEXT NOT NULL,
                        `oilBrandId` INTEGER NOT NULL,
                        `oilBrandName` TEXT NOT NULL,
                        `quantity` REAL,
                        `kmAtChange` INTEGER NOT NULL,
                        `intervalKm` INTEGER NOT NULL,
                        `airFilterChanged` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /**
         * Migración 36 → 37: reconstruye pending_inspecciones_moto para forzar
         * DEFAULT '' en las columnas agregadas por MIGRATION_23_24/24_25/25_26.
         *
         * Dispositivos que ya ejecutaron esas migraciones antes de que se les
         * agregara DEFAULT '' quedaron con esas columnas sin default en SQLite,
         * lo que no coincide con lo que @ColumnInfo(defaultValue = "") declara
         * en InspeccionMotoEntity y hace fallar la validación de Room al migrar
         * (IllegalStateException: "Migration didn't properly handle").
         * SQLite no soporta ALTER COLUMN, así que se recrea la tabla completa.
         */
        val MIGRATION_36_37 = object : Migration(36, 37) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pending_inspecciones_moto_new` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `serverId` INTEGER,
                        `uuid` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `idVehiculo` INTEGER NOT NULL,
                        `kilometrajeReportado` INTEGER NOT NULL,
                        `estadoVehiculo` TEXT NOT NULL,
                        `conscienteResponsabilidad` TEXT NOT NULL DEFAULT '',
                        `aprobadoRuta` TEXT NOT NULL DEFAULT '',
                        `observacionesFinales` TEXT NOT NULL,
                        `checkSoat` TEXT NOT NULL,
                        `checkTecno` TEXT NOT NULL,
                        `checkLicencia` TEXT NOT NULL,
                        `checkExtintor` TEXT NOT NULL,
                        `idUbicacion` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL,
                        `fechaSoat` TEXT NOT NULL,
                        `fechaTecno` TEXT NOT NULL,
                        `fechaLicencia` TEXT NOT NULL,
                        `checkNivelAceite` TEXT NOT NULL DEFAULT '',
                        `checkEstadoLlantas` TEXT NOT NULL DEFAULT '',
                        `checkEstadoLuces` TEXT NOT NULL DEFAULT '',
                        `placaVehiculo` TEXT NOT NULL DEFAULT '',
                        `tipoVehiculo` TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO `pending_inspecciones_moto_new`
                        (`localId`, `serverId`, `uuid`, `timestamp`, `idVehiculo`,
                         `kilometrajeReportado`, `estadoVehiculo`, `conscienteResponsabilidad`,
                         `aprobadoRuta`, `observacionesFinales`, `checkSoat`, `checkTecno`,
                         `checkLicencia`, `checkExtintor`, `idUbicacion`, `isSynced`, `isSyncing`,
                         `fechaSoat`, `fechaTecno`, `fechaLicencia`, `checkNivelAceite`,
                         `checkEstadoLlantas`, `checkEstadoLuces`, `placaVehiculo`, `tipoVehiculo`)
                    SELECT
                        `localId`, `serverId`, `uuid`, `timestamp`, `idVehiculo`,
                        `kilometrajeReportado`, `estadoVehiculo`, `conscienteResponsabilidad`,
                        `aprobadoRuta`, `observacionesFinales`, `checkSoat`, `checkTecno`,
                        `checkLicencia`, `checkExtintor`, `idUbicacion`, `isSynced`, `isSyncing`,
                        `fechaSoat`, `fechaTecno`, `fechaLicencia`, `checkNivelAceite`,
                        `checkEstadoLlantas`, `checkEstadoLuces`, `placaVehiculo`, `tipoVehiculo`
                    FROM `pending_inspecciones_moto`
                """.trimIndent())

                database.execSQL("DROP TABLE `pending_inspecciones_moto`")
                database.execSQL("ALTER TABLE `pending_inspecciones_moto_new` RENAME TO `pending_inspecciones_moto`")
            }
        }

        /**
         * Migración 37 → 38: mismo problema que MIGRATION_36_37 pero en
         * vehiculo_inspections. Las columnas agregadas por MIGRATION_26_27
         * (urlImagenSoat/Tecno/Licencia/Extintor, registrarCambioAceite, oilType,
         * oilAirFilterChanged) quedaron sin DEFAULT en dispositivos que ya habían
         * migrado antes de que VehiculoInspectionEntity declarara
         * @ColumnInfo(defaultValue = ...) en esos campos.
         */
        val MIGRATION_37_38 = object : Migration(37, 38) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vehiculo_inspections_new` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `UUID` TEXT NOT NULL,
                        `serverId` INTEGER,
                        `timestamp` INTEGER NOT NULL,
                        `placaVehiculo` TEXT NOT NULL,
                        `marca` TEXT NOT NULL,
                        `tipoVehiculo` TEXT NOT NULL,
                        `kilometrajeReportado` INTEGER NOT NULL,
                        `responsableInspeccion` TEXT NOT NULL,
                        `aprobadoRuta` INTEGER NOT NULL,
                        `observacionesFinales` TEXT NOT NULL,
                        `nivelAceite` TEXT NOT NULL,
                        `nivelRefrigerante` TEXT NOT NULL,
                        `nivelFrenos` TEXT NOT NULL,
                        `estadoLlantas` TEXT NOT NULL,
                        `lucesGeneral` TEXT NOT NULL,
                        `estadoVisual` TEXT NOT NULL,
                        `limpiezaGeneral` TEXT NOT NULL,
                        `checkSoat` TEXT NOT NULL,
                        `checkTecno` TEXT NOT NULL,
                        `checkLicencia` TEXT NOT NULL,
                        `checkExtintor` TEXT NOT NULL,
                        `vigenciaExtintor` TEXT NOT NULL,
                        `fechaVencSoat` TEXT NOT NULL,
                        `fechaVencTecno` TEXT NOT NULL,
                        `fechaVencLicencia` TEXT NOT NULL,
                        `urlImagenSoat` TEXT NOT NULL DEFAULT '',
                        `urlImagenTecno` TEXT NOT NULL DEFAULT '',
                        `urlImagenLicencia` TEXT NOT NULL DEFAULT '',
                        `urlImagenExtintor` TEXT NOT NULL DEFAULT '',
                        `registrarCambioAceite` INTEGER NOT NULL DEFAULT 0,
                        `oilType` TEXT NOT NULL DEFAULT '',
                        `oilBrandId` INTEGER,
                        `oilIntervalKm` INTEGER,
                        `oilQuantity` REAL,
                        `oilAirFilterChanged` INTEGER NOT NULL DEFAULT 0,
                        `tieneBotiquin` INTEGER NOT NULL,
                        `tieneSeñalizacion` INTEGER NOT NULL,
                        `tieneLineasEmergencia` INTEGER NOT NULL,
                        `tieneLlantaRepuesto` INTEGER NOT NULL,
                        `tieneGatoHidraulico` INTEGER NOT NULL,
                        `saludFisica` INTEGER NOT NULL,
                        `saludMental` INTEGER NOT NULL,
                        `sobrio` INTEGER NOT NULL,
                        `medicamentos` INTEGER NOT NULL,
                        `conscienteResponsabilidad` INTEGER NOT NULL,
                        `condicionParaConducir` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO `vehiculo_inspections_new`
                        (`localId`, `UUID`, `serverId`, `timestamp`, `placaVehiculo`, `marca`,
                         `tipoVehiculo`, `kilometrajeReportado`, `responsableInspeccion`, `aprobadoRuta`,
                         `observacionesFinales`, `nivelAceite`, `nivelRefrigerante`, `nivelFrenos`,
                         `estadoLlantas`, `lucesGeneral`, `estadoVisual`, `limpiezaGeneral`, `checkSoat`,
                         `checkTecno`, `checkLicencia`, `checkExtintor`, `vigenciaExtintor`, `fechaVencSoat`,
                         `fechaVencTecno`, `fechaVencLicencia`, `urlImagenSoat`, `urlImagenTecno`,
                         `urlImagenLicencia`, `urlImagenExtintor`, `registrarCambioAceite`, `oilType`,
                         `oilBrandId`, `oilIntervalKm`, `oilQuantity`, `oilAirFilterChanged`, `tieneBotiquin`,
                         `tieneSeñalizacion`, `tieneLineasEmergencia`, `tieneLlantaRepuesto`,
                         `tieneGatoHidraulico`, `saludFisica`, `saludMental`, `sobrio`, `medicamentos`,
                         `conscienteResponsabilidad`, `condicionParaConducir`, `isSynced`, `isSyncing`)
                    SELECT
                        `localId`, `UUID`, `serverId`, `timestamp`, `placaVehiculo`, `marca`,
                        `tipoVehiculo`, `kilometrajeReportado`, `responsableInspeccion`, `aprobadoRuta`,
                        `observacionesFinales`, `nivelAceite`, `nivelRefrigerante`, `nivelFrenos`,
                        `estadoLlantas`, `lucesGeneral`, `estadoVisual`, `limpiezaGeneral`, `checkSoat`,
                        `checkTecno`, `checkLicencia`, `checkExtintor`, `vigenciaExtintor`, `fechaVencSoat`,
                        `fechaVencTecno`, `fechaVencLicencia`, `urlImagenSoat`, `urlImagenTecno`,
                        `urlImagenLicencia`, `urlImagenExtintor`, `registrarCambioAceite`, `oilType`,
                        `oilBrandId`, `oilIntervalKm`, `oilQuantity`, `oilAirFilterChanged`, `tieneBotiquin`,
                        `tieneSeñalizacion`, `tieneLineasEmergencia`, `tieneLlantaRepuesto`,
                        `tieneGatoHidraulico`, `saludFisica`, `saludMental`, `sobrio`, `medicamentos`,
                        `conscienteResponsabilidad`, `condicionParaConducir`, `isSynced`, `isSyncing`
                    FROM `vehiculo_inspections`
                """.trimIndent())

                database.execSQL("DROP TABLE `vehiculo_inspections`")
                database.execSQL("ALTER TABLE `vehiculo_inspections_new` RENAME TO `vehiculo_inspections`")
            }
        }

        /**
         * Migración 38 → 39: mismo problema, esta vez preventivo en
         * vehiculo_oil_changes.assetType (agregada por MIGRATION_33_34). No se
         * confirmó una falla real en este caso, pero sigue el mismo patrón de
         * ALTER TABLE + @ColumnInfo(defaultValue) que ya rompió otras dos tablas,
         * así que se cierra por consistencia antes de que aparezca en producción.
         */
        val MIGRATION_38_39 = object : Migration(38, 39) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vehiculo_oil_changes_new` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `placa` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `oilType` TEXT NOT NULL,
                        `oilBrandId` INTEGER NOT NULL,
                        `oilBrandName` TEXT NOT NULL,
                        `quantity` REAL,
                        `kmAtChange` INTEGER NOT NULL,
                        `intervalKm` INTEGER NOT NULL,
                        `airFilterChanged` INTEGER NOT NULL,
                        `assetType` TEXT NOT NULL DEFAULT 'VEHICLE',
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO `vehiculo_oil_changes_new`
                        (`localId`, `placa`, `timestamp`, `oilType`, `oilBrandId`, `oilBrandName`,
                         `quantity`, `kmAtChange`, `intervalKm`, `airFilterChanged`, `assetType`,
                         `isSynced`, `isSyncing`)
                    SELECT
                        `localId`, `placa`, `timestamp`, `oilType`, `oilBrandId`, `oilBrandName`,
                        `quantity`, `kmAtChange`, `intervalKm`, `airFilterChanged`, `assetType`,
                        `isSynced`, `isSyncing`
                    FROM `vehiculo_oil_changes`
                """.trimIndent())

                database.execSQL("DROP TABLE `vehiculo_oil_changes`")
                database.execSQL("ALTER TABLE `vehiculo_oil_changes_new` RENAME TO `vehiculo_oil_changes`")
            }
        }

        /**
         * Migración 39 → 40: elimina las tablas del módulo de combustibles
         * (fuel_logs_local, fuel_stations_local). El módulo se retiró de las
         * entidades de @Database (ver rama desarrollo-modulo-combustibles) sin
         * subir la versión, lo que dejaba el hash de esquema de Room
         * desincronizado en cualquier instalación que ya hubiera llegado a la
         * versión 39 con esas tablas presentes, y la app crasheaba al abrir
         * con "Room cannot verify the data integrity" (checkIdentity).
         */
        val MIGRATION_39_40 = object : Migration(39, 40) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `fuel_logs_local`")
                database.execSQL("DROP TABLE IF EXISTS `fuel_stations_local`")
            }
        }

        // MachineOilChangeImprovedEntity nunca tuvo consumidores reales (sin
        // repositorio, sin caso de uso, sin UI) — se retira sin reemplazo.
        val MIGRATION_40_41 = object : Migration(40, 41) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `machine_oil_changes_improved`")
            }
        }

        // VehicleOilChangeImprovedEntity nunca tuvo consumidores reales (sin
        // repositorio, sin caso de uso, sin UI) — se retira sin reemplazo. Los
        // endpoints /v1/improved-oil-changes/* que la acompañaban en ApiService
        // tampoco tenían controlador en el backend (nunca se implementaron).
        val MIGRATION_41_42 = object : Migration(41, 42) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `vehicle_oil_changes_improved`")
            }
        }

        // OilAnalysisSosEntity nunca tuvo consumidores reales (sin repositorio, sin
        // caso de uso, sin UI) ni del lado backend (sin controlador) — mismo patrón
        // que MachineOilChangeImproved/VehicleOilChangeImproved.
        val MIGRATION_42_43 = object : Migration(42, 43) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `oil_analysis_sos`")
            }
        }

        /**
         * Migración 43 → 44: módulo Subestaciones (Civil). Crea la tabla de ejecuciones
         * pendientes (mismo patrón que pending_forms: UUID + isSynced/isSyncing para
         * idempotencia y lock atómico de sync) y los catálogos cacheados de estaciones/
         * actividades (mismo patrón que `machines`: id sin autogenerar porque viene del
         * servidor, clearAndInsert reemplaza todo). También agrega la 3ª FK polimórfica
         * nullable a pending_images para que la evidencia de una ejecución use el mismo
         * ImageDao/ImageSyncWorker que ya usan Form y Vehículo.
         */
        val MIGRATION_43_44 = object : Migration(43, 44) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pending_mant_ejecucion` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `serverId` INTEGER,
                        `uuidCliente` TEXT NOT NULL,
                        `fecha` TEXT NOT NULL,
                        `mesEjecucion` INTEGER NOT NULL,
                        `semanaEjecucion` INTEGER NOT NULL,
                        `estacionId` INTEGER NOT NULL,
                        `estacionNombre` TEXT NOT NULL,
                        `tipoMantenimiento` TEXT NOT NULL,
                        `tipoActividad` TEXT NOT NULL,
                        `actividadId` INTEGER,
                        `actividadNombre` TEXT,
                        `programacionId` INTEGER,
                        `esProgramada` INTEGER NOT NULL,
                        `motivoNoCatalogado` TEXT,
                        `resultado` TEXT NOT NULL,
                        `observaciones` TEXT NOT NULL,
                        `descripcionLibre` TEXT,
                        `isSynced` INTEGER NOT NULL,
                        `isSyncing` INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `mant_estacion_cache` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `frecuenciaBase` TEXT NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `mant_actividad_cache` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `nombre` TEXT NOT NULL
                    )
                """.trimIndent())
                database.execSQL("ALTER TABLE pending_images ADD COLUMN ejecucionUUID TEXT")
            }
        }

        /**
         * Migración 44 → 45: agrega `syncFallido` a `pending_mant_ejecucion` — antes
         * un fallo persistente de envío (red/servidor) era indistinguible de "todavía
         * no hay señal, esperando". Se limpia (`syncFallido=0`) cada vez que se
         * reintenta el envío (ver `EjecucionDao.acquireLock`), y se marca en `1` solo
         * cuando `syncEjecucion` falla de verdad.
         */
        val MIGRATION_44_45 = object : Migration(44, 45) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pending_mant_ejecucion ADD COLUMN syncFallido INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
    abstract fun formDao(): FormDao
    abstract fun machineDao(): MachineDao
    abstract fun logDao(): LogDao
    abstract fun machineOilChangeDao(): MachineOilChangeDao
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
    abstract fun motoOilChangeDao(): MotoOilChangeDao
    abstract fun ejecucionDao(): EjecucionDao
    abstract fun estacionCacheDao(): EstacionCacheDao
    abstract fun actividadCacheDao(): ActividadCacheDao
}
