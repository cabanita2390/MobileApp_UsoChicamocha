package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiculoInspectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: VehiculoInspectionEntity): Long

    @Query("SELECT * FROM vehiculo_inspections WHERE isSynced = 0 AND isSyncing = 0")
    suspend fun getPendingInspections(): List<VehiculoInspectionEntity>

    @Query("SELECT * FROM vehiculo_inspections ORDER BY timestamp DESC")
    fun getAllInspectionsFlow(): Flow<List<VehiculoInspectionEntity>>

    @Query("UPDATE vehiculo_inspections SET isSynced = 1, serverId = :serverId, isSyncing = 0 WHERE UUID = :uuid")
    suspend fun markAsSynced(uuid: String, serverId: Long)

    @Query("UPDATE vehiculo_inspections SET isSyncing = 1 WHERE UUID = :uuid")
    suspend fun markAsSyncing(uuid: String)

    @Query("UPDATE vehiculo_inspections SET isSyncing = 0 WHERE UUID = :uuid")
    suspend fun markAsNotSyncing(uuid: String)

    @Query("SELECT isSynced FROM vehiculo_inspections WHERE UUID = :uuid")
    suspend fun isInspectionAlreadySynced(uuid: String): Boolean?

    @Query("UPDATE vehiculo_inspections SET isSyncing = 1 WHERE UUID = :uuid AND isSyncing = 0 AND isSynced = 0")
    suspend fun acquireInspectionLock(uuid: String): Int
}
