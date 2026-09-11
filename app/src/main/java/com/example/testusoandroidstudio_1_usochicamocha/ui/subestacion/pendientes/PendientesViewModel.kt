package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.pendientes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerCumplimientoAnioLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEjecucionPorProgramacionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEjecucionesNoProgramadasAnioLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.SincronizarCumplimientoAnioUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.SincronizarEjecucionesNoProgramadasAnioUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.CitaUi
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.EstadoCita
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.estadoDeCita
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.nombreMes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class FiltroPendientes { POR_HACER, VENCIDAS, REALIZADAS }

data class MesGrupo(val anio: Int, val mes: Int, val citas: List<CitaUi>) {
    val label: String
        get() {
            val hoy = LocalDate.now()
            val sufijo = if (anio == hoy.year && mes == hoy.monthValue) "MES EN CURSO" else "MES CERRADO"
            return "${nombreMes(mes)} $anio · $sufijo"
        }
}

data class PendientesUiState(
    val filtro: FiltroPendientes = FiltroPendientes.POR_HACER,
    val grupos: List<MesGrupo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PendientesViewModel @Inject constructor(
    private val obtenerCumplimientoAnioLocalUseCase: ObtenerCumplimientoAnioLocalUseCase,
    private val sincronizarCumplimientoAnioUseCase: SincronizarCumplimientoAnioUseCase,
    private val obtenerEjecucionesNoProgramadasAnioLocalUseCase: ObtenerEjecucionesNoProgramadasAnioLocalUseCase,
    private val sincronizarEjecucionesNoProgramadasAnioUseCase: SincronizarEjecucionesNoProgramadasAnioUseCase,
    private val obtenerEjecucionPorProgramacionUseCase: ObtenerEjecucionPorProgramacionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Se lee UNA sola vez, al construir el ViewModel, directo del argumento de
    // navegación (Hilt ya puebla el SavedStateHandle con los args de la
    // NavBackStackEntry actual) — no depende de ningún efecto en la pantalla que
    // pueda volver a dispararse y pisar un cambio manual de chip después. Un tap
    // en un chip llama `onFiltroChange` directo y es lo único que puede cambiar
    // el filtro de ahí en adelante.
    private val filtroInicial: FiltroPendientes = when (savedStateHandle.get<String>("filtroInicial")) {
        "Realizadas" -> FiltroPendientes.REALIZADAS
        "Vencidas" -> FiltroPendientes.VENCIDAS
        "PorHacer" -> FiltroPendientes.POR_HACER
        else -> FiltroPendientes.POR_HACER
    }

    private val _uiState = MutableStateFlow(PendientesUiState(filtro = filtroInicial))
    val uiState = _uiState.asStateFlow()

    private var citasCrudas: List<CitaProgramada> = emptyList()

    // `cargar()` se re-dispara en cada ON_RESUME de la pantalla — sin cancelar el
    // colector anterior se irían acumulando suscripciones duplicadas al mismo Flow.
    private var localJob: Job? = null

    init {
        cargar()
    }

    /**
     * Suscribe al caché local del año en curso (Room, offline-first) — combina las citas
     * programadas (mant_cumplimiento_cache) con las ejecuciones NO programadas
     * (mant_ejecucion_no_programada_cache) para que "Realizadas" muestre ambas — y dispara un
     * refresco en segundo plano contra el backend de las dos fuentes: best-effort, si falla
     * (sin red), la pantalla se queda con lo último cacheado en vez de quedar vacía/rota.
     */
    fun cargar() {
        val hoy = LocalDate.now()
        _uiState.update { it.copy(isLoading = true, error = null) }

        localJob?.cancel()
        localJob = viewModelScope.launch {
            combine(
                obtenerCumplimientoAnioLocalUseCase(hoy.year, hoy.monthValue),
                obtenerEjecucionesNoProgramadasAnioLocalUseCase(hoy.year, hoy.monthValue)
            ) { programadas, noProgramadas -> programadas + noProgramadas }
                .collect { citas ->
                    citasCrudas = citas
                    _uiState.update { it.copy(isLoading = false, grupos = agrupar(citas, it.filtro)) }
                }
        }

        viewModelScope.launch {
            val resultadoCumplimiento = sincronizarCumplimientoAnioUseCase(hoy.year, hoy.monthValue)
            val resultadoNoProgramadas = sincronizarEjecucionesNoProgramadasAnioUseCase(hoy.year)
            val error = resultadoCumplimiento.exceptionOrNull() ?: resultadoNoProgramadas.exceptionOrNull()
            if (error != null) {
                _uiState.update {
                    if (citasCrudas.isEmpty()) {
                        it.copy(isLoading = false, error = error.message ?: "Sin conexión. No hay datos guardados de pendientes.")
                    } else {
                        it.copy(error = null)
                    }
                }
            } else {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    fun onFiltroChange(filtro: FiltroPendientes) {
        _uiState.update { it.copy(filtro = filtro, grupos = agrupar(citasCrudas, filtro)) }
    }

    /** Resuelve el ejecucionId de una cita ya EJECUTADA para poder abrir su Detalle. */
    fun resolverDetalle(programacionId: Long, onResuelto: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            obtenerEjecucionPorProgramacionUseCase(programacionId)
                .onSuccess { detalle ->
                    if (detalle != null) onResuelto(detalle.id) else onError("Aún no hay un registro para esta cita.")
                }
                .onFailure { e -> onError(e.message ?: "No se pudo abrir el detalle.") }
        }
    }

    private fun agrupar(citas: List<CitaProgramada>, filtro: FiltroPendientes): List<MesGrupo> {
        val hoy = LocalDate.now()
        val conEstado = citas.map { it to estadoDeCita(it, hoy) }
        val filtradas = when (filtro) {
            FiltroPendientes.POR_HACER -> conEstado.filter {
                it.second == EstadoCita.VENCIDA || it.second == EstadoCita.PENDIENTE
            }
            FiltroPendientes.VENCIDAS -> conEstado.filter { it.second == EstadoCita.VENCIDA }
            FiltroPendientes.REALIZADAS -> conEstado.filter { it.second == EstadoCita.EJECUTADA }
        }
        return filtradas
            .groupBy { it.first.anio to it.first.mes }
            .entries
            .sortedWith(compareByDescending<Map.Entry<Pair<Int, Int>, List<Pair<CitaProgramada, EstadoCita>>>> { it.key.first }
                .thenByDescending { it.key.second })
            .map { (ym, lista) ->
                MesGrupo(
                    anio = ym.first,
                    mes = ym.second,
                    citas = lista.map { CitaUi(it.first, it.second) }
                )
            }
    }
}
