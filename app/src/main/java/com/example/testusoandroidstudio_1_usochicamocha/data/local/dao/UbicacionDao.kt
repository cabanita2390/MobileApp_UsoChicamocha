package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.UbicacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UbicacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ubicaciones: List<UbicacionEntity>)

    @Query("SELECT * FROM ubicaciones ORDER BY nombreUbicacion ASC")
    fun getAllUbicaciones(): Flow<List<UbicacionEntity>>

    @Query("SELECT COUNT(id) FROM ubicaciones")
    suspend fun count(): Int

    @Query("DELETE FROM ubicaciones")
    suspend fun deleteAll()

    @Transaction
    suspend fun clearAndInsert(ubicaciones: List<UbicacionEntity>) {
        deleteAll()
        insertAll(ubicaciones)
    }
}
