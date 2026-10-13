package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import kotlinx.coroutines.flow.Flow

interface VehiculoOilChangeRepository {
    suspend fun saveLocally(entity: VehiculoOilChangeEntity): Long
    fun getAllFlow(): Flow<List<VehiculoOilChangeEntity>>
    suspend fun syncPending(): Result<Unit>

    /** Borra un cambio de aceite que todavía no se envió (p. ej. uno viejo sin cantidad, que el backend rechaza). */
    suspend fun deletePending(localId: Int)
}
