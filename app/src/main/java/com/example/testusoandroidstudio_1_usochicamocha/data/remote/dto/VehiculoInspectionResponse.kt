package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VehiculoInspectionResponse(
    @SerializedName("idInspeccion")
    val id: Long,
    @SerializedName("mensaje")
    val mensaje: String
)
