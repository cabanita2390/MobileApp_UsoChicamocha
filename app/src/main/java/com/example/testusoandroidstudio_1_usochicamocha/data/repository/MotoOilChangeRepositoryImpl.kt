package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MotoOilChangeDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.VehicleOilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MotoOilChangeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MotoOilChangeRepositoryImpl @Inject constructor(
    private val dao: MotoOilChangeDao,
    private val apiService: ApiService
) : MotoOilChangeRepository {

    companion object {
        private const val TAG = "MotoOilChangeRepo"
    }

    override suspend fun saveLocally(entity: MotoOilChangeEntity): Long {
        Log.d(TAG, "Saving moto oil change locally for plate: ${entity.placa}")
        return dao.insert(entity)
    }

    override suspend fun getPendingOilChanges(): List<MotoOilChangeEntity> {
        return dao.getPendingOilChanges()
    }

    override fun getAllFlow(): Flow<List<MotoOilChangeEntity>> {
        return dao.getAllFlow()
    }

    override suspend fun syncPending(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting sync of pending moto oil changes")
            val pending = getPendingOilChanges()
            Log.d(TAG, "Found ${pending.size} pending oil changes to sync")

            if (pending.isEmpty()) {
                Log.d(TAG, "No pending oil changes to sync")
                return Result.success(Unit)
            }

            var successCount = 0
            var errorCount = 0
            var lastError: Exception? = null

            for (oilChange in pending) {
                try {
                    // Adquirir lock para evitar duplicados (retorna número de filas actualizadas)
                    val rowsUpdated = dao.acquireOilChangeLock(oilChange.localId)
                    if (rowsUpdated <= 0) {
                        Log.w(TAG, "Could not acquire lock for oil change ${oilChange.localId}, skipping")
                        continue
                    }

                    Log.d(TAG, "Syncing oil change for placa: ${oilChange.placa}")

                    // Crear request para enviar al backend
                    val request = VehicleOilChangeRequest(
                        placa = oilChange.placa,
                        dateStamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                            .format(java.util.Date(oilChange.timestamp)),
                        oilType = oilChange.oilType ?: "motor",
                        brandId = oilChange.oilBrandId,
                        quantity = oilChange.quantity,
                        kmAtChange = oilChange.kmAtChange ?: 0,
                        intervalKm = oilChange.intervalKm ?: 10000,
                        airFilterChanged = oilChange.airFilterChanged
                    )

                    // Enviar al backend
                    val response = apiService.registerMotoOilChange(oilChange.placa, request)

                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e(TAG, "Backend error (${response.code()}): $errorBody")
                        errorCount++
                        lastError = Exception("Backend rejected oil change: ${response.code()} - $errorBody")
                        throw lastError
                    }

                    // Marcar como sincronizado en la BD local
                    dao.markAsSynced(oilChange.localId)
                    successCount++
                    Log.d(TAG, "✅ Oil change ${oilChange.localId} synced successfully (ID: ${response.body()})")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error syncing oil change ${oilChange.localId}: ${e.message}", e)
                    errorCount++
                    lastError = e
                } finally {
                    // Liberar lock
                    dao.releaseOilChangeLock(oilChange.localId)
                }
            }

            Log.d(TAG, "Sync completed: $successCount synced, $errorCount failed")

            return if (errorCount > 0) {
                Result.failure(lastError ?: Exception("Some oil changes failed to sync"))
            } else {
                Result.success(Unit)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}", e)
            Result.failure(e)
        }
    }
}
