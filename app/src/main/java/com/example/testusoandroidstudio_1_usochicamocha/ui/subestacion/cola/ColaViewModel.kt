package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.cola

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEjecucionesColaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColaViewModel @Inject constructor(
    obtenerEjecucionesColaUseCase: ObtenerEjecucionesColaUseCase,
    private val localSyncCoordinator: LocalSyncCoordinator
) : ViewModel() {

    /**
     * `actuales = null` marca la semilla inicial de `scan` (antes de la primera
     * emisión real) — distinto de una lista real vacía, para no contar el primer
     * fetch entero como "recién confirmado". Un uuidCliente que estaba en la
     * emisión anterior y ya no está en la actual se sincronizó con éxito y salió
     * de `getColaFlow()` (que solo trae `isSynced=0`) — se cuenta como confirmado.
     * Mismo espíritu que `st.enviados`/`queueEmptyMsg` en el mock.
     */
    private data class Acumulado(val actuales: List<Ejecucion>?, val confirmadosSesion: Int)

    private val acumulado: StateFlow<Acumulado> = obtenerEjecucionesColaUseCase()
        .scan(Acumulado(actuales = null, confirmadosSesion = 0)) { previo, nuevos ->
            val confirmadosNuevos = previo.actuales?.let { anteriores ->
                val uuidsAntes = anteriores.map { it.uuidCliente }.toSet()
                val uuidsAhora = nuevos.map { it.uuidCliente }.toSet()
                (uuidsAntes - uuidsAhora).size
            } ?: 0
            Acumulado(nuevos, previo.confirmadosSesion + confirmadosNuevos)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Acumulado(null, 0))

    val pendientes: StateFlow<List<Ejecucion>> = acumulado
        .map { it.actuales ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Cuántos registros se confirmaron (sincronizaron con éxito) desde que se abrió esta pantalla. */
    val confirmadosSesion: StateFlow<Int> = acumulado
        .map { it.confirmadosSesion }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val syncStatus: StateFlow<LocalSyncCoordinator.SyncStatus> = localSyncCoordinator.syncStatus

    fun sincronizarAhora() {
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.SubstationSaved("manual")
            )
        }
    }
}
