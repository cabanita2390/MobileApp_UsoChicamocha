package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MaintenanceEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface MaintenanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maintenance: MaintenanceEntity)

    @Query("SELECT * FROM maintenance_forms WHERE isSynced = 0 AND isSyncing = 0 ORDER BY dateTime DESC")
    fun getPendingMaintenanceForms(): Flow<List<MaintenanceEntity>>

    @Query("UPDATE maintenance_forms SET isSyncing = 1 WHERE id = :id")
    suspend fun markAsSyncing(id: Int)

    @Query("UPDATE maintenance_forms SET isSyncing = 0 WHERE id = :id")
    suspend fun markAsNotSyncing(id: Int)

    @Query("UPDATE maintenance_forms SET isSyncing = 0, syncError = :error WHERE id = :id")
    suspend fun markAsSyncFailed(id: Int, error: String)

    @Query("DELETE FROM maintenance_forms WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM maintenance_forms WHERE id = :id")
    suspend fun getMaintenanceById(id: Int): MaintenanceEntity?
}
