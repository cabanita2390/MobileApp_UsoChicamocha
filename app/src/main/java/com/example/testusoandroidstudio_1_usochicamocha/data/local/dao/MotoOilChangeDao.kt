package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MotoOilChangeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(oilChange: MotoOilChangeEntity): Long

    @Query("SELECT * FROM moto_oil_changes WHERE isSynced = 0 AND isSyncing = 0 ORDER BY timestamp DESC")
    suspend fun getPendingOilChanges(): List<MotoOilChangeEntity>

    @Query("SELECT * FROM moto_oil_changes ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<MotoOilChangeEntity>>

    @Query("UPDATE moto_oil_changes SET isSynced = 1, isSyncing = 0 WHERE localId = :localId")
    suspend fun markAsSynced(localId: Int)

    @Query("UPDATE moto_oil_changes SET isSyncing = 1 WHERE localId = :localId AND isSyncing = 0 AND isSynced = 0")
    suspend fun acquireOilChangeLock(localId: Int): Int

    @Query("UPDATE moto_oil_changes SET isSyncing = 0 WHERE localId = :localId")
    suspend fun releaseOilChangeLock(localId: Int)

    @Query("DELETE FROM moto_oil_changes WHERE localId = :localId")
    suspend fun delete(localId: Int)
}
