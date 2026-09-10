package com.example.testusoandroidstudio_1_usochicamocha.domain.model

/** Detalle completo de una ejecución ya registrada en el servidor (pantalla Detalle, solo lectura + edición). */
data class EjecucionDetalle(
    val id: Long,
    val fecha: String,
    val mesEjecucion: Int,
    val semanaEjecucion: Int,
    val estacionId: Long,
    val estacionNombre: String,
    val disciplina: String,
    val tipoMantenimiento: String,
    val tipoActividad: String,
    val actividadId: Long?,
    val actividadNombre: String?,
    val esProgramada: Boolean,
    val motivoNoCatalogado: String?,
    val resultado: String,
    val observaciones: String,
    val descripcionLibre: String?,
    val responsable: String,
    val uuidCliente: String,
    val evidencias: List<EvidenciaDetalle>,
    val evidenciaPendiente: Boolean,
    val ediciones: List<EdicionDetalle>
)

data class EvidenciaDetalle(
    val id: Long,
    val rutaArchivo: String,
    val nombreOriginal: String,
    val subidoEn: String?
)

data class EdicionDetalle(
    val usuario: String,
    val motivo: String,
    val editadoEn: String?
)
