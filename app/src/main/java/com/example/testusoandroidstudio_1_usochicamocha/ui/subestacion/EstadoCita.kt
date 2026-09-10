package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion

import androidx.compose.ui.graphics.Color
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionTono
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionTonos
import java.time.LocalDate
import java.time.YearMonth

/**
 * Estado visual de una cita del cronograma, derivado en el cliente (el backend solo
 * expone `cumple`/`ejecutado`) — mismo cálculo que usaba el mock del diseño:
 * mes futuro → PROGRAMADA; mes actual → EJECUTADA si cumple, si no PENDIENTE;
 * mes pasado → EJECUTADA si cumple, si no VENCIDA. Compartido entre Cronograma y
 * Pendientes para no duplicar la regla.
 */
enum class EstadoCita { PENDIENTE, VENCIDA, EJECUTADA, PROGRAMADA }

data class CitaUi(val cita: CitaProgramada, val estado: EstadoCita)

fun estadoDeCita(cita: CitaProgramada, hoy: LocalDate = LocalDate.now()): EstadoCita {
    val citaYearMonth = YearMonth.of(cita.anio, cita.mes)
    val actual = YearMonth.of(hoy.year, hoy.monthValue)
    return when {
        citaYearMonth > actual -> EstadoCita.PROGRAMADA
        cita.cumple -> EstadoCita.EJECUTADA
        citaYearMonth == actual -> EstadoCita.PENDIENTE
        else -> EstadoCita.VENCIDA
    }
}

/**
 * Tono semántico por estado — igual al `tone`/`qTone` del mock: PENDIENTE es morado
 * (mismo acento que el resto de la UI "activa"), no ámbar. Ámbar en el mock se
 * reserva para avisos/notas, no para el estado de una cita.
 */
fun tonoDeEstado(estado: EstadoCita): SubestacionTono = when (estado) {
    EstadoCita.EJECUTADA -> SubestacionTonos.Ejecutada
    EstadoCita.PENDIENTE -> SubestacionTonos.Pendiente
    EstadoCita.VENCIDA -> SubestacionTonos.Vencida
    EstadoCita.PROGRAMADA -> SubestacionTonos.Programada
}

fun colorDeEstado(estado: EstadoCita): Color = tonoDeEstado(estado).accent

fun etiquetaDeEstado(estado: EstadoCita): String = when (estado) {
    EstadoCita.EJECUTADA -> "Ejecutada"
    EstadoCita.PENDIENTE -> "Pendiente"
    EstadoCita.VENCIDA -> "Vencida"
    EstadoCita.PROGRAMADA -> "Programada"
}

// Etiquetas en español de los códigos que expone el backend — compartidas entre
// Detalle y el resumen del wizard de Captura para no duplicar el mapeo.
val L_ACT = mapOf("INSPECCION" to "Inspección", "MANTENIMIENTO" to "Mantenimiento", "NO_PROGRAMADO" to "No programado", "OTRO" to "Otro")
val L_MANT = mapOf("PREVENTIVO" to "Preventivo", "CORRECTIVO" to "Correctivo", "PREDICTIVO" to "Predictivo", "NO_PROGRAMADO" to "No programado")
val L_RES = mapOf("CONFORME" to "Conforme", "CON_HALLAZGOS" to "Con hallazgos", "REQUIERE_INTERVENCION" to "Requiere intervención")
val L_MOT = mapOf("NO_PROGRAMADO" to "No programado", "OTRO" to "Otro")

private val NOMBRES_MES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

fun nombreMes(mes: Int): String = NOMBRES_MES.getOrElse(mes - 1) { "Mes $mes" }
