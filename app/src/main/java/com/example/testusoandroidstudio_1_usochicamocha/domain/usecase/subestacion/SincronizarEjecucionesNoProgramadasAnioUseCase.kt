package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

/**
 * Refresca en segundo plano el caché de ejecuciones NO programadas del año (best-effort: no
 * bloquea la UI, que ya lee de Room vía [ObtenerEjecucionesNoProgramadasAnioLocalUseCase]).
 * Se dispara al entrar a Pendientes junto con [SincronizarCumplimientoAnioUseCase], y también
 * desde SyncDataWorker en el mismo punto que ese caso de uso.
 */
class SincronizarEjecucionesNoProgramadasAnioUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(anio: Int): Result<Unit> {
        return repository.sincronizarEjecucionesNoProgramadasDelAnio(anio)
    }
}
