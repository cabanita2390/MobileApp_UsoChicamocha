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

/**
 * Una cita del cronograma con su estado de cumplimiento, para las pantallas Cronograma/Pendientes.
 *
 * También se usa como envoltorio sintético para una ejecución NO programada (sin cita de
 * cronograma asociada — ver GET /ejecuciones esProgramada=false) dentro de la pestaña
 * "Realizadas" de Pendientes: esas ejecuciones nunca aparecen en v_mant_cumplimiento
 * (arranca desde mant_programacion), así que se modelan como una "cita" ya EJECUTADA
 * (`cumple=true`) con `programacionId = -ejecucionId` (negativo, nunca colisiona con un id
 * real de programación, solo para tener una key estable en la UI) y `esProgramada=false`.
 * `ejecucionId` viaja en ese caso para poder navegar directo a Detalle sin pasar por
 * "ejecución por programación" (que no existe para estos registros).
 */
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
    val cumple: Boolean,
    val esProgramada: Boolean = true,
    val ejecucionId: Long? = null
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
