package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiculoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<VehiculoEntity>)

    @Query("SELECT * FROM vehiculos ORDER BY placa ASC")
    fun getAllVehicles(): Flow<List<VehiculoEntity>>

    @Query("DELETE FROM vehiculos")
    suspend fun clearAllVehicles()
}
