package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EstacionCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EstacionCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(estaciones: List<EstacionCacheEntity>)

    @Query("SELECT * FROM mant_estacion_cache ORDER BY nombre ASC")
    fun getAllFlow(): Flow<List<EstacionCacheEntity>>

    @Query("DELETE FROM mant_estacion_cache")
    suspend fun deleteAll()

    @Transaction
    suspend fun clearAndInsert(estaciones: List<EstacionCacheEntity>) {
        deleteAll()
        insertAll(estaciones)
    }
}
