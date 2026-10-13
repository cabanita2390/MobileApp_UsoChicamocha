package com.example.testusoandroidstudio_1_usochicamocha

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ArchivosInterceptor
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.SyncDataWorker
import okhttp3.OkHttpClient
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var tokenManager: TokenManager

    /** Cargador de imágenes de toda la app: agrega la credencial a las URL de /uploads. */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient { OkHttpClient.Builder().addInterceptor(ArchivosInterceptor(tokenManager)).build() }
        .build()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("MyApplication", "App initialized with Hilt")
        scheduleBackgroundSync()
        triggerImmediateSync()
    }

    private fun scheduleBackgroundSync() {
        try {
            // 1. Definimos las restricciones comunes: solo se ejecuta si hay internet.
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // 2. Creamos la petición para el WORKER DE DATOS (TEXTO)
            val syncDataRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            // 3. Planificamos el trabajo de datos de forma única.
            // El SyncDataWorker se encargará de encolar ImageSyncWorker cuando sea necesario
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "background_sync_worker",
                ExistingPeriodicWorkPolicy.KEEP, // Mantiene el trabajo existente si ya está planificado
                syncDataRequest
            )

            Log.d("MyApplication", "Programado SyncDataWorker periódico - él coordinará todo el proceso de sync")
        } catch (e: Exception) {
            Log.e("MyApplication", "Error scheduling background sync", e)
        }
    }

    private fun triggerImmediateSync() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // CORRECCIÓN: Solo encolamos el worker de DATOS
            // El SyncDataWorker se encargará de encolar el worker de imágenes DESPUÉS de sincronizar los formularios
            val oneTimeSyncDataRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
                .setConstraints(constraints)
                .build()

            // Encolamos SOLO el trabajo de datos
            // ExistingWorkPolicy.KEEP: si ya hay un trabajo en cola, no hace nada.
            WorkManager.getInstance(this).enqueueUniqueWork(
                "immediate_data_sync",
                ExistingWorkPolicy.KEEP,
                oneTimeSyncDataRequest
            )

            Log.d("MyApplication", "Encolado SyncDataWorker - él se encargará de encolar ImageSyncWorker después de sincronizar datos")
        } catch (e: Exception) {
            Log.e("MyApplication", "Error triggering immediate sync", e)
        }
    }
}
