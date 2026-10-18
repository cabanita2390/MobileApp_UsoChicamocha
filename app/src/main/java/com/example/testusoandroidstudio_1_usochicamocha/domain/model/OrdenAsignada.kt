package com.example.testusoandroidstudio_1_usochicamocha.domain.model

/** Orden de trabajo (OT-EB) que el operario tiene que ejecutar para corregir un hallazgo. */
data class OrdenAsignada(
    val id: Long,
    val consecutivo: String,
    /** ALTA o MEDIA. */
    val prioridad: String,
    /** yyyy-MM-dd */
    val fechaLimite: String,
    val vencida: Boolean,
    val estacionId: Long,
    val estacionNombre: String,
    val actividadId: Long,
    val actividadNombre: String,
    val instrucciones: String,
    val hallazgoFecha: String,
    val hallazgoResultado: String,
    val hallazgoObservaciones: String,
    /** Si el supervisor la devolvió, por qué; null si no. */
    val motivoDevolucion: String?,
    val fotosHallazgo: List<String>,
    /** Para cualquier operario: también les aparece a los demás hasta que alguien la registre. */
    val paraCualquiera: Boolean = false
)
