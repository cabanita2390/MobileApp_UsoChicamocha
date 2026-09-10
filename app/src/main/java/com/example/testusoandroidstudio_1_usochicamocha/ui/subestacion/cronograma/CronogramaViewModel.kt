package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.cronograma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerCronogramaUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEjecucionPorProgramacionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEstacionesCacheUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.CitaUi
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.estadoDeCita
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import javax.inject.Inject

/** Minúsculas + sin tildes/diacríticos — igual a `norm()` del mock, para que "sotaquira" encuentre "Sotaquirá". */
private fun norm(texto: String): String =
    Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}"), "")

data class EstacionGrupo(
    val estacionId: Long,
    val estacionNombre: String,
    val estacionTipo: String,
    val estacionFrecuencia: String?,
    val citas: List<CitaUi>
)

data class CronogramaUiState(
    val anio: Int = LocalDate.now().year,
    val mes: Int = LocalDate.now().monthValue,
    val query: String = "",
    val grupos: List<EstacionGrupo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CronogramaViewModel @Inject constructor(
    private val obtenerCronogramaUseCase: ObtenerCronogramaUseCase,
    private val obtenerEjecucionPorProgramacionUseCase: ObtenerEjecucionPorProgramacionUseCase,
    obtenerEstacionesCacheUseCase: ObtenerEstacionesCacheUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CronogramaUiState())
    val uiState = _uiState.asStateFlow()

    private var citasCrudas: List<CitaProgramada> = emptyList()

    // El endpoint de cumplimiento no trae la frecuencia base de la estación (solo
    // el catálogo la tiene) — se cruza por estacionId con lo que ya está cacheado
    // en Room para el wizard, sin pedirle nada nuevo al backend.
    private var frecuenciaPorEstacion: Map<Long, String> = emptyMap()

    init {
        viewModelScope.launch {
            obtenerEstacionesCacheUseCase().collect { estaciones ->
                frecuenciaPorEstacion = estaciones.associate { it.id to it.frecuenciaBase }
                _uiState.update { it.copy(grupos = agrupar(citasCrudas, it.query)) }
            }
        }
        cargar()
    }

    fun cargar() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            obtenerCronogramaUseCase(s.anio, s.mes)
                .onSuccess { citas ->
                    citasCrudas = citas
                    _uiState.update { it.copy(isLoading = false, grupos = agrupar(citas, it.query)) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "No se pudo cargar el cronograma.")
                    }
                }
        }
    }

    fun mesAnterior() {
        val s = _uiState.value
        if (s.mes == 1) onMesCambiado(s.anio - 1, 12) else onMesCambiado(s.anio, s.mes - 1)
    }

    fun mesSiguiente() {
        val s = _uiState.value
        if (s.mes == 12) onMesCambiado(s.anio + 1, 1) else onMesCambiado(s.anio, s.mes + 1)
    }

    private fun onMesCambiado(anio: Int, mes: Int) {
        _uiState.update { it.copy(anio = anio, mes = mes) }
        cargar()
    }

    /** Salto directo a un mes elegido del selector (equivalente al `<select>` del mock). */
    fun irAMes(anio: Int, mes: Int) = onMesCambiado(anio, mes)

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, grupos = agrupar(citasCrudas, query)) }
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

    private fun agrupar(citas: List<CitaProgramada>, query: String): List<EstacionGrupo> {
        val hoy = LocalDate.now()
        val q = norm(query)
        val filtradas = if (q.isBlank()) citas else citas.filter {
            norm(it.estacionNombre).contains(q) || norm(it.actividadNombre).contains(q)
        }
        return filtradas.groupBy { it.estacionId }
            .map { (id, citasEstacion) ->
                EstacionGrupo(
                    estacionId = id,
                    estacionNombre = citasEstacion.first().estacionNombre,
                    estacionTipo = citasEstacion.first().estacionTipo,
                    estacionFrecuencia = frecuenciaPorEstacion[id],
                    citas = citasEstacion.map { CitaUi(it, estadoDeCita(it, hoy)) }
                )
            }
            .sortedBy { it.estacionNombre }
    }
}
