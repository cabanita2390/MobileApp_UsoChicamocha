package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerCumplimientoAnioLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEstacionesCacheUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.SincronizarCumplimientoAnioUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.EstadoCita
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.estadoDeCita
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.nombreMes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * KPIs de la tarjeta "hero" del Home de Subestaciones (mock líneas ~36-55): citas
 * ejecutadas del mes en curso / total programadas, más los conteos de pendientes y
 * vencidas que ya usa PendientesScreen — reutiliza el mismo caso de uso y la misma
 * derivación de estado (`estadoDeCita`) en vez de duplicar la lógica.
 */
data class SubestacionHomeUiState(
    val mesLabel: String = "",
    val anio: Int = LocalDate.now().year,
    val ejecutadasDelMes: Int = 0,
    val programadasDelMes: Int = 0,
    val porcentaje: Int = 0,
    val pendientes: Int = 0,
    val pendientesDelMes: Int = 0,
    val vencidas: Int = 0,
    val realizadasCount: Int = 0,
    val estacionesTotal: Int = 0,
    val responsableNombre: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class SubestacionHomeViewModel @Inject constructor(
    private val obtenerCumplimientoAnioLocalUseCase: ObtenerCumplimientoAnioLocalUseCase,
    private val sincronizarCumplimientoAnioUseCase: SincronizarCumplimientoAnioUseCase,
    private val obtenerEstacionesCacheUseCase: ObtenerEstacionesCacheUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubestacionHomeUiState())
    val uiState = _uiState.asStateFlow()

    // `cargar()` se re-dispara en cada ON_RESUME de la pantalla — sin cancelar el
    // colector anterior se irían acumulando suscripciones duplicadas al mismo Flow.
    private var localJob: Job? = null

    init {
        cargar()
        viewModelScope.launch {
            obtenerEstacionesCacheUseCase().collect { lista ->
                _uiState.update { it.copy(estacionesTotal = lista.size) }
            }
        }
        viewModelScope.launch {
            tokenManager.getUsername().collect { username ->
                _uiState.update { it.copy(responsableNombre = username ?: "") }
            }
        }
    }

    /**
     * Suscribe al caché local del año en curso (Room, offline-first) y dispara un
     * refresco en segundo plano contra el backend — best-effort: si falla (sin red),
     * la pantalla se queda con los últimos KPIs cacheados en vez de quedar vacía/rota.
     */
    fun cargar() {
        val hoy = LocalDate.now()
        _uiState.update { it.copy(isLoading = true) }

        localJob?.cancel()
        localJob = viewModelScope.launch {
            obtenerCumplimientoAnioLocalUseCase(hoy.year, hoy.monthValue).collect { citas ->
                val conEstado = citas.map { it to estadoDeCita(it, hoy) }
                val delMes = conEstado.filter { it.first.anio == hoy.year && it.first.mes == hoy.monthValue }
                val ejecutadasDelMes = delMes.count { it.second == EstadoCita.EJECUTADA }
                val pendientesDelMes = delMes.count { it.second == EstadoCita.PENDIENTE }
                val vencidas = conEstado.count { it.second == EstadoCita.VENCIDA }
                val programadasDelMes = delMes.size
                // Mock: realizadasResumen cuenta TODAS las ejecutadas, no solo las del
                // mes en curso — el caché de cumplimiento ya trae todo el año hasta el mes
                // actual, así que basta con no filtrar por delMes acá.
                val realizadasCount = conEstado.count { it.second == EstadoCita.EJECUTADA }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mesLabel = nombreMes(hoy.monthValue),
                        anio = hoy.year,
                        ejecutadasDelMes = ejecutadasDelMes,
                        programadasDelMes = programadasDelMes,
                        porcentaje = if (programadasDelMes > 0) (ejecutadasDelMes * 100 / programadasDelMes) else 0,
                        // Mock: pendMes = vencidas.length + pendientesMes.length — todo el
                        // backlog (vencidas de meses cerrados + pendientes del mes actual),
                        // no solo el mes en curso.
                        pendientes = pendientesDelMes + vencidas,
                        pendientesDelMes = pendientesDelMes,
                        vencidas = vencidas,
                        realizadasCount = realizadasCount
                    )
                }
            }
        }

        viewModelScope.launch {
            sincronizarCumplimientoAnioUseCase(hoy.year, hoy.monthValue)
                .onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }
}
