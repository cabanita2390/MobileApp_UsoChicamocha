package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

/**
 * Refresca en segundo plano el caché de cumplimiento de los meses 1..mesActual del año
 * (best-effort: no bloquea la UI, que ya lee de Room vía [ObtenerCumplimientoAnioLocalUseCase]).
 * Se dispara al entrar a Pendientes/Home y también periódicamente desde SyncDataWorker,
 * igual que el catálogo de estaciones/actividades.
 */
class SincronizarCumplimientoAnioUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(anio: Int, mesActual: Int): Result<Unit> {
        return repository.sincronizarCumplimientoDelAnio(anio, mesActual)
    }
}
