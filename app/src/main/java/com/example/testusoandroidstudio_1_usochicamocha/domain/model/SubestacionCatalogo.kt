package com.example.testusoandroidstudio_1_usochicamocha.domain.model

/** Catálogos de solo lectura del módulo Subestaciones, cacheados en Room para uso offline. */
data class EstacionCatalogo(
    val id: Long,
    val nombre: String,
    val tipo: String,
    val frecuenciaBase: String
)

data class ActividadCatalogo(
    val id: Long,
    val nombre: String
)

/** Una cita del cronograma con su estado de cumplimiento, para las pantallas Cronograma/Pendientes. */
data class CitaProgramada(
    val programacionId: Long,
    val anio: Int,
    val mes: Int,
    val estacionId: Long,
    val estacionNombre: String,
    val estacionTipo: String,
    val actividadId: Long,
    val actividadNombre: String,
    val ejecutado: Int,
    val cumple: Boolean
)

/** Cita "cruda" del cronograma (sin estado de cumplimiento), para precargar el wizard de captura. */
data class ProgramacionCita(
    val id: Long,
    val anio: Int,
    val mes: Int,
    val estacionId: Long,
    val actividadId: Long,
    val actividadNombre: String
)
