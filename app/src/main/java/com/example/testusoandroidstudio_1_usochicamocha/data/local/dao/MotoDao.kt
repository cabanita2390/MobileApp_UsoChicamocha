package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(motos: List<MotoEntity>)

    @Query("SELECT * FROM motos ORDER BY placa ASC")
    fun getAllMotos(): Flow<List<MotoEntity>>

    @Query("SELECT COUNT(id) FROM motos")
    suspend fun count(): Int

    @Query("DELETE FROM motos")
    suspend fun deleteAll()

    @Transaction
    suspend fun clearAndInsert(motos: List<MotoEntity>) {
        deleteAll()
        insertAll(motos)
    }
}
