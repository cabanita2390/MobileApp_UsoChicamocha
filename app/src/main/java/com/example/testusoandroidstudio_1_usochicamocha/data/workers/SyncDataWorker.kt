package com.example.testusoandroidstudio_1_usochicamocha.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.SessionStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.auth.ValidateSessionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.GetPendingFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.SyncMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange.GetPendingMachineOilChangeFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange.SyncMachineOilChangeFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncUbicacionesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncDocumentosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.GetPendingInspeccionesMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SyncInspeccionMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.GetPendingVehiculoInspectionsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SyncVehiculoInspectionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SyncVehiclesCatalogUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

/**
 * Worker para sincronización de datos que procesa formularios, mantenimientos, imágenes y datos maestros.
 * Corregido para evitar duplicaciones y loops recursivos.
 */
@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val getPendingFormsUseCase: GetPendingFormsUseCase,
    private val syncFormUseCase: SyncFormUseCase,
    private val getPendingMachineOilChangeFormsUseCase: GetPendingMachineOilChangeFormsUseCase,
    private val syncMachineOilChangeFormsUseCase: SyncMachineOilChangeFormsUseCase,
    private val syncMachinesUseCase: SyncMachinesUseCase,
    private val syncOilsUseCase: SyncOilsUseCase,
    private val syncMotosUseCase: SyncMotosUseCase,
    private val syncUbicacionesUseCase: SyncUbicacionesUseCase,
    private val syncDocumentosUseCase: SyncDocumentosUseCase,
    private val getPendingInspeccionesMotoUseCase: GetPendingInspeccionesMotoUseCase,
    private val syncInspeccionMotoUseCase: SyncInspeccionMotoUseCase,
    private val getPendingVehiculoInspectionsUseCase: GetPendingVehiculoInspectionsUseCase,
    private val syncVehiculoInspectionUseCase: SyncVehiculoInspectionUseCase,
    private val syncVehiclesCatalogUseCase: SyncVehiclesCatalogUseCase,
    private val vehiculoRepository: VehiculoInspectionRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val syncType = inputData.getString("SYNC_TYPE") ?: "ALL_DATA"
        val workId = id.toString().take(8)
        
        try {
            Log.d("SyncDataWorker", "🔄 [$workId] === SYNC SESSION START - Type: $syncType ===")
            
            // 1. Validar sesión con timeout
            try {
                val sessionStatus = withTimeout(10000) {
                    validateSessionUseCase()
                }
                if (sessionStatus == SessionStatus.EXPIRED) {
                    Log.d("SyncDataWorker", "🔐 [$workId] Sesión expirada. No se puede sincronizar.")
                    return Result.failure()
                }
            } catch (e: Exception) {
                Log.w("SyncDataWorker", "⚠️ [$workId] Timeout validando sesión, continuando...")
            }

            // Determine what to sync based on SyncType
            val syncAll = syncType.isNullOrEmpty() || syncType == "ALL_DATA"
            val syncFormsOnly = syncType == "FORMS_ONLY"
            val syncMachineOilChangeOnly = syncType == "MACHINE_OIL_CHANGE_ONLY"
            val syncImagesOnly = syncType == "IMAGES_ONLY"
            val syncMachinesOnly = syncType == "MACHINES_ONLY"
            val syncOilsOnly = syncType == "OILS_ONLY"
            val syncMotosOnly = syncType == "MOTOS_ONLY"
            val syncUbicacionesOnly = syncType == "UBICACIONES_ONLY"
            val syncDocumentsOnly = syncType == "DOCUMENTS_ONLY"
            val syncMasterDataOnly = syncType == "MASTER_DATA"
            val syncVehiclesOnly = syncType == "VEHICLES_ONLY"
            val syncVehiclesCatalogOnly = syncType == "VEHICLES_CATALOG"
            val syncMotosPendingOnly = syncType == "MOTOS_PENDING"
            val syncVehiclesPendingOnly = syncType == "VEHICLES_PENDING"
            val syncVehiclesDocumentsOnly = syncType == "VEHICLES_DOCUMENTS"

            val shouldSyncForms = syncAll || syncFormsOnly
            val shouldSyncMachineOilChange = syncAll || syncMachineOilChangeOnly
            val shouldSyncVehicles = syncAll || syncVehiclesOnly || syncFormsOnly || syncVehiclesCatalogOnly || syncVehiclesPendingOnly || syncVehiclesDocumentsOnly
            val shouldSyncMotosPending = syncAll || syncFormsOnly || syncMotosPendingOnly
            
            var formsSyncedCount = 0
            var machineOilChangeSyncedCount = 0
            var motoSyncedCount = 0
            var vehiclesSynced = 0
            var totalErrors = 0
            var pendingForms: List<com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form> = emptyList()
            var pendingMachineOilChange: List<com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm> = emptyList()
            var pendingInspMoto: List<com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente> = emptyList()
            var pendingVehicles: List<com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity> = emptyList()

            // 2. FORMULARIOS con timeout por cada formulario
            if (shouldSyncForms) {
                Log.d("SyncDataWorker", "📝 [$workId] Processing forms...")
                try {
                    pendingForms = withTimeout(30000) {
                        getPendingFormsUseCase().first()
                    }
                    Log.d("SyncDataWorker", "📋 [$workId] Found ${pendingForms.size} pending forms to sync")
                    
                    if (pendingForms.isNotEmpty()) {
                        pendingForms.forEachIndexed { index, form ->
                            try {
                                Log.d("SyncDataWorker", "📝 [$workId] Syncing form ${index + 1}/${pendingForms.size}: ${form.UUID}")
                                
                                val result = withTimeout(30000) {
                                    syncFormUseCase(form)
                                }
                                
                                if (result.isSuccess) {
                                    formsSyncedCount++
                                    Log.d("SyncDataWorker", "✅ [$workId] Form synced successfully: ${form.UUID}")
                                } else {
                                    totalErrors++
                                    Log.e("SyncDataWorker", "❌ [$workId] Form sync failed: ${form.UUID} - ${result.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                totalErrors++
                                Log.e("SyncDataWorker", "❌ [$workId] Exception syncing form ${form.UUID}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    totalErrors++
                    Log.e("SyncDataWorker", "❌ [$workId] Error fetching forms", e)
                }
            }

            // 3. CAMBIOS DE ACEITE DE MAQUINARIA con timeout por cada cambio
            if (shouldSyncMachineOilChange) {
                Log.d("SyncDataWorker", "🔧 [$workId] Processing machineOilChange...")
                try {
                    pendingMachineOilChange = withTimeout(30000) {
                        getPendingMachineOilChangeFormsUseCase().first()
                    }
                    Log.d("SyncDataWorker", "🛠️ [$workId] Found ${pendingMachineOilChange.size} pending machineOilChange forms to sync")
                    
                    if (pendingMachineOilChange.isNotEmpty()) {
                        pendingMachineOilChange.forEachIndexed { index, machineOilChange ->
                            try {
                                Log.d("SyncDataWorker", "🔧 [$workId] Syncing machineOilChange ${index + 1}/${pendingMachineOilChange.size}: ${machineOilChange.id}")
                                
                                val result = withTimeout(30000) {
                                    syncMachineOilChangeFormsUseCase(machineOilChange)
                                }
                                
                                if (result.isSuccess) {
                                    machineOilChangeSyncedCount++
                                    Log.d("SyncDataWorker", "✅ [$workId] MachineOilChangeForm synced successfully: ${machineOilChange.id}")
                                } else {
                                    totalErrors++
                                    Log.e("SyncDataWorker", "❌ [$workId] MachineOilChangeForm sync failed: ${machineOilChange.id} - ${result.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                totalErrors++
                                Log.e("SyncDataWorker", "❌ [$workId] Exception syncing machineOilChange ${machineOilChange.id}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    totalErrors++
                    Log.e("SyncDataWorker", "❌ [$workId] Error fetching machineOilChange forms", e)
                }
            }

            // 3.5 INSPECCIONES MOTO pendientes
            if (shouldSyncMotosPending) {
                Log.d("SyncDataWorker", "🏍️ [$workId] Procesando inspecciones de moto pendientes...")
                try {
                    pendingInspMoto = getPendingInspeccionesMotoUseCase.asList()
                    Log.d("SyncDataWorker", "🏍️ [$workId] ${pendingInspMoto.size} inspecciones moto pendientes encontradas")
                    
                    if (pendingInspMoto.isNotEmpty()) {
                        Log.d("SyncDataWorker", "🏍️ [$workId] --- INICIO BUCLE SINCRONIZACIÓN MOTO ---")
                        pendingInspMoto.forEachIndexed { index, inspeccion ->
                            try {
                                Log.d("SyncDataWorker", "🏍️ [$workId] Sincronizando inspección moto ${index + 1}/${pendingInspMoto.size}: ${inspeccion.uuid}")
                                val result = withTimeout(30000) {
                                    syncInspeccionMotoUseCase(inspeccion)
                                }
                                if (result.isSuccess) {
                                    motoSyncedCount++
                                    Log.d("SyncDataWorker", "✅ [$workId] Inspección moto sincronizada con éxito: ${inspeccion.uuid}")
                                } else {
                                    totalErrors++
                                    Log.e("SyncDataWorker", "❌ [$workId] Fallo al sincronizar inspección moto: ${inspeccion.uuid} - ${result.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                totalErrors++
                                Log.e("SyncDataWorker", "❌ [$workId] Excepción sincronizando inspección moto ${inspeccion.uuid}", e)
                            }
                        }
                        Log.d("SyncDataWorker", "🏍️ [$workId] --- FIN BUCLE SINCRONIZACIÓN MOTO (Sincronizadas: $motoSyncedCount, Errores: $totalErrors) ---")
                    } else {
                        Log.d("SyncDataWorker", "🏍️ [$workId] No hay inspecciones de moto pendientes por sincronizar.")
                    }
                } catch (e: Exception) {
                    totalErrors++
                    Log.e("SyncDataWorker", "❌ [$workId] Error al obtener inspecciones moto pendientes", e)
                }
            }

            // 4. INSPECCIONES DE VEHÍCULOS
            if (shouldSyncVehicles) {
                Log.d("SyncDataWorker", "🚗 [$workId] Processing vehicle inspections...")
                try {
                    pendingVehicles = withTimeout(30000) {
                        getPendingVehiculoInspectionsUseCase()
                    }
                    Log.d("SyncDataWorker", "📋 [$workId] Found ${pendingVehicles.size} pending vehicle inspections to sync")
                    
                    if (pendingVehicles.isNotEmpty()) {
                        pendingVehicles.forEachIndexed { index, inspection ->
                            try {
                                Log.d("SyncDataWorker", "🚗 [$workId] Syncing vehicle inspection ${index + 1}/${pendingVehicles.size}: ${inspection.UUID}")
                                
                                val result = withTimeout(30000) {
                                    syncVehiculoInspectionUseCase(inspection)
                                }
                                
                                if (result.isSuccess) {
                                    vehiclesSynced++
                                    Log.d("SyncDataWorker", "✅ [$workId] Vehicle inspection synced successfully: ${inspection.UUID}")
                                } else {
                                    totalErrors++
                                    Log.e("SyncDataWorker", "❌ [$workId] Vehicle inspection sync failed: ${inspection.UUID} - ${result.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                totalErrors++
                                Log.e("SyncDataWorker", "❌ [$workId] Exception syncing vehicle inspection ${inspection.UUID}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    totalErrors++
                    Log.e("SyncDataWorker", "❌ [$workId] Error fetching vehicle inspections", e)
                }
            }

            // 5. IMÁGENES con timeout
            if (shouldSyncForms || shouldSyncMachineOilChange || shouldSyncVehicles || syncImagesOnly) {
                try {
                    Log.d("SyncDataWorker", "🖼️ [$workId] Enqueuing image sync...")
                    val imageWork = OneTimeWorkRequestBuilder<ImageSyncWorker>().build()
                    WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                        "chained_image_sync_$workId",
                        ExistingWorkPolicy.KEEP,
                        imageWork
                    )
                    Log.d("SyncDataWorker", "✅ [$workId] Image sync worker enqueued")
                } catch (e: Exception) {
                    totalErrors++
                    Log.e("SyncDataWorker", "❌ [$workId] Failed to enqueue image sync worker", e)
                }
            }

            // 5. DATOS MAESTROS con timeout
            // La descarga del catálogo (máquinas, vehículos, motos) es independiente de si hay
            // formularios pendientes de subir. Siempre debe ejecutarse para que el dispositivo
            // reciba los activos nuevos creados desde el admin web.
            val isExplicitMasterSync = syncMasterDataOnly || syncMachinesOnly || syncOilsOnly || syncMotosOnly || syncUbicacionesOnly || syncDocumentsOnly || syncVehiclesCatalogOnly || syncVehiclesDocumentsOnly

            if (isExplicitMasterSync || syncAll) {
                Log.d("SyncDataWorker", "⚙️ [$workId] Syncing master data... Type: $syncType")
                try {
                    if (syncMachinesOnly) {
                        withTimeout(60000) {
                            syncMachinesUseCase()
                        }
                        Log.d("SyncDataWorker", "✅ [$workId] Machines synced successfully")
                    } else if (syncOilsOnly) {
                        withTimeout(60000) {
                            syncOilsUseCase()
                        }
                        Log.d("SyncDataWorker", "✅ [$workId] Oils synced successfully")
                    } else if (syncMotosOnly) {
                        withTimeout(60000) {
                            syncMotosUseCase()
                        }
                        Log.d("SyncDataWorker", "✅ [$workId] Motos synced successfully")
                    } else if (syncUbicacionesOnly) {
                        withTimeout(60000) {
                            syncUbicacionesUseCase()
                        }
                        Log.d("SyncDataWorker", "✅ [$workId] Ubicaciones synced successfully")
                    } else if (syncDocumentsOnly) {
                        withTimeout(120000) {
                            syncDocumentosUseCase()
                        }
                        Log.d("SyncDataWorker", "✅ [$workId] Moto Documents synced successfully")
                    } else if (syncVehiclesCatalogOnly) {
                        withTimeout(60000) {
                            syncVehiclesCatalogUseCase()
                        }
                        Log.d("SyncDataWorker", "✅ [$workId] Vehicles Catalog synced successfully")
                    } else if (syncVehiclesDocumentsOnly) {
                        withTimeout(180000) {
                            vehiculoRepository.syncAllVehiclesDocuments()
                        }
                        Log.d("SyncDataWorker", "✅ [$workId] Vehicles Documents synced successfully")
                    } else {
                        // Por defecto: sincroniza todo (MASTER_DATA o ALL_DATA)
                        withTimeout(270000) {
                            syncMachinesUseCase()
                            syncOilsUseCase()
                            syncMotosUseCase()
                            syncUbicacionesUseCase()
                            syncVehiclesCatalogUseCase()
                            vehiculoRepository.syncAllVehiclesDocuments()
                            syncDocumentosUseCase() // Moto documents
                        }
                        Log.d("SyncDataWorker", "✅ [$workId] Master data (Machines, Oils, Motos, Ubicaciones, Vehicles, Documents) synced successfully")
                    }
                } catch (e: Exception) {
                    totalErrors++
                    Log.e("SyncDataWorker", "❌ [$workId] Error syncing master data", e)
                }
            }

            // Log summary
            Log.d("SyncDataWorker", "🏁 [$workId] === SYNC SESSION COMPLETE ===")
            Log.d("SyncDataWorker", "📊 [$workId] Summary - Forms: $formsSyncedCount, MachineOilChangeForm: $machineOilChangeSyncedCount, Motos: $motoSyncedCount, Vehicles: $vehiclesSynced, Errors: $totalErrors")
            
            // 6. Summary y Result
            val hasDataToProcess = pendingForms.isNotEmpty() || 
                                  pendingMachineOilChange.isNotEmpty() || 
                                  pendingInspMoto.isNotEmpty() ||
                                  pendingVehicles.isNotEmpty() ||
                                  isExplicitMasterSync
            
            return if (totalErrors == 0 && hasDataToProcess) {
                Log.d("SyncDataWorker", "🎉 [$workId] All sync operations completed successfully")
                Result.success()
            } else if (totalErrors > 0 && hasDataToProcess) {
                Log.w("SyncDataWorker", "⚠️ [$workId] Some sync operations failed, but session completed")
                Result.success() // Success para no entrar en retry loop
            } else {
                Log.d("SyncDataWorker", "✅ [$workId] No data to sync or completed with some errors")
                Result.success()
            }

        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("SyncDataWorker", "⏰ [$workId] Sync session timed out", e)
            return Result.failure()
        } catch (e: Exception) {
            Log.e("SyncDataWorker", "💥 [$workId] Critical error during sync session: ${e.message}", e)
            return Result.failure()
        } finally {
            Log.d("SyncDataWorker", "🔚 [$workId] Sync session cleanup completed")
        }
    }
}
