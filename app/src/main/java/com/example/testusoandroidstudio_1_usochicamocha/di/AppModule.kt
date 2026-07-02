package com.example.testusoandroidstudio_1_usochicamocha.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.WorkManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.AuthRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.util.AppLogger
import com.example.testusoandroidstudio_1_usochicamocha.util.NetworkMonitor
import com.example.testusoandroidstudio_1_usochicamocha.util.TokenRefreshMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Providers que no encajan en Network/Database/Repository/UseCase:
 * WorkManager, TokenManager, NetworkMonitor, LocalSyncCoordinator y TokenRefreshMonitor.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideTokenManager(dataStore: DataStore<Preferences>): TokenManager = TokenManager(dataStore)

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor = NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideLocalSyncCoordinator(
        @ApplicationContext context: Context,
        workManager: WorkManager
    ): LocalSyncCoordinator = LocalSyncCoordinator(context, workManager)

    @Provides
    @Singleton
    fun provideTokenRefreshMonitor(
        tokenManager: TokenManager,
        authRepository: AuthRepository,
        appLogger: AppLogger
    ): TokenRefreshMonitor = TokenRefreshMonitor(tokenManager, authRepository, appLogger)
}
