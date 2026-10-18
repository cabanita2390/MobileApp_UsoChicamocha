package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.captura

import android.net.Uri
import com.google.gson.Gson

/**
 * Lo que el técnico lleva capturado en el formulario. Se guarda en el SavedStateHandle del
 * ViewModel, que Android conserva aunque mate el proceso con la app en segundo plano (cámara,
 * llamada, batería baja): antes, al volver, el formulario arrancaba de cero y se perdían el
 * resultado, las observaciones y las fotos elegidas. La rotación ya lo conservaba (ViewModel).
 *
 * Los catálogos y la sugerencia de cita no se guardan: se vuelven a cargar solos.
 */
internal data class BorradorCaptura(
    val currentStep: Int,
    val programacionId: Long?,
    /** Gson deja null en borradores de versiones anteriores (sin órdenes de trabajo). */
    val ordenId: Long?,
    val mesProgramado: Int?,
    val fecha: String,
    val estacionId: Long?,
    val estacionNombre: String,
    val mesEjecucion: Int,
    val semanaEjecucion: Int,
    val periodoAjustadoManualmente: Boolean,
    val tipoActividad: String,
    val tipoMantenimiento: String,
    val modoLibre: Boolean,
    val actividadId: Long?,
    val actividadNombre: String?,
    val descripcionLibre: String,
    val resultado: String,
    val observaciones: String,
    val fotos: List<String>
) {
    fun aplicarA(s: CapturaUiState): CapturaUiState = s.copy(
        currentStep = currentStep,
        programacionId = programacionId,
        ordenId = ordenId,
        mesProgramado = mesProgramado,
        fecha = fecha,
        estacionId = estacionId,
        estacionNombre = estacionNombre,
        mesEjecucion = mesEjecucion,
        semanaEjecucion = semanaEjecucion,
        periodoAjustadoManualmente = periodoAjustadoManualmente,
        tipoActividad = tipoActividad,
        tipoMantenimiento = tipoMantenimiento,
        modoLibre = modoLibre,
        actividadId = actividadId,
        actividadNombre = actividadNombre,
        descripcionLibre = descripcionLibre,
        resultado = resultado,
        observaciones = observaciones,
        fotos = fotos.map(Uri::parse)
    )

    fun aJson(): String = gson.toJson(this)

    companion object {
        const val KEY = "borrador_captura"
        private val gson = Gson()

        fun de(s: CapturaUiState) = BorradorCaptura(
            currentStep = s.currentStep,
            programacionId = s.programacionId,
            ordenId = s.ordenId,
            mesProgramado = s.mesProgramado,
            fecha = s.fecha,
            estacionId = s.estacionId,
            estacionNombre = s.estacionNombre,
            mesEjecucion = s.mesEjecucion,
            semanaEjecucion = s.semanaEjecucion,
            periodoAjustadoManualmente = s.periodoAjustadoManualmente,
            tipoActividad = s.tipoActividad,
            tipoMantenimiento = s.tipoMantenimiento,
            modoLibre = s.modoLibre,
            actividadId = s.actividadId,
            actividadNombre = s.actividadNombre,
            descripcionLibre = s.descripcionLibre,
            resultado = s.resultado,
            observaciones = s.observaciones,
            fotos = s.fotos.map(Uri::toString)
        )

        fun desdeJson(json: String?): BorradorCaptura? =
            json?.let { runCatching { gson.fromJson(it, BorradorCaptura::class.java) }.getOrNull() }
    }
}
