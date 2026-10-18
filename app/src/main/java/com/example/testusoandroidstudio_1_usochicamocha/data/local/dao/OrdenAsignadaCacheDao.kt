package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.OrdenAsignadaCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdenAsignadaCacheDao {

    /** Las del usuario en sesión: primero prioridad alta, luego la que vence antes. */
    @Query(
        "SELECT * FROM mant_orden_asignada_cache WHERE usuarioId = (SELECT usuarioId FROM sesion_actual WHERE id = 1) " +
            "ORDER BY CASE prioridad WHEN 'ALTA' THEN 0 ELSE 1 END, fechaLimite, id"
    )
    fun delUsuarioFlow(): Flow<List<OrdenAsignadaCacheEntity>>

    @Query("SELECT * FROM mant_orden_asignada_cache WHERE id = :id")
    suspend fun getById(id: Long): OrdenAsignadaCacheEntity?

    @Query("SELECT usuarioId FROM sesion_actual WHERE id = 1")
    suspend fun usuarioEnSesion(): Long?

    /** Ya se registró en el teléfono: sale de la lista sin esperar a que el servidor responda. */
    @Query("DELETE FROM mant_orden_asignada_cache WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM mant_orden_asignada_cache WHERE usuarioId = :usuarioId")
    suspend fun deleteDelUsuario(usuarioId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ordenes: List<OrdenAsignadaCacheEntity>)

    /**
     * Reemplaza las del usuario con la respuesta del servidor, sin volver a mostrar las que ya se
     * registraron en el teléfono y todavía no se envían (`excluir`).
     */
    @Transaction
    suspend fun reemplazar(usuarioId: Long, ordenes: List<OrdenAsignadaCacheEntity>, excluir: Set<Long>) {
        deleteDelUsuario(usuarioId)
        insertAll(ordenes.filter { it.id !in excluir })
    }
}
