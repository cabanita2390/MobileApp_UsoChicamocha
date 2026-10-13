package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity
import kotlinx.coroutines.flow.Flow

interface MotoOilChangeRepository {
    suspend fun saveLocally(entity: MotoOilChangeEntity): Long
    suspend fun getPendingOilChanges(): List<MotoOilChangeEntity>
    fun getAllFlow(): Flow<List<MotoOilChangeEntity>>
    suspend fun syncPending(): Result<Unit>

    /** Borra un cambio de aceite que todavía no se envió (p. ej. uno viejo sin cantidad, que el backend rechaza). */
    suspend fun deletePending(localId: Int)
}
