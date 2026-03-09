
package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.LogDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MachineDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MaintenanceDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.OilDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoInspectionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LogEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MaintenanceEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity

@Database(
    entities = [
        FormEntity::class,
        MachineEntity::class,
        LogEntity::class,
        MaintenanceEntity::class,
        OilEntity::class,
        ImageEntity::class,
        VehiculoInspectionEntity::class,
        VehiculoEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun formDao(): FormDao
    abstract fun machineDao(): MachineDao
    abstract fun logDao(): LogDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun oilDao(): OilDao
    abstract fun imageDao(): ImageDao
    abstract fun vehiculoInspectionDao(): VehiculoInspectionDao
    abstract fun vehiculoDao(): VehiculoDao
}
