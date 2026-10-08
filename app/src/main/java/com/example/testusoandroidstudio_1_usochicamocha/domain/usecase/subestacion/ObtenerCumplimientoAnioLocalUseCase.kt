package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

/**
 * Cumplimiento del año completo (incluye citas futuras ya ejecutadas), leído del caché en Room — usada por
 * PendientesViewModel y SubestacionHomeViewModel (offline-first). No confundir con
 * [ObtenerPendientesUseCase], que sigue siendo online-first y la sigue usando
 * CapturaViewModel para el wizard de captura (necesita datos al momento, no un caché
 * que puede estar desactualizado).
 *
 * En enero y febrero suma las citas no ejecutadas del año anterior ([VentanaVencidas]).
 */
class ObtenerCumplimientoAnioLocalUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    /** Fecha de hoy (se reemplaza en tests). */
    internal var hoy: () -> LocalDate = { LocalDate.now() }

    operator fun invoke(anio: Int): Flow<List<CitaProgramada>> {
        val delAnio = repository.getCumplimientoLocalDelAnioFlow(anio)
        if (!VentanaVencidas.incluyeAnioAnterior(hoy())) return delAnio
        return combine(repository.getCumplimientoLocalDelAnioFlow(anio - 1), delAnio) { anterior, actual ->
            anterior.filter { !it.cumple } + actual
        }
    }
}
