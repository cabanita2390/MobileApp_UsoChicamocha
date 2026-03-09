package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoItem

data class VehicleDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("placa")
    val placa: String,
    @SerializedName("marca")
    val marca: String?,
    @SerializedName("tipoVehiculo")
    val tipoVehiculo: String?
)

fun VehicleDto.toVehiculoItem(): VehiculoItem {
    return VehiculoItem(
        idVehiculo   = id,
        placa        = placa,
        marca        = marca        ?: "",
        tipoVehiculo = tipoVehiculo ?: ""
    )
}
