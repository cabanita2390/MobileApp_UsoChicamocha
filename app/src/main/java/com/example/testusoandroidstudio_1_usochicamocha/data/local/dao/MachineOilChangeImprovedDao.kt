package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineOilChangeImprovedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineOilChangeImprovedDao {

    @Insert
    suspend fun insert(entity: MachineOilChangeImprovedEntity): Long

    @Update
    suspend fun update(entity: MachineOilChangeImprovedEntity)

    @Query("SELECT * FROM machine_oil_changes_improved WHERE machine_id = :machineId ORDER BY date_stamp DESC")
    fun getByMachineId(machineId: Long): Flow<List<MachineOilChangeImprovedEntity>>

    @Query("SELECT * FROM machine_oil_changes_improved WHERE machine_id = :machineId ORDER BY date_stamp DESC LIMIT 1")
    suspend fun getLatestByMachineId(machineId: Long): MachineOilChangeImprovedEntity?

    @Query("SELECT * FROM machine_oil_changes_improved WHERE is_synced = 0 ORDER BY created_at ASC")
    fun getUnsyncedChanges(): Flow<List<MachineOilChangeImprovedEntity>>

    @Query("UPDATE machine_oil_changes_improved SET is_synced = 1, sync_error = NULL WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE machine_oil_changes_improved SET sync_error = :error WHERE id = :id")
    suspend fun markSyncError(id: Long, error: String)

    @Query("SELECT COUNT(*) FROM machine_oil_changes_improved WHERE machine_id = :machineId")
    suspend fun countByMachineId(machineId: Long): Int
}
