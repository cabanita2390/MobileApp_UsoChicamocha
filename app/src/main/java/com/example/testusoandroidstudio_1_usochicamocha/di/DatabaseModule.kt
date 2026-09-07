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
            // No existe una ruta de migración real entre la v2 y la v23 (nunca se
            // escribieron Migration para ese rango; MIGRATION_1_2 migraba a una tabla
            // sync_tracking que ya ni siquiera está registrada en @Database). Sin esto,
            // cualquier instalación que haya quedado en esas versiones crashea en bucle
            // al abrir la app en vez de simplemente recrear la base de datos.
            .fallbackToDestructiveMigrationFrom(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22
            )
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
                AppDatabase.MIGRATION_33_34,
                AppDatabase.MIGRATION_34_35,
                AppDatabase.MIGRATION_35_36,
                AppDatabase.MIGRATION_36_37,
                AppDatabase.MIGRATION_37_38,
                AppDatabase.MIGRATION_38_39,
                AppDatabase.MIGRATION_39_40,
                AppDatabase.MIGRATION_40_41,
                AppDatabase.MIGRATION_41_42,
                AppDatabase.MIGRATION_42_43
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
    fun provideMachineOilChangeDao(db: AppDatabase): MachineOilChangeDao = db.machineOilChangeDao()

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
}
