package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import kotlinx.coroutines.flow.Flow

interface VehiculoOilChangeRepository {
    suspend fun saveLocally(entity: VehiculoOilChangeEntity): Long
    fun getAllFlow(): Flow<List<VehiculoOilChangeEntity>>
    suspend fun syncPending(): Result<Unit>
}
