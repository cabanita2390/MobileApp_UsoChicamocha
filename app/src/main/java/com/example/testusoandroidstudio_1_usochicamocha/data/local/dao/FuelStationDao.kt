package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelStationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelStationDao {
    @Query("SELECT * FROM fuel_stations_local ORDER BY name ASC")
    fun getAll(): Flow<List<FuelStationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stations: List<FuelStationEntity>)

    @Query("DELETE FROM fuel_stations_local")
    suspend fun deleteAll()
}
