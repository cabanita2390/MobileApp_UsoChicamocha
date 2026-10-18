package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Espejo de OrdenMovilResponse (GET /substation/ordenes/asignadas). Solo los campos que usa el móvil. */
data class OrdenAsignadaDto(
    @SerializedName("orden") val orden: OrdenResumenDto,
    @SerializedName("evidenciasHallazgo") val evidenciasHallazgo: List<EvidenciaSubestacionDto>?,
    @SerializedName("motivoDevolucion") val motivoDevolucion: String?
)

data class OrdenResumenDto(
    @SerializedName("id") val id: Long,
    @SerializedName("consecutivo") val consecutivo: String,
    @SerializedName("prioridad") val prioridad: String,
    @SerializedName("fechaLimite") val fechaLimite: String,
    @SerializedName("vencida") val vencida: Boolean?,
    @SerializedName("estacionId") val estacionId: Long,
    @SerializedName("estacionNombre") val estacionNombre: String,
    @SerializedName("actividadId") val actividadId: Long,
    @SerializedName("actividadNombre") val actividadNombre: String,
    @SerializedName("instrucciones") val instrucciones: String,
    @SerializedName("hallazgoFecha") val hallazgoFecha: String?,
    @SerializedName("hallazgoResultado") val hallazgoResultado: String?,
    @SerializedName("hallazgoObservaciones") val hallazgoObservaciones: String?
)
