package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

data class KilometrajeValidacionResponse(
    @SerializedName("alerta")
    val alerta: Boolean,

    @SerializedName("mensaje")
    val mensaje: String
)
