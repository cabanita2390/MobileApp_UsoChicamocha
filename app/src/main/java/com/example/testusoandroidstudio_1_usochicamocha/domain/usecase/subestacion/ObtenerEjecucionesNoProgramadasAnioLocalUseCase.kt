package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Ejecuciones NO programadas (sin cita de cronograma) del año hasta el mes actual, leídas
 * del caché en Room — usada por PendientesViewModel junto con
 * [ObtenerCumplimientoAnioLocalUseCase] para armar la pestaña "Realizadas": esa pestaña
 * necesita tanto las citas programadas cumplidas como las actividades civiles hechas fuera
 * de cronograma, y v_mant_cumplimiento (fuente de CumplimientoCacheEntity) nunca trae estas
 * últimas porque arranca desde mant_programacion.
 */
class ObtenerEjecucionesNoProgramadasAnioLocalUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(anio: Int, mesActual: Int): Flow<List<CitaProgramada>> {
        return repository.getEjecucionesNoProgramadasLocalDelAnioFlow(anio, mesActual)
    }
}
