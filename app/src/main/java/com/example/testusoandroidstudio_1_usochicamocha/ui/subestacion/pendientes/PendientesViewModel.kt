package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.pendientes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEjecucionPorProgramacionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerPendientesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.CitaUi
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.EstadoCita
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.estadoDeCita
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.nombreMes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val obtenerPendientesUseCase: ObtenerPendientesUseCase,
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

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val hoy = LocalDate.now()
            obtenerPendientesUseCase(hoy.year, hoy.monthValue)
                .onSuccess { citas ->
                    citasCrudas = citas
                    _uiState.update { it.copy(isLoading = false, grupos = agrupar(citas, it.filtro)) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "No se pudieron cargar los pendientes.")
                    }
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
