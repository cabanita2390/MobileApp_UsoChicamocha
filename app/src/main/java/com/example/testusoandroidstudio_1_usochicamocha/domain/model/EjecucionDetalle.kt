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
    val ediciones: List<EdicionDetalle>,
    /** Visita que ejecutó una orden de trabajo: como las de las citas, su actividad no se cambia al editar. */
    val deOrden: Boolean = false,
    /** Eléctrico: lo marcado en «¿Qué se intervino?»; vacío en Civil y en registros anteriores. */
    val conjuntos: List<ItemRef> = emptyList(),
    val elementos: List<ItemRef> = emptyList(),
    val conjuntoOtro: String? = null,
    val elementoOtro: String? = null
)

/** Conjunto o elemento marcado en una visita (id para editar, nombre para mostrar). */
data class ItemRef(
    val id: Long,
    val nombre: String
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
