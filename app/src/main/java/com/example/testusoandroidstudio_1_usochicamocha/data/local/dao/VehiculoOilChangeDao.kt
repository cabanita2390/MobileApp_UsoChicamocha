package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiculoOilChangeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VehiculoOilChangeEntity): Long

    @Query("SELECT * FROM vehiculo_oil_changes ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<VehiculoOilChangeEntity>>

    @Query("SELECT * FROM vehiculo_oil_changes WHERE isSynced = 0 AND isSyncing = 0")
    suspend fun getPending(): List<VehiculoOilChangeEntity>

    @Query("UPDATE vehiculo_oil_changes SET isSynced = 1, isSyncing = 0 WHERE localId = :id")
    suspend fun markAsSynced(id: Int)

    @Query("UPDATE vehiculo_oil_changes SET isSyncing = 1 WHERE localId = :id AND isSyncing = 0 AND isSynced = 0")
    suspend fun acquireLock(id: Int): Int

    @Query("UPDATE vehiculo_oil_changes SET isSyncing = 0 WHERE localId = :id")
    suspend fun markAsNotSyncing(id: Int)
}
