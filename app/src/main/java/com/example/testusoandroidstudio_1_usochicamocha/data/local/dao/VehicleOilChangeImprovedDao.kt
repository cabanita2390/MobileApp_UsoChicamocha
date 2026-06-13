package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehicleOilChangeImprovedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleOilChangeImprovedDao {

    @Insert
    suspend fun insert(entity: VehicleOilChangeImprovedEntity): Long

    @Update
    suspend fun update(entity: VehicleOilChangeImprovedEntity)

    @Query("SELECT * FROM vehicle_oil_changes_improved WHERE placa = :placa ORDER BY date_stamp DESC")
    fun getByPlaca(placa: String): Flow<List<VehicleOilChangeImprovedEntity>>

    @Query("SELECT * FROM vehicle_oil_changes_improved WHERE placa = :placa ORDER BY date_stamp DESC LIMIT 1")
    suspend fun getLatestByPlaca(placa: String): VehicleOilChangeImprovedEntity?

    @Query("SELECT * FROM vehicle_oil_changes_improved WHERE is_synced = 0 ORDER BY created_at ASC")
    fun getUnsyncedChanges(): Flow<List<VehicleOilChangeImprovedEntity>>

    @Query("UPDATE vehicle_oil_changes_improved SET is_synced = 1, sync_error = NULL WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE vehicle_oil_changes_improved SET sync_error = :error WHERE id = :id")
    suspend fun markSyncError(id: Long, error: String)

    @Query("SELECT COUNT(*) FROM vehicle_oil_changes_improved WHERE placa = :placa")
    suspend fun countByPlaca(placa: String): Int
}
