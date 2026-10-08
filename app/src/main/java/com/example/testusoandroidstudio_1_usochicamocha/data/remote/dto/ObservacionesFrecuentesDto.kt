package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Observaciones más usadas de un tipo de actividad (GET substation/observaciones/frecuentes). */
data class ObservacionesFrecuentesDto(
    @SerializedName("tipoActividad") val tipoActividad: String,
    @SerializedName("textos") val textos: List<String>
)
