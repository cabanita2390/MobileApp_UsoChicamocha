package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Detalle de una ejecución leído del caché en Room (null = todavía no se ha consultado
 * con éxito desde este dispositivo) — usada por DetalleViewModel. No confundir con
 * [ObtenerDetalleEjecucionUseCase], que sigue siendo online-first y la sigue usando
 * CapturaViewModel (modo edición del wizard).
 */
class ObtenerDetalleEjecucionLocalUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(id: Long): Flow<EjecucionDetalle?> {
        return repository.getDetalleLocalFlow(id)
    }
}
