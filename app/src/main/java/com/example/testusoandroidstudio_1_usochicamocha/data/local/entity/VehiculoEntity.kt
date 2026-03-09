package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoItem

@Entity(tableName = "vehiculos")
data class VehiculoEntity(
    @PrimaryKey val idVehiculo: Int,
    val placa: String,
    val marca: String,
    val tipoVehiculo: String
)

fun VehiculoEntity.toVehiculoItem(): VehiculoItem {
    return VehiculoItem(
        idVehiculo = idVehiculo,
        placa = placa,
        marca = marca,
        tipoVehiculo = tipoVehiculo
    )
}

fun VehiculoItem.toEntity(): VehiculoEntity {
    return VehiculoEntity(
        idVehiculo = idVehiculo,
        placa = placa,
        marca = marca,
        tipoVehiculo = tipoVehiculo
    )
}
