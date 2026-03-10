package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.InspeccionMotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspeccionMotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspeccion(entity: InspeccionMotoEntity)

    /** Retorna las inspecciones pendientes de sincronizar (no bloqueadas por otro proceso) */
    @Query("SELECT * FROM pending_inspecciones_moto WHERE isSynced = 0 AND isSyncing = 0 ORDER BY timestamp ASC")
    fun getPendingInspecciones(): Flow<List<InspeccionMotoEntity>>

    /** Versión suspend para el SyncDataWorker */
    @Query("SELECT * FROM pending_inspecciones_moto WHERE isSynced = 0 AND isSyncing = 0 ORDER BY timestamp ASC")
    suspend fun getPendingInspeccionesList(): List<InspeccionMotoEntity>

    /**
     * Lock atómico: marca la inspección como sincronizando SOLO si no está ya en proceso.
     * @return 1 si obtuvo el lock, 0 si ya estaba siendo sincronizada
     */
    @Query("UPDATE pending_inspecciones_moto SET isSyncing = 1 WHERE uuid = :uuid AND isSyncing = 0 AND isSynced = 0")
    suspend fun acquireLock(uuid: String): Int

    /** Libera el lock atómico */
    @Query("UPDATE pending_inspecciones_moto SET isSyncing = 0 WHERE uuid = :uuid")
    suspend fun releaseLock(uuid: String)

    /** Marca la inspección como sincronizada con éxito */
    @Query("UPDATE pending_inspecciones_moto SET isSynced = 1, serverId = :serverId, isSyncing = 0 WHERE uuid = :uuid")
    suspend fun markAsSynced(uuid: String, serverId: Long)

    /** Verifica si una inspección ya fue sincronizada */
    @Query("SELECT 1 FROM pending_inspecciones_moto WHERE uuid = :uuid AND isSynced = 1 LIMIT 1")
    suspend fun isAlreadySynced(uuid: String): Boolean?

    /** Limpia locks colgados (inspecciones que quedaron en isSyncing = 1 por crash) */
    @Query("UPDATE pending_inspecciones_moto SET isSyncing = 0 WHERE isSyncing = 1")
    suspend fun resetStuckSyncing()
}
