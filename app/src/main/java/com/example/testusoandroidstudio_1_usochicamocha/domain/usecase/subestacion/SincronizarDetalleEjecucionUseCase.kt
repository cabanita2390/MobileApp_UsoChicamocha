package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

/**
 * Trae del backend el detalle de una ejecución y lo cachea en Room (best-effort: se
 * dispara al entrar a la pantalla Detalle, pero no bloquea la UI, que ya muestra lo
 * último cacheado vía [ObtenerDetalleEjecucionLocalUseCase]).
 */
class SincronizarDetalleEjecucionUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return repository.sincronizarDetalle(id)
    }
}
