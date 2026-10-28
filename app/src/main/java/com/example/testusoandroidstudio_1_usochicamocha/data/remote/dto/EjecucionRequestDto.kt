package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Espejo exacto de EjecucionRequest del backend (substation.application.dto). */
data class EjecucionRequestDto(
    @SerializedName("fecha") val fecha: String,
    @SerializedName("mesEjecucion") val mesEjecucion: Int,
    @SerializedName("semanaEjecucion") val semanaEjecucion: Int,
    @SerializedName("estacionId") val estacionId: Long,
    @SerializedName("disciplina") val disciplina: String,
    @SerializedName("tipoMantenimiento") val tipoMantenimiento: String,
    @SerializedName("tipoActividad") val tipoActividad: String,
    @SerializedName("actividadId") val actividadId: Long?,
    @SerializedName("programacionId") val programacionId: Long?,
    @SerializedName("motivoNoCatalogado") val motivoNoCatalogado: String?,
    @SerializedName("resultado") val resultado: String,
    @SerializedName("observaciones") val observaciones: String,
    @SerializedName("descripcionLibre") val descripcionLibre: String?,
    @SerializedName("uuidCliente") val uuidCliente: String,
    /** Orden de trabajo (OT-EB) que esta visita ejecuta; null en citas y registros libres. */
    @SerializedName("ordenId") val ordenId: Long? = null,
    /** Eléctrico: lo marcado en «¿Qué se intervino?» (el backend ignora los de otra disciplina). */
    @SerializedName("conjuntoIds") val conjuntoIds: List<Long>? = null,
    @SerializedName("elementoIds") val elementoIds: List<Long>? = null,
    @SerializedName("conjuntoOtro") val conjuntoOtro: String? = null,
    @SerializedName("elementoOtro") val elementoOtro: String? = null
)
