package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ItemIntervenidoCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemIntervenidoCacheDao {
    @Query("SELECT * FROM mant_intervenido_cache ORDER BY nombre ASC")
    fun getAllFlow(): Flow<List<ItemIntervenidoCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemIntervenidoCacheEntity>)

    @Query("DELETE FROM mant_intervenido_cache WHERE tipo = :tipo")
    suspend fun deleteTipo(tipo: String)

    /** Reemplaza un catálogo (conjuntos o elementos) sin tocar el otro. */
    @Transaction
    suspend fun reemplazar(tipo: String, items: List<ItemIntervenidoCacheEntity>) {
        deleteTipo(tipo)
        insertAll(items)
    }
}
