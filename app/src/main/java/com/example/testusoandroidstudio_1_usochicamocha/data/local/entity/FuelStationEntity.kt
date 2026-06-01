package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_stations_local")
data class FuelStationEntity(
    @PrimaryKey val id: Long,
    val name: String
)
