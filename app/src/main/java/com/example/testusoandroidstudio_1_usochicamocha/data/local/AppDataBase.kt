package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
        com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity::class
    ],
    version = 23,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
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
}
