package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Cumplimiento de todo el año hasta el mes actual, leído del caché en Room — usada por
 * PendientesViewModel y SubestacionHomeViewModel (offline-first). No confundir con
 * [ObtenerPendientesUseCase], que sigue siendo online-first y la sigue usando
 * CapturaViewModel para el wizard de captura (necesita datos al momento, no un caché
 * que puede estar desactualizado).
 */
class ObtenerCumplimientoAnioLocalUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(anio: Int, mesActual: Int): Flow<List<CitaProgramada>> {
        return repository.getCumplimientoLocalDelAnioFlow(anio, mesActual)
    }
}
