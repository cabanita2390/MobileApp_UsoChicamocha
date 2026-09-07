package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineOilChangeEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface MachineOilChangeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(machineOilChange: MachineOilChangeEntity)

    @Query("SELECT * FROM maintenance_forms WHERE isSynced = 0 AND isSyncing = 0 ORDER BY dateTime DESC")
    fun getPendingMachineOilChangeForms(): Flow<List<MachineOilChangeEntity>>

    @Query("UPDATE maintenance_forms SET isSyncing = 1 WHERE id = :id")
    suspend fun markAsSyncing(id: Int)

    @Query("UPDATE maintenance_forms SET isSyncing = 0 WHERE id = :id")
    suspend fun markAsNotSyncing(id: Int)

    @Query("UPDATE maintenance_forms SET isSyncing = 0, syncError = :error WHERE id = :id")
    suspend fun markAsSyncFailed(id: Int, error: String)

    @Query("DELETE FROM maintenance_forms WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM maintenance_forms WHERE id = :id")
    suspend fun getMachineOilChangeById(id: Int): MachineOilChangeEntity?
}
