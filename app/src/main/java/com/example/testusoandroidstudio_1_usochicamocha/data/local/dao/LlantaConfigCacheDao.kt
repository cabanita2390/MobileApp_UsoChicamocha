package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LlantaConfigCacheEntity

@Dao
interface LlantaConfigCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<LlantaConfigCacheEntity>)

    @Query("SELECT * FROM llanta_config_cache WHERE tipoActivo = :tipoActivo AND activoId = :activoId LIMIT 1")
    suspend fun get(tipoActivo: String, activoId: Long): LlantaConfigCacheEntity?

    @Query("DELETE FROM llanta_config_cache")
    suspend fun deleteAll()

    /** El servidor manda todas las asignaciones vigentes: las que ya no vienen se quitaron. */
    @Transaction
    suspend fun clearAndInsert(configs: List<LlantaConfigCacheEntity>) {
        deleteAll()
        insertAll(configs)
    }
}
