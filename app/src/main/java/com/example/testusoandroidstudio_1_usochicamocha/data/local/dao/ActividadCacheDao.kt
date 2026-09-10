package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ActividadCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actividades: List<ActividadCacheEntity>)

    @Query("SELECT * FROM mant_actividad_cache ORDER BY nombre ASC")
    fun getAllFlow(): Flow<List<ActividadCacheEntity>>

    @Query("DELETE FROM mant_actividad_cache")
    suspend fun deleteAll()

    @Transaction
    suspend fun clearAndInsert(actividades: List<ActividadCacheEntity>) {
        deleteAll()
        insertAll(actividades)
    }
}
