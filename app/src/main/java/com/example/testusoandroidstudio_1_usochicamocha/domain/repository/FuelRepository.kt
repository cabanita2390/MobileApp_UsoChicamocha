package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelLogEntity
import kotlinx.coroutines.flow.Flow

interface FuelRepository {
    suspend fun saveLocal(entity: FuelLogEntity): Long
    fun getAllFlow(): Flow<List<FuelLogEntity>>
    fun getByAssetFlow(assetType: String, assetId: Long): Flow<List<FuelLogEntity>>
    suspend fun syncPending()
    fun getPendingCount(): Flow<Int>
}
