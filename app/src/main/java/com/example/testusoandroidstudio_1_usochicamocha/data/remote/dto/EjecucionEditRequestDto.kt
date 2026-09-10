package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Espejo exacto de EjecucionEditRequest del backend. Sin estacionId/disciplina/
 * programacionId/uuidCliente: no son editables (ver PUT /ejecuciones/{id}).
 */
data class EjecucionEditRequestDto(
    @SerializedName("fecha") val fecha: String,
    @SerializedName("mesEjecucion") val mesEjecucion: Int,
    @SerializedName("semanaEjecucion") val semanaEjecucion: Int,
    @SerializedName("tipoMantenimiento") val tipoMantenimiento: String,
    @SerializedName("tipoActividad") val tipoActividad: String,
    @SerializedName("actividadId") val actividadId: Long?,
    @SerializedName("motivoNoCatalogado") val motivoNoCatalogado: String?,
    @SerializedName("resultado") val resultado: String,
    @SerializedName("observaciones") val observaciones: String,
    @SerializedName("descripcionLibre") val descripcionLibre: String?,
    @SerializedName("motivoEdicion") val motivoEdicion: String
)
