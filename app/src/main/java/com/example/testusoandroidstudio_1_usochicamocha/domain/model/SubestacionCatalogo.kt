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
    val nombre: String,
    /** Código de la disciplina (CIVIL, ELECTRICO…): el wizard muestra solo las de la disciplina elegida. */
    val disciplina: String = "CIVIL"
)

/**
 * Conjunto o elemento que el operario puede marcar en «¿Qué se intervino?» (las disciplinas que lo usan).
 * [tipo] es [ItemIntervenido.CONJUNTO] o [ItemIntervenido.ELEMENTO].
 */
data class ItemIntervenido(
    val id: Long,
    val nombre: String,
    val disciplina: String,
    val tipo: String,
    /** Solo conjuntos: código del ícono elegido en la web (SUBESTACION, GABINETE…); null = genérico. */
    val icono: String? = null,
    /** Solo conjuntos: lo que tiene adentro, en orden (el conjunto es la caja y estos su contenido). */
    val elementos: List<ElementoDeConjunto> = emptyList()
) {
    companion object {
        const val CONJUNTO = "CONJUNTO"
        const val ELEMENTO = "ELEMENTO"
    }
}

/** Un elemento dentro de un conjunto. [pideDetalle]: al marcarlo se escribe cómo se encuentra. */
data class ElementoDeConjunto(
    val id: Long,
    val nombre: String,
    val pideDetalle: Boolean = false
)

/** Un elemento marcado en la visita, dentro de su conjunto, con el texto si lo pide. */
data class ElementoMarcado(
    val conjuntoId: Long,
    val elementoId: Long,
    val detalle: String? = null
)

/** "Otro" en los elementos de un conjunto: el backend lo agrega a ese conjunto para la próxima vez. */
data class ElementoOtro(
    val conjuntoId: Long,
    val texto: String
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
    val ejecucionId: Long? = null,
    /** Registrada en este equipo pero todavía en la cola de envío (sin id del servidor). */
    val enCola: Boolean = false,
    /** Código de la disciplina (CIVIL, ELECTRICO, ELECTROMECANICO). */
    val disciplina: String = "CIVIL"
)
// Si una disciplina se registra desde el móvil lo dice el backend: ver Disciplina.capturaMovil.

/** Cita "cruda" del cronograma (sin estado de cumplimiento), para precargar el wizard de captura. */
data class ProgramacionCita(
    val id: Long,
    val anio: Int,
    val mes: Int,
    val estacionId: Long,
    val actividadId: Long,
    val actividadNombre: String
)
