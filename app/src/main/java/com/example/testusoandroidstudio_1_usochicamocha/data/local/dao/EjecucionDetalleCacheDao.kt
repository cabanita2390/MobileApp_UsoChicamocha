package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionDetalleCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EjecucionDetalleCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(detalle: EjecucionDetalleCacheEntity)

    @Query("SELECT * FROM mant_ejecucion_detalle_cache WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<EjecucionDetalleCacheEntity?>
}
