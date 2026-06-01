package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FuelLogEntity): Long

    @Query("SELECT * FROM fuel_logs_local ORDER BY fuelDateTime DESC, createdAt DESC")
    fun getAllFlow(): Flow<List<FuelLogEntity>>

    @Query("SELECT * FROM fuel_logs_local WHERE assetType = :assetType AND assetId = :assetId ORDER BY fuelDateTime DESC")
    fun getByAssetFlow(assetType: String, assetId: Long): Flow<List<FuelLogEntity>>

    @Query("SELECT * FROM fuel_logs_local WHERE isSynced = 0 AND isSyncing = 0")
    suspend fun getPending(): List<FuelLogEntity>

    @Query("UPDATE fuel_logs_local SET remoteId = :remoteId, efficiencyValue = :efficiencyValue, efficiencyUnit = :efficiencyUnit, isAnomaly = :isAnomaly, isSynced = 1, isSyncing = 0 WHERE localId = :id")
    suspend fun markAsSynced(id: Int, remoteId: Long, efficiencyValue: Double?, efficiencyUnit: String?, isAnomaly: Boolean)

    @Query("UPDATE fuel_logs_local SET isSyncing = 1 WHERE localId = :id AND isSyncing = 0 AND isSynced = 0")
    suspend fun acquireLock(id: Int): Int

    @Query("UPDATE fuel_logs_local SET isSyncing = 0 WHERE localId = :id")
    suspend fun releaseLock(id: Int)

    @Query("SELECT COUNT(*) FROM fuel_logs_local WHERE isSynced = 0")
    fun getPendingCount(): Flow<Int>
}
