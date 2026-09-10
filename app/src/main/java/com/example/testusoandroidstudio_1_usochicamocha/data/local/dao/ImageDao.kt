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

    /**
     * CORREGIDO: Se ha renombrado la columna 'pf.serverId as formServerId'
     * a 'pf.serverId as serverId' para que coincida exactamente con la propiedad
     * en el POJO 'ImageForSync', solucionando el error de compilación de Room.
     * AÑADIDO: Filtrar imágenes que no estén siendo sincronizadas por otro worker
     * AÑADIDO (subestaciones): 3er LEFT JOIN a pending_mant_ejecucion, mismo patrón
     * que vehiculo_inspections — solo sube evidencia una vez que su ejecución padre
     * ya tiene serverId.
     */
    @Query("""
        SELECT
            pi.localId,
            pi.localUri,
            COALESCE(pf.serverId, vi.serverId, ej.serverId) as serverId
        FROM pending_images pi
        LEFT JOIN pending_forms pf ON pi.formUUID = pf.UUID
        LEFT JOIN vehiculo_inspections vi ON pi.vehicleInspectionUUID = vi.UUID
        LEFT JOIN pending_mant_ejecucion ej ON pi.ejecucionUUID = ej.uuidCliente
        WHERE pi.isSynced = 0 AND pi.isSyncing = 0
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

