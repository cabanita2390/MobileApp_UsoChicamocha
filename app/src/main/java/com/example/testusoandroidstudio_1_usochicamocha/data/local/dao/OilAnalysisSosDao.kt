package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OilAnalysisSosEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OilAnalysisSosDao {

    @Insert
    suspend fun insert(entity: OilAnalysisSosEntity): Long

    @Update
    suspend fun update(entity: OilAnalysisSosEntity)

    @Query("SELECT * FROM oil_analysis_sos WHERE machine_id = :machineId ORDER BY analysis_date DESC")
    fun getByMachineId(machineId: Long): Flow<List<OilAnalysisSosEntity>>

    @Query("SELECT * FROM oil_analysis_sos WHERE machine_id = :machineId AND is_approved = 1 ORDER BY analysis_date DESC LIMIT 1")
    suspend fun getLatestApprovedByMachineId(machineId: Long): OilAnalysisSosEntity?

    @Query("SELECT * FROM oil_analysis_sos WHERE is_approved = 0 ORDER BY analysis_date DESC")
    fun getPendingApprovals(): Flow<List<OilAnalysisSosEntity>>

    @Query("SELECT * FROM oil_analysis_sos WHERE machine_id = :machineId AND is_approved = 0 ORDER BY analysis_date DESC")
    fun getPendingByMachineId(machineId: Long): Flow<List<OilAnalysisSosEntity>>

    @Query("SELECT * FROM oil_analysis_sos WHERE is_synced = 0 ORDER BY local_created_at ASC")
    fun getUnsyncedAnalyses(): Flow<List<OilAnalysisSosEntity>>

    @Query("UPDATE oil_analysis_sos SET is_synced = 1, sync_error = NULL WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE oil_analysis_sos SET sync_error = :error WHERE id = :id")
    suspend fun markSyncError(id: Long, error: String)

    @Query("UPDATE oil_analysis_sos SET is_approved = 1 WHERE id = :id")
    suspend fun markAsApproved(id: Long)
}
