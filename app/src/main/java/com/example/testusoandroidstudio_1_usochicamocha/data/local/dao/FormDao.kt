package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.PendingFormWithImageCount
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: FormEntity)

    @Query("SELECT * FROM pending_forms WHERE uuid = :uuid")
    suspend fun getFormByUuid(uuid: String): FormEntity?

    @Query("UPDATE pending_forms SET isSynced = 1, serverId = :serverId, isSyncing = 0 WHERE uuid = :uuid")
    suspend fun markAsSynced(uuid: String, serverId: Long)

    /**
     * ATOMIC LOCK: Marca un formulario como sincronizando SOLO si no está ya sincronizando
     * @return 1 si obtuvo el lock exitosamente, 0 si ya estaba siendo sincronizado
     */
    @Query("UPDATE pending_forms SET isSyncing = 1 WHERE uuid = :uuid AND isSyncing = 0 AND isSynced = 0")
    suspend fun acquireFormLock(uuid: String): Int

    /**
     * Release del lock atómico
     */
    @Query("UPDATE pending_forms SET isSyncing = 0 WHERE uuid = :uuid")
    suspend fun markAsNotSyncing(uuid: String)

    /**
     * Verifica si un formulario ya está sincronizado
     * @return true si está sincronizado, false si no está sincronizado, null si el formulario no existe
     */
    @Query("SELECT 1 FROM pending_forms WHERE uuid = :uuid AND isSynced = 1 LIMIT 1")
    suspend fun isFormAlreadySynced(uuid: String): Boolean?

    /**
     * Limpia locks colgados (formularios que quedaron en estado isSyncing = 1)
     */
    @Query("UPDATE pending_forms SET isSyncing = 0 WHERE isSyncing = 1")
    suspend fun resetStuckSyncingForms()

    /**
     * AÑADIDO: Se restaura esta función para que el FormRepositoryImpl pueda
     * cumplir con el método getPendingForms() de su interfaz. Esta consulta es
     * usada por el SyncDataWorker para obtener los formularios de texto a sincronizar.
     */
    @Query("SELECT * FROM pending_forms WHERE isSynced = 0 AND isSyncing = 0 AND (propietarioId IS NULL OR propietarioId = (SELECT usuarioId FROM sesion_actual WHERE id = 1)) ORDER BY timestamp DESC")
    fun getPendingFormsFlow(): Flow<List<FormEntity>>

    @Query("""
        SELECT 
            pf.*, 
            COUNT(pi.localId) as totalImageCount, 
            SUM(CASE WHEN pi.isSynced = 1 THEN 1 ELSE 0 END) as syncedImageCount
        FROM pending_forms pf
        LEFT JOIN pending_images pi ON pf.UUID = pi.formUUID
        GROUP BY pf.localId
        HAVING (pf.isSynced = 0 AND pf.isSyncing = 0) OR (COUNT(pi.localId) > SUM(CASE WHEN pi.isSynced = 1 THEN 1 ELSE 0 END))
        ORDER BY pf.timestamp DESC
    """)
    fun getPendingFormsWithImageCount(): Flow<List<PendingFormWithImageCount>>
}
