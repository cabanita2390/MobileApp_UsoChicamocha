package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ConjuntoElementoCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConjuntoElementoCacheDao {
    @Query("SELECT * FROM mant_conjunto_elemento_cache ORDER BY conjuntoId, orden")
    fun getAllFlow(): Flow<List<ConjuntoElementoCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ConjuntoElementoCacheEntity>)

    @Query("DELETE FROM mant_conjunto_elemento_cache")
    suspend fun deleteAll()

    @Transaction
    suspend fun reemplazar(items: List<ConjuntoElementoCacheEntity>) {
        deleteAll()
        insertAll(items)
    }
}
