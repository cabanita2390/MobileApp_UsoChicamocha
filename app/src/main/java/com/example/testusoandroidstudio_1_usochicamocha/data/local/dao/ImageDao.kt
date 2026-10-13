package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.ImageForSync
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImages(images: List<ImageEntity>)

    @Query("UPDATE pending_images SET isSynced = 1 WHERE localId = :localId")
    suspend fun markAsSynced(localId: Int)

    @Query("UPDATE pending_images SET isSyncing = 1 WHERE localId = :localId")
    suspend fun markAsSyncing(localId: Int)

    @Query("UPDATE pending_images SET isSyncing = 0 WHERE localId = :localId")
    suspend fun markAsNotSyncing(localId: Int)

    @Query("SELECT * FROM pending_images WHERE isSynced = 0")
    suspend fun getNoSincronizadas(): List<ImageEntity>

    @Query("UPDATE pending_images SET perdida = 1, isSyncing = 0 WHERE localId = :localId")
    suspend fun marcarPerdida(localId: Int)

    @Query("SELECT * FROM pending_images WHERE ejecucionUUID = :ejecucionUuid AND perdida = 1 AND isSynced = 0 ORDER BY localId")
    suspend fun getPerdidasDeEjecucion(ejecucionUuid: String): List<ImageEntity>

    /** Foto tomada de nuevo: vuelve a la cola de subida con el archivo nuevo. */
    @Query("UPDATE pending_images SET localUri = :localUri, perdida = 0, isSyncing = 0 WHERE localId = :localId")
    suspend fun reemplazarFotoPerdida(localId: Int, localUri: String)

    @Query("DELETE FROM pending_images WHERE ejecucionUUID = :ejecucionUuid AND perdida = 1 AND isSynced = 0")
    suspend fun descartarPerdidas(ejecucionUuid: String)

    @Query("UPDATE pending_images SET localUri = :localUri WHERE localId = :localId")
    suspend fun actualizarLocalUri(localId: Int, localUri: String)

    /** Locks de subida que quedaron puestos si el proceso murió subiendo (ver LiberadorLocksHuerfanos). */
    @Query("UPDATE pending_images SET isSyncing = 0 WHERE isSyncing = 1 AND isSynced = 0")
    suspend fun resetStuckSyncing()

    /**
     * CORREGIDO: Se ha renombrado la columna 'pf.serverId as formServerId'
     * a 'pf.serverId as serverId' para que coincida exactamente con la propiedad
     * en el POJO 'ImageForSync', solucionando el error de compilación de Room.
     * AÑADIDO: Filtrar imágenes que no estén siendo sincronizadas por otro worker
     * AÑADIDO (subestaciones): 3er LEFT JOIN a pending_mant_ejecucion, mismo patrón
     * que vehiculo_inspections — solo sube evidencia una vez que su ejecución padre
     * ya tiene serverId.
     * AÑADIDO: se incluye pi.tipo para que SyncPendingImagesUseCase sepa a qué
     * repositorio/endpoint despachar cada fila (antes todas se enviaban al endpoint
     * de inspección de vehículo sin importar su origen real — ver ImageEntity.tipo).
     */
    @Query("""
        SELECT
            pi.localId,
            pi.localUri,
            pi.tipo,
            COALESCE(pf.serverId, vi.serverId, ej.serverId) as serverId
        FROM pending_images pi
        LEFT JOIN pending_forms pf ON pi.formUUID = pf.UUID
        LEFT JOIN vehiculo_inspections vi ON pi.vehicleInspectionUUID = vi.UUID
        LEFT JOIN pending_mant_ejecucion ej ON pi.ejecucionUUID = ej.uuidCliente
        WHERE pi.isSynced = 0 AND pi.isSyncing = 0 AND pi.perdida = 0
          AND (
            (pf.isSynced = 1 AND pf.serverId IS NOT NULL)
            OR
            (vi.isSynced = 1 AND vi.serverId IS NOT NULL)
            OR
            (ej.isSynced = 1 AND ej.serverId IS NOT NULL)
          )
        LIMIT 10
    """)
    fun getPendingImagesForSync(): Flow<List<ImageForSync>>
}

