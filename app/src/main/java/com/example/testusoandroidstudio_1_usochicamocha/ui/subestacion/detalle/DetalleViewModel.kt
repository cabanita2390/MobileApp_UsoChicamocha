package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerDetalleEjecucionLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.SincronizarDetalleEjecucionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleUiState(
    val isLoading: Boolean = true,
    val detalle: EjecucionDetalle? = null,
    val error: String? = null
)

/**
 * Pantalla Detalle: offline-first — lee el detalle cacheado en Room (bajo demanda: se
 * cachea la primera vez que se consulta con éxito, ver EjecucionDetalleCacheEntity) y
 * dispara un refresco en segundo plano contra el backend. Si no hay red y tampoco hay
 * nada cacheado para este id todavía, se muestra un error explicándolo en vez de una
 * pantalla en blanco.
 */
@HiltViewModel
class DetalleViewModel @Inject constructor(
    private val obtenerDetalleEjecucionLocalUseCase: ObtenerDetalleEjecucionLocalUseCase,
    private val sincronizarDetalleEjecucionUseCase: SincronizarDetalleEjecucionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState = _uiState.asStateFlow()

    private var cargadoParaId: Long? = null

    // `cargar()` puede volver a llamarse para el mismo id (reintentar, recargarSiCorresponde)
    // — sin cancelar el colector anterior se irían acumulando suscripciones duplicadas.
    private var localJob: Job? = null

    fun cargar(ejecucionId: Long) {
        cargadoParaId = ejecucionId
        _uiState.update { it.copy(isLoading = true, error = null) }

        localJob?.cancel()
        localJob = viewModelScope.launch {
            obtenerDetalleEjecucionLocalUseCase(ejecucionId).collect { detalle ->
                _uiState.update { it.copy(isLoading = false, detalle = detalle) }
            }
        }

        viewModelScope.launch {
            sincronizarDetalleEjecucionUseCase(ejecucionId)
                .onFailure { e ->
                    _uiState.update {
                        if (it.detalle == null) {
                            it.copy(isLoading = false, error = e.message ?: "Sin conexión y no hay datos guardados de este registro.")
                        } else {
                            it.copy(error = null)
                        }
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(error = null) }
                }
        }
    }

    /** Se llama al volver a esta pantalla (ej. tras editar) para refrescar sin perder el estado del scroll. */
    fun recargarSiCorresponde() {
        cargadoParaId?.let { cargar(it) }
    }
}
