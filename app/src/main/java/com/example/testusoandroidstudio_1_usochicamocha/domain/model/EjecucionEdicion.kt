package com.example.testusoandroidstudio_1_usochicamocha.domain.model

/** Payload de dominio para PUT /ejecuciones/{id} — corrección de una ejecución ya sincronizada. */
data class EjecucionEdicion(
    val fecha: String,
    val mesEjecucion: Int,
    val semanaEjecucion: Int,
    val tipoMantenimiento: String,
    val tipoActividad: String,
    val actividadId: Long?,
    val motivoNoCatalogado: String?,
    val resultado: String,
    val observaciones: String,
    val descripcionLibre: String?,
    val motivoEdicion: String
)
