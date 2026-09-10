package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEjecucionesPendientesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Solo alimenta la píldora roja de conteo del botón de sincronización en
 * `SubestacionTopBar` — independiente del ViewModel propio de cada pantalla para no
 * duplicar la inyección de `ObtenerEjecucionesPendientesUseCase` en las 5 pantallas
 * que usan el header.
 */
@HiltViewModel
class PendingSyncBadgeViewModel @Inject constructor(
    obtenerEjecucionesPendientesUseCase: ObtenerEjecucionesPendientesUseCase
) : ViewModel() {
    val pendingCount: StateFlow<Int> = obtenerEjecucionesPendientesUseCase()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
