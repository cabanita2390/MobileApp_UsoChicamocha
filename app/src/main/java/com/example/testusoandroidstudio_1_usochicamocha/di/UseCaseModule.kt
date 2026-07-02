package com.example.testusoandroidstudio_1_usochicamocha.di

import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.GetPendingInspeccionesMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SaveInspeccionMotoLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SyncInspeccionMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.log.GetLogsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.GetLocalMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.SyncMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalUbicacionesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncDocumentosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncUbicacionesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.GetPendingVehiculoInspectionsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SaveVehiculoInspectionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SyncVehiclesCatalogUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SyncVehiculoInspectionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.AppLogger
import com.example.testusoandroidstudio_1_usochicamocha.util.NetworkMonitor
import com.example.testusoandroidstudio_1_usochicamocha.util.TokenRefreshMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(repo: AuthRepository, logger: AppLogger): LoginUseCase = LoginUseCase(repo, logger)

    @Provides
    @Singleton
    fun provideSyncMachinesUseCase(repo: MachineRepository, logger: AppLogger): SyncMachinesUseCase = SyncMachinesUseCase(repo, logger)

    @Provides
    @Singleton
    fun provideValidateSessionUseCase(tm: TokenManager, repo: AuthRepository, nm: NetworkMonitor): ValidateSessionUseCase = ValidateSessionUseCase(tm, repo, nm)

    @Provides
    @Singleton
    fun provideLogoutUseCase(repo: AuthRepository, monitor: TokenRefreshMonitor): LogoutUseCase = LogoutUseCase(repo, monitor)

    @Provides
    @Singleton
    fun provideGetPendingFormsUseCase(repo: FormRepository): GetPendingFormsUseCase = GetPendingFormsUseCase(repo)

    @Provides
    @Singleton
    fun provideGetLocalMachinesUseCase(repo: MachineRepository): GetLocalMachinesUseCase = GetLocalMachinesUseCase(repo)

    @Provides
    @Singleton
    fun provideGetLogsUseCase(repo: LogRepository): GetLogsUseCase = GetLogsUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveMaintenanceFormUseCase(repo: MaintenanceRepository): SaveMaintenanceFormUseCase = SaveMaintenanceFormUseCase(repo)

    @Provides
    @Singleton
    fun provideGetPendingMaintenanceFormsUseCase(repo: MaintenanceRepository): GetPendingMaintenanceFormsUseCase = GetPendingMaintenanceFormsUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncMaintenanceFormsUseCase(repo: MaintenanceRepository): SyncMaintenanceFormsUseCase = SyncMaintenanceFormsUseCase(repo)

    @Provides
    @Singleton
    fun provideGetLocalMotosUseCase(repo: MotoRepository): GetLocalMotosUseCase = GetLocalMotosUseCase(repo)

    @Provides
    @Singleton
    fun provideGetLocalUbicacionesUseCase(repo: MotoRepository): GetLocalUbicacionesUseCase = GetLocalUbicacionesUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncMotosUseCase(repo: MotoRepository): SyncMotosUseCase = SyncMotosUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncUbicacionesUseCase(repo: MotoRepository): SyncUbicacionesUseCase = SyncUbicacionesUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncDocumentosUseCase(repo: MotoRepository): SyncDocumentosUseCase = SyncDocumentosUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncOilsUseCase(repo: OilRepository, logger: AppLogger): SyncOilsUseCase = SyncOilsUseCase(repo, logger)

    @Provides
    @Singleton
    fun provideGetLocalOilsUseCase(repo: OilRepository): GetLocalOilsUseCase = GetLocalOilsUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncPendingImagesUseCase(repo: FormRepository): SyncPendingImagesUseCase = SyncPendingImagesUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveVehiculoInspectionUseCase(repo: VehiculoInspectionRepository): SaveVehiculoInspectionUseCase = SaveVehiculoInspectionUseCase(repo)

    @Provides
    @Singleton
    fun provideGetPendingVehiculoInspectionsUseCase(repo: VehiculoInspectionRepository): GetPendingVehiculoInspectionsUseCase = GetPendingVehiculoInspectionsUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncVehiculoInspectionUseCase(repo: VehiculoInspectionRepository): SyncVehiculoInspectionUseCase = SyncVehiculoInspectionUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncVehiclesCatalogUseCase(repo: VehiculoInspectionRepository): SyncVehiclesCatalogUseCase = SyncVehiclesCatalogUseCase(repo)

    @Provides
    @Singleton
    fun provideSaveInspeccionMotoLocalUseCase(repo: InspeccionMotoRepository): SaveInspeccionMotoLocalUseCase =
        SaveInspeccionMotoLocalUseCase(repo)

    @Provides
    @Singleton
    fun provideSyncInspeccionMotoUseCase(repo: InspeccionMotoRepository): SyncInspeccionMotoUseCase =
        SyncInspeccionMotoUseCase(repo)

    @Provides
    @Singleton
    fun provideGetPendingInspeccionesMotoUseCase(repo: InspeccionMotoRepository): GetPendingInspeccionesMotoUseCase =
        GetPendingInspeccionesMotoUseCase(repo)
}
