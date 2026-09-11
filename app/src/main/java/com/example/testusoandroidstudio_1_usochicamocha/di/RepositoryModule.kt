package com.example.testusoandroidstudio_1_usochicamocha.di

import android.content.Context
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.*
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.repository.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService, tokenManager: TokenManager): AuthRepository = AuthRepositoryImpl(apiService, tokenManager)

    @Provides
    @Singleton
    fun provideFormRepository(
        @ApplicationContext context: Context,
        formDao: FormDao,
        imageDao: ImageDao,
        apiService: ApiService
    ): FormRepository = FormRepositoryImpl(context, formDao, imageDao, apiService)

    @Provides
    @Singleton
    fun provideMachineRepository(apiService: ApiService, machineDao: MachineDao): MachineRepository = MachineRepositoryImpl(apiService, machineDao)

    @Provides
    @Singleton
    fun provideLogRepository(logDao: LogDao): LogRepository = LogRepositoryImpl(logDao)

    @Provides
    @Singleton
    fun provideMachineOilChangeRepository(machineOilChangeDao: MachineOilChangeDao, apiService: ApiService): MachineOilChangeRepository {
        return MachineOilChangeRepositoryImpl(machineOilChangeDao, apiService)
    }

    @Provides
    @Singleton
    fun provideVehiculoInspectionRepository(
        vehiculoInspectionDao: VehiculoInspectionDao,
        vehiculoDao: VehiculoDao,
        documentoVehiculoDao: DocumentoVehiculoDao,
        apiService: ApiService
    ): VehiculoInspectionRepository = VehiculoInspectionRepositoryImpl(vehiculoInspectionDao, vehiculoDao, documentoVehiculoDao, apiService)

    @Provides
    @Singleton
    fun provideVehiculoOilChangeRepository(
        dao: VehiculoOilChangeDao,
        apiService: ApiService
    ): VehiculoOilChangeRepository = VehiculoOilChangeRepositoryImpl(dao, apiService)

    @Provides
    @Singleton
    fun provideMotoOilChangeRepository(
        dao: MotoOilChangeDao,
        apiService: ApiService
    ): MotoOilChangeRepository = MotoOilChangeRepositoryImpl(dao, apiService)

    @Provides
    @Singleton
    fun provideOilRepository(apiService: ApiService, oilDao: OilDao): OilRepository = OilRepositoryImpl(apiService, oilDao)

    @Provides
    @Singleton
    fun provideMotoRepository(
        apiService: ApiService,
        motoDao: MotoDao,
        ubicacionDao: UbicacionDao,
        documentoMotoDao: DocumentoMotoDao
    ): MotoRepository = MotoRepositoryImpl(apiService, motoDao, ubicacionDao, documentoMotoDao)

    @Provides
    @Singleton
    fun provideInspeccionMotoRepository(
        dao: InspeccionMotoDao,
        apiService: ApiService
    ): InspeccionMotoRepository = InspeccionMotoRepositoryImpl(dao, apiService)

    @Provides
    @Singleton
    fun provideSubestacionRepository(
        @ApplicationContext context: Context,
        ejecucionDao: EjecucionDao,
        estacionCacheDao: EstacionCacheDao,
        actividadCacheDao: ActividadCacheDao,
        imageDao: ImageDao,
        apiService: ApiService,
        cumplimientoCacheDao: CumplimientoCacheDao,
        ejecucionDetalleCacheDao: EjecucionDetalleCacheDao
    ): SubestacionRepository = SubestacionRepositoryImpl(
        context, ejecucionDao, estacionCacheDao, actividadCacheDao, imageDao, apiService,
        cumplimientoCacheDao, ejecucionDetalleCacheDao
    )
}
