package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.CleanupWorker
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.SyncDataWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSyncCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {

    // Scope interno para monitorear trabajos sin bloquear
    private val coordinatorScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    companion object {
        private const val TAG = "LocalSyncCoordinator"
        private const val COORDINATED_SYNC_WORK = "coordinated_sync_work"
        private const val CLEANUP_WORK = "periodic_cleanup_work"
        private const val MASTER_DATA_PERIODIC_WORK = "master_data_periodic_sync"
        private const val SYNC_TIMEOUT_MS = 300000L // 5 minutos timeout
    }
    
    /**
     * Programa sincronización periódica de datos maestros (Placas, Ubicaciones) cada 15 min.
     */
    fun schedulePeriodicMasterDataSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val periodicWork = PeriodicWorkRequestBuilder<SyncDataWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(workDataOf("SYNC_TYPE" to SyncType.MASTER_DATA.name))
            .addTag(COORDINATED_SYNC_WORK)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            MASTER_DATA_PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
        Log.d(TAG, "⚙️ Periodic master data sync (15m) scheduled")
    }
    
    private val activeSyncOperations = ConcurrentHashMap<String, Long>()
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus
    
    sealed class SyncTrigger {
        abstract fun getWorkName(): String

        data class FormSaved(val formType: String) : SyncTrigger() {
            override fun getWorkName(): String = "form_save_sync"
        }
        data class MaintenanceSaved(val maintenanceType: String) : SyncTrigger() {
            override fun getWorkName(): String = "maintenance_save_sync"
        }
        data class ManualSync(val syncType: SyncType) : SyncTrigger() {
            override fun getWorkName(): String = "manual_${syncType}_sync"
        }
        data class PeriodicSync(val interval: Long) : SyncTrigger() {
            override fun getWorkName(): String = "periodic_sync"
        }
        data class AppStartSync(val reason: String) : SyncTrigger() {
            override fun getWorkName(): String = "app_start_sync"
        }
    }

    enum class SyncType {
        ALL_DATA,
        FORMS_ONLY,
        MAINTENANCE_ONLY,
        IMAGES_ONLY,
        MASTER_DATA,
        MACHINES_ONLY,
        OILS_ONLY,
        MOTOS_ONLY,
        UBICACIONES_ONLY,
        DOCUMENTS_ONLY
    }

    enum class SyncStatus {
        IDLE,
        COORDINATING,
        SYNCING,
        CLEANING,
        ERROR
    }
    
    /**
     * Punto de entrada principal para sincronización coordinada
     */
    suspend fun coordinateSync(trigger: SyncTrigger): Result<Unit> {
        val workName = trigger.getWorkName()
        val currentTime = System.currentTimeMillis()
        
        return try {
            Log.d(TAG, "🎯 Coordinating sync triggered: $trigger")
            
            // 1. Verificar y limpiar trabajos bloqueados o muy antiguos
            cleanupStuckWork(workName)
            
            // 2. Verificar si hay un work con el mismo nombre ya en progreso
            val existingWorkInfos = workManager.getWorkInfosForUniqueWork(workName).await()
            val runningWork = existingWorkInfos.find {
                it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
            }
            
            if (runningWork != null) {
                Log.d(TAG, "⏭️  Work $workName already running (id: ${runningWork.id}), attaching monitor")
                // Si ya está corriendo, asegurar que el estado refleje eso y monitorear
                if (_syncStatus.value == SyncStatus.IDLE) {
                    _syncStatus.value = SyncStatus.SYNCING
                }
                // Monitor the existing work so the UI doesn't freeze
                monitorWorkStatus(runningWork.id)
                return Result.success(Unit)
            }
            
            // 3. Marcar operación como activa en memoria
            activeSyncOperations[workName] = currentTime
            _syncStatus.value = SyncStatus.COORDINATING
            
            // 4. Crear constraints para el work
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            // 5. Crear el work request apropiado
            val workRequest = createWorkRequestForTrigger(trigger, constraints)
            
            // 6. Encolar con nombre único usando REPLACE para sobrescribir trabajos anteriores
            workManager.enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            Log.d(TAG, "✅ Coordinated sync enqueued: $workName with id: ${workRequest.id}")
            _syncStatus.value = SyncStatus.SYNCING
            
            // 7. Monitorear el trabajo para resetear el estado cuando termine
            monitorWorkStatus(workRequest.id)
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error coordinating sync for $workName", e)
            _syncStatus.value = SyncStatus.ERROR
            // Resetear el estado después de un error
            coordinatorScope.launch {
                kotlinx.coroutines.delay(3000)
                _syncStatus.value = SyncStatus.IDLE
            }
            Result.failure(e)
        } finally {
            activeSyncOperations.remove(workName)
        }
    }
    
    /**
     * Limpia trabajos que puedan estar bloqueados
     */
    private suspend fun cleanupStuckWork(workName: String) {
        try {
            val existingWorkInfos = workManager.getWorkInfosForUniqueWork(workName).await()
            val stuckWork = existingWorkInfos.find { workInfo ->
                workInfo.state == WorkInfo.State.ENQUEUED
            }
            
            if (stuckWork != null) {
                Log.d(TAG, "🧹 Cancelling stuck work: $workName")
                workManager.cancelWorkById(stuckWork.id)
                kotlinx.coroutines.delay(1000) // Dar tiempo para la cancelación
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up stuck work for $workName", e)
        }
    }

    private fun monitorWorkStatus(workId: java.util.UUID) {
        coordinatorScope.launch {
            try {
                Log.d(TAG, "🔍 Starting to monitor work: $workId")
                // CRITICAL FIX: Usar un timeout más largo y manejo robusto de estados
                kotlinx.coroutines.withTimeout(SYNC_TIMEOUT_MS) {
                    workManager.getWorkInfoByIdFlow(workId).collect { workInfo ->
                        if (workInfo == null) {
                            Log.w(TAG, "⚠️ WorkInfo is null for workId: $workId")
                            return@collect
                        }
                        
                        Log.d(TAG, "📊 Work $workId state: ${workInfo.state}")
                        
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                Log.d(TAG, "🏁 Work $workId completed successfully")
                                resetSyncStateAfterDelay("Work completed successfully")
                                throw java.util.concurrent.CancellationException("Work finished")
                            }
                            WorkInfo.State.FAILED -> {
                                val failureReason = workInfo.outputData.getString("FAILURE_REASON") ?: "Unknown error"
                                Log.e(TAG, "🏁 Work $workId failed. Reason: $failureReason")
                                resetSyncStateAfterDelay("Work failed")
                                throw java.util.concurrent.CancellationException("Work finished")
                            }
                            WorkInfo.State.CANCELLED -> {
                                Log.w(TAG, "🏁 Work $workId was cancelled")
                                resetSyncStateAfterDelay("Work cancelled")
                                throw java.util.concurrent.CancellationException("Work finished")
                            }
                            WorkInfo.State.RUNNING -> {
                                Log.d(TAG, "🔄 Work $workId is currently running")
                                _syncStatus.value = SyncStatus.SYNCING
                            }
                            WorkInfo.State.ENQUEUED -> {
                                Log.d(TAG, "⏳ Work $workId is enqueued")
                                _syncStatus.value = SyncStatus.COORDINATING
                            }
                            WorkInfo.State.BLOCKED -> {
                                Log.w(TAG, "⛔ Work $workId is blocked")
                                _syncStatus.value = SyncStatus.COORDINATING
                            }
                        }
                    }
                }
            } catch (e: java.util.concurrent.CancellationException) {
                Log.d(TAG, "✅ Monitor for work $workId cancelled normally")
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.w(TAG, "⚠️ Sync monitoring timed out for work $workId after ${SYNC_TIMEOUT_MS}ms")
                _syncStatus.value = SyncStatus.IDLE // Reset UI after timeout
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error monitoring work $workId", e)
                _syncStatus.value = SyncStatus.IDLE // Fallback safe
            }
        }
    }
    
    private fun resetSyncStateAfterDelay(reason: String) {
        coordinatorScope.launch {
            Log.d(TAG, "🔄 Resetting sync state after delay. Reason: $reason")
            kotlinx.coroutines.delay(2000) // Dar tiempo para que la UI procese el resultado
            _syncStatus.value = SyncStatus.IDLE
        }
    }
    
    /**
     * Programa limpieza periódica de la base de datos
     */
    fun schedulePeriodicCleanup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val periodicWork = PeriodicWorkRequestBuilder<CleanupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            CLEANUP_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
        
        Log.d(TAG, "🧹 Periodic cleanup scheduled")
    }
    
    /**
     * Ejecuta limpieza inmediata
     */
    suspend fun executeImmediateCleanup(): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.CLEANING
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
                .setConstraints(constraints)
                .build()
            
            workManager.enqueueUniqueWork(
                "immediate_cleanup",
                ExistingWorkPolicy.KEEP,
                cleanupRequest
            )
            
            Log.d(TAG, "🧹 Immediate cleanup enqueued")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error scheduling cleanup", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el estado actual de la sincronización
     */
    fun getCurrentSyncStatus(): SyncStatus {
        return _syncStatus.value
    }

    /**
     * Observa el estado de un trigger específico
     */
    fun observeSyncTrigger(trigger: SyncTrigger): Flow<Boolean> {
        return workManager.getWorkInfosForUniqueWorkFlow(trigger.getWorkName())
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }
            .distinctUntilChanged()
    }

    /**
     * Cancela todas las operaciones de sincronización activas
     */
    fun cancelAllSyncOperations() {
        workManager.cancelAllWorkByTag(COORDINATED_SYNC_WORK)
        activeSyncOperations.clear()
        _syncStatus.value = SyncStatus.IDLE
        Log.d(TAG, "🚫 All sync operations cancelled")
    }

    private fun createWorkRequestForTrigger(trigger: SyncTrigger, constraints: Constraints): OneTimeWorkRequest {
        return when (trigger) {
            is SyncTrigger.ManualSync -> {
                val inputData = workDataOf("SYNC_TYPE" to trigger.syncType.name)
                OneTimeWorkRequestBuilder<SyncDataWorker>()
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .addTag(COORDINATED_SYNC_WORK)
                    .build()
            }
            is SyncTrigger.FormSaved,
            is SyncTrigger.MaintenanceSaved,
            is SyncTrigger.AppStartSync,
            is SyncTrigger.PeriodicSync -> {
                OneTimeWorkRequestBuilder<SyncDataWorker>()
                    .setConstraints(constraints)
                    .addTag(COORDINATED_SYNC_WORK)
                    .build()
            }
        }
    }
}