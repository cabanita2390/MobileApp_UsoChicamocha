package com.example.testusoandroidstudio_1_usochicamocha.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.work.WorkManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.AppDatabase
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.*
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.AuthInterceptor
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.TokenAuthenticator
import com.example.testusoandroidstudio_1_usochicamocha.data.repository.*
import com.example.testusoandroidstudio_1_usochicamocha.data.repository.MaintenanceRepositoryImpl
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.log.GetLogsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.GetLocalMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.SyncMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance.*
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.GetPendingVehiculoInspectionsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SaveVehiculoInspectionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SyncVehiculoInspectionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SyncVehiclesCatalogUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.AppLogger
import com.example.testusoandroidstudio_1_usochicamocha.util.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //private const val BASE_URL = "https://pdxs8r4k-8080.use2.devtunnels.ms/"+"api/"
    //private const val BASE_URL = "https://usochimochabackend.onrender.com/"+"api/"
    //private const val BASE_URL = "https://server.usochicamocha.co/"+"api/"  // ← PRODUCCIÓN
    //private const val BASE_URL = "http://10.2.236.89:8080/"+"api/"          // ← LOCAL (WiFi, dispositivo físico)
    private const val BASE_URL = "http://localhost:8080/"+"api/"               // ← LOCAL (USB + adb reverse)
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides


    
    @Singleton
    fun provideApiService(tokenAuthenticator: TokenAuthenticator, authInterceptor: AuthInterceptor): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenManager(dataStore: DataStore<Preferences>): TokenManager = TokenManager(dataStore)

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor = NetworkMonitor(context)

    // Repositories
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
    fun provideMaintenanceRepository(maintenanceDao: MaintenanceDao, apiService: ApiService): MaintenanceRepository {
        return MaintenanceRepositoryImpl(maintenanceDao, apiService)
    }

    @Provides
    @Singleton
    fun provideVehiculoInspectionRepository(
        vehiculoInspectionDao: VehiculoInspectionDao,
        vehiculoDao: VehiculoDao,
        apiService: ApiService
    ): VehiculoInspectionRepository = VehiculoInspectionRepositoryImpl(vehiculoInspectionDao, vehiculoDao, apiService)

    // DAOs
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
    fun provideVehiculoInspectionDao(db: AppDatabase): VehiculoInspectionDao = db.vehiculoInspectionDao()

    @Provides
    @Singleton
    fun provideVehiculoDao(db: AppDatabase): VehiculoDao = db.vehiculoDao()

    // Use Cases
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
    fun provideLogoutUseCase(repo: AuthRepository): LogoutUseCase = LogoutUseCase(repo)

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
    fun provideOilRepository(apiService: ApiService, oilDao: OilDao): OilRepository = OilRepositoryImpl(apiService, oilDao)

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
    fun provideLocalSyncCoordinator(
        @ApplicationContext context: Context,
        workManager: WorkManager
    ): LocalSyncCoordinator = LocalSyncCoordinator(context, workManager)
}
