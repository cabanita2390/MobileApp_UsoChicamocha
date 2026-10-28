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
    @SerializedName("motivoEdicion") val motivoEdicion: String,
    /** Eléctrico. null = el servidor los deja como estaban (Civil no los envía). */
    @SerializedName("conjuntoIds") val conjuntoIds: List<Long>? = null,
    @SerializedName("elementoIds") val elementoIds: List<Long>? = null,
    @SerializedName("conjuntoOtro") val conjuntoOtro: String? = null,
    @SerializedName("elementoOtro") val elementoOtro: String? = null
)
