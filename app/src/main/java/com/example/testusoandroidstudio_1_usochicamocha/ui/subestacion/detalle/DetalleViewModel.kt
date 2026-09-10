package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerDetalleEjecucionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

/** Pantalla Detalle: solo lectura, siempre online (ver Simplificaciones del plan de implementación). */
@HiltViewModel
class DetalleViewModel @Inject constructor(
    private val obtenerDetalleEjecucionUseCase: ObtenerDetalleEjecucionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState = _uiState.asStateFlow()

    private var cargadoParaId: Long? = null

    fun cargar(ejecucionId: Long) {
        cargadoParaId = ejecucionId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            obtenerDetalleEjecucionUseCase(ejecucionId)
                .onSuccess { d -> _uiState.update { it.copy(isLoading = false, detalle = d) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "No se pudo cargar el detalle.") }
                }
        }
    }

    /** Se llama al volver a esta pantalla (ej. tras editar) para refrescar sin perder el estado del scroll. */
    fun recargarSiCorresponde() {
        cargadoParaId?.let { cargar(it) }
    }
}
