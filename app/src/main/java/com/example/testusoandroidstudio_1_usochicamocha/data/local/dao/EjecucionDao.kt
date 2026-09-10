package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity
import kotlinx.coroutines.flow.Flow

/** Fila de `getColaFlow()` con el conteo de fotos ya resuelto por SQL (subquery a `pending_images`). */
data class EjecucionConFotos(
    @Embedded val entity: EjecucionEntity,
    val fotosCount: Int
)

@Dao
interface EjecucionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEjecucion(ejecucion: EjecucionEntity)

    @Query("SELECT * FROM pending_mant_ejecucion WHERE uuidCliente = :uuid")
    suspend fun getByUuid(uuid: String): EjecucionEntity?

    @Query("UPDATE pending_mant_ejecucion SET isSynced = 1, serverId = :serverId, isSyncing = 0 WHERE uuidCliente = :uuid")
    suspend fun markAsSynced(uuid: String, serverId: Long)

    /**
     * ATOMIC LOCK: 1 si obtuvo el lock, 0 si ya estaba siendo sincronizado. Mismo
     * patrón que FormDao.acquireFormLock. También limpia `syncFallido` — un nuevo
     * intento de envío borra el error previo hasta que (si acaso) vuelva a fallar.
     */
    @Query("UPDATE pending_mant_ejecucion SET isSyncing = 1, syncFallido = 0 WHERE uuidCliente = :uuid AND isSyncing = 0 AND isSynced = 0")
    suspend fun acquireLock(uuid: String): Int

    @Query("UPDATE pending_mant_ejecucion SET isSyncing = 0 WHERE uuidCliente = :uuid")
    suspend fun markAsNotSyncing(uuid: String)

    /** Fallo real de envío (respuesta no exitosa / excepción) — distinto de "todavía sin intentar". */
    @Query("UPDATE pending_mant_ejecucion SET isSyncing = 0, syncFallido = 1 WHERE uuidCliente = :uuid")
    suspend fun markSyncFailed(uuid: String)

    @Query("SELECT 1 FROM pending_mant_ejecucion WHERE uuidCliente = :uuid AND isSynced = 1 LIMIT 1")
    suspend fun isAlreadySynced(uuid: String): Boolean?

    @Query("UPDATE pending_mant_ejecucion SET isSyncing = 0 WHERE isSyncing = 1")
    suspend fun resetStuckSyncing()

    @Query("SELECT * FROM pending_mant_ejecucion WHERE isSynced = 0 AND isSyncing = 0 ORDER BY fecha DESC")
    fun getPendingFlow(): Flow<List<EjecucionEntity>>

    /**
     * Para la pantalla Cola: a diferencia de `getPendingFlow()` (usada por el worker
     * y el badge del header, que solo necesitan "qué falta subir"), esta SÍ incluye
     * los registros con `isSyncing=1` — así el técnico ve "Enviando…" en vez de que
     * el registro desaparezca de la lista mientras se sube.
     */
    @Query("""
        SELECT e.*, (SELECT COUNT(*) FROM pending_images i WHERE i.ejecucionUUID = e.uuidCliente) AS fotosCount
        FROM pending_mant_ejecucion e
        WHERE e.isSynced = 0
        ORDER BY e.fecha DESC
    """)
    fun getColaFlow(): Flow<List<EjecucionConFotos>>
}
