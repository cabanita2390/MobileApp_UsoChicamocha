package com.example.testusoandroidstudio_1_usochicamocha.di

import android.content.Context
import androidx.room.Room
import com.example.testusoandroidstudio_1_usochicamocha.data.local.AppDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
            .addMigrations(
                AppDatabase.MIGRATION_23_24,
                AppDatabase.MIGRATION_24_25,
                AppDatabase.MIGRATION_25_26,
                AppDatabase.MIGRATION_26_27,
                AppDatabase.MIGRATION_27_28,
                AppDatabase.MIGRATION_28_29,
                AppDatabase.MIGRATION_29_30,
                AppDatabase.MIGRATION_30_31,
                AppDatabase.MIGRATION_31_32,
                AppDatabase.MIGRATION_32_33,
                AppDatabase.MIGRATION_33_34
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideFormDao(db: AppDatabase): FormDao = db.formDao()

    @Provides
    @Singleton
    fun provideImageDao(db: AppDatabase): ImageDao = db.imageDao()

    @Provides
    @Singleton
    fun provideMachineDao(db: AppDatabase): MachineDao = db.machineDao()

    @Provides
    @Singleton
    fun provideLogDao(db: AppDatabase): LogDao = db.logDao()

    @Provides
    @Singleton
    fun provideMaintenanceDao(db: AppDatabase): MaintenanceDao = db.maintenanceDao()

    @Provides
    @Singleton
    fun provideOilDao(db: AppDatabase): OilDao = db.oilDao()

    @Provides
    @Singleton
    fun provideMotoDao(db: AppDatabase): MotoDao = db.motoDao()

    @Provides
    @Singleton
    fun provideUbicacionDao(db: AppDatabase): UbicacionDao = db.ubicacionDao()

    @Provides
    @Singleton
    fun provideDocumentoMotoDao(db: AppDatabase): DocumentoMotoDao = db.documentoMotoDao()

    @Provides
    @Singleton
    fun provideVehiculoInspectionDao(db: AppDatabase): VehiculoInspectionDao = db.vehiculoInspectionDao()

    @Provides
    @Singleton
    fun provideVehiculoDao(db: AppDatabase): VehiculoDao = db.vehiculoDao()

    @Provides
    @Singleton
    fun provideDocumentoVehiculoDao(db: AppDatabase): DocumentoVehiculoDao = db.documentoVehiculoDao()

    @Provides
    @Singleton
    fun provideVehiculoOilChangeDao(db: AppDatabase): VehiculoOilChangeDao = db.vehiculoOilChangeDao()

    @Provides
    @Singleton
    fun provideMotoOilChangeDao(db: AppDatabase): MotoOilChangeDao = db.motoOilChangeDao()

    @Provides
    @Singleton
    fun provideInspeccionMotoDao(db: AppDatabase): InspeccionMotoDao = db.inspeccionMotoDao()

    @Provides
    @Singleton
    fun provideFuelLogDao(db: AppDatabase): FuelLogDao = db.fuelLogDao()

    @Provides
    @Singleton
    fun provideFuelStationDao(db: AppDatabase): FuelStationDao = db.fuelStationDao()

    @Provides
    @Singleton
    fun provideVehicleOilChangeImprovedDao(db: AppDatabase): VehicleOilChangeImprovedDao = db.vehicleOilChangeImprovedDao()

    @Provides
    @Singleton
    fun provideMachineOilChangeImprovedDao(db: AppDatabase): MachineOilChangeImprovedDao = db.machineOilChangeImprovedDao()

    @Provides
    @Singleton
    fun provideOilAnalysisSosDao(db: AppDatabase): OilAnalysisSosDao = db.oilAnalysisSosDao()
}
