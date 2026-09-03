package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoOilChangeDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.VehicleOilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoOilChangeRepository
import kotlinx.coroutines.flow.Flow

class VehiculoOilChangeRepositoryImpl(
    private val dao: VehiculoOilChangeDao,
    private val apiService: ApiService
) : VehiculoOilChangeRepository {

    companion object {
        private const val TAG = "VehiculoOilChangeRepo"
    }

    override suspend fun saveLocally(entity: VehiculoOilChangeEntity): Long {
        Log.d(TAG, "Saving oil change locally for plate: ${entity.placa}")
        return dao.insert(entity)
    }

    override fun getAllFlow(): Flow<List<VehiculoOilChangeEntity>> = dao.getAllFlow()

    override suspend fun syncPending(): Result<Unit> {
        val pending = dao.getPending()
        Log.d(TAG, "🔄 Syncing ${pending.size} pending vehicle oil changes")
        var hasError = false

        for (item in pending) {
            val lock = dao.acquireLock(item.localId)
            if (lock == 0) continue

            try {
                val request = VehicleOilChangeRequest(
                    placa = item.placa,
                    dateStamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                        .format(java.util.Date(item.timestamp)),
                    oilType = item.oilType,
                    brandId = item.oilBrandId,
                    quantity = item.quantity,
                    kmAtChange = item.kmAtChange,
                    intervalKm = item.intervalKm,
                    airFilterChanged = item.airFilterChanged
                )
                val response = apiService.registerVehicleOilChange(request)
                if (response.isSuccessful) {
                    dao.markAsSynced(item.localId)
                    Log.d(TAG, "✅ Oil change synced for ${item.placa}")
                } else {
                    dao.markAsNotSyncing(item.localId)
                    Log.e(TAG, "❌ Error syncing oil change ${item.localId}: ${response.code()}")
                    hasError = true
                }
            } catch (e: Exception) {
                dao.markAsNotSyncing(item.localId)
                Log.e(TAG, "❌ Exception syncing oil change ${item.localId}", e)
                hasError = true
            }
        }

        return if (hasError) Result.failure(Exception("Some oil changes failed to sync")) else Result.success(Unit)
    }
}
