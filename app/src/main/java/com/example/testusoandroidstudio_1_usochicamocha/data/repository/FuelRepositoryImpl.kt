package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FuelLogDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelLogEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.FuelLogRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FuelRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

class FuelRepositoryImpl(
    private val dao: FuelLogDao,
    private val api: ApiService
) : FuelRepository {

    override suspend fun saveLocal(entity: FuelLogEntity): Long = dao.insert(entity)

    override fun getAllFlow(): Flow<List<FuelLogEntity>> = dao.getAllFlow()

    override fun getByAssetFlow(assetType: String, assetId: Long): Flow<List<FuelLogEntity>> =
        dao.getByAssetFlow(assetType, assetId)

    override fun getPendingCount(): Flow<Int> = dao.getPendingCount()

    override suspend fun syncPending() {
        val pending = dao.getPending()
        Log.d("FuelRepository", "🔄 Syncing ${pending.size} pending fuel logs")

        for (entity in pending) {
            val locked = dao.acquireLock(entity.localId)
            if (locked == 0) continue

            try {
                // Prepare invoice photo: if local path exists, convert to Base64
                var invoicePhotoBase64: String? = null
                var invoiceFileName: String? = null
                if (!entity.invoicePhotoPath.isNullOrBlank()) {
                    val file = File(entity.invoicePhotoPath)
                    if (file.exists()) {
                        invoicePhotoBase64 = file.readBytes().let {
                            java.util.Base64.getEncoder().encodeToString(it)
                        }
                        invoiceFileName = file.name
                        Log.d("FuelRepository", "📸 Invoice photo converted to Base64 for fuel log ${entity.localId}")
                    }
                }

                val request = FuelLogRequest(
                    syncId = entity.syncId,
                    assetType = entity.assetType,
                    assetId = entity.assetId,
                    assetPlate = entity.assetPlate,
                    fuelDateTime = entity.fuelDateTime,
                    odometerKm = entity.odometerKm,
                    hourMeter = entity.hourMeter,
                    quantity = entity.quantity,
                    quantityUnit = entity.quantityUnit,
                    pricePerUnit = entity.pricePerUnit,
                    totalCostActual = entity.totalCostActual,
                    fuelType = entity.fuelType,
                    serviceStation = entity.serviceStation,
                    discountAmount = entity.discountAmount,
                    invoicePhotoUrl = entity.invoicePhotoUrl,
                    invoicePhotoBase64 = invoicePhotoBase64,
                    invoiceFileName = invoiceFileName,
                    voucherNumber = entity.voucherNumber,
                    notes = entity.notes
                )
                val response = api.registerFuelLog(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    val remoteId = body?.id ?: -1L
                    dao.markAsSynced(
                        id = entity.localId,
                        remoteId = remoteId,
                        efficiencyValue = body?.efficiencyValue,
                        efficiencyUnit = body?.efficiencyUnit,
                        isAnomaly = body?.isAnomaly ?: false
                    )
                    Log.d("FuelRepository", "✅ Fuel log ${entity.localId} synced → remoteId=$remoteId" +
                            " efficiency=${body?.efficiencyValue} ${body?.efficiencyUnit}" +
                            " anomaly=${body?.isAnomaly}")
                } else {
                    dao.releaseLock(entity.localId)
                    Log.w("FuelRepository", "⚠️ Fuel log ${entity.localId} sync failed: ${response.code()}")
                }
            } catch (e: Exception) {
                dao.releaseLock(entity.localId)
                Log.e("FuelRepository", "❌ Error syncing fuel log ${entity.localId}", e)
            }
        }
    }

}
