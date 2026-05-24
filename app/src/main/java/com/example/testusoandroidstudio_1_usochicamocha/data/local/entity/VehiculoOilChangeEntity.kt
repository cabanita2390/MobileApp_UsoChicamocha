package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehiculo_oil_changes")
data class VehiculoOilChangeEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val placa: String,
    val timestamp: Long,
    val oilType: String,
    val oilBrandId: Long,
    val oilBrandName: String,
    val quantity: Double? = null,
    val kmAtChange: Int,
    val intervalKm: Int,
    val airFilterChanged: Boolean = false,
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false
)
