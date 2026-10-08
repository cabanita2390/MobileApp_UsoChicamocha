package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Refresca en segundo plano el caché de cumplimiento de los 12 meses del año
 * (best-effort: no bloquea la UI, que ya lee de Room vía [ObtenerCumplimientoAnioLocalUseCase]).
 * Se dispara al entrar a Pendientes/Home y también periódicamente desde SyncDataWorker,
 * igual que el catálogo de estaciones/actividades.
 *
 * En enero y febrero también refresca el año anterior ([VentanaVencidas]); si eso falla no
 * invalida el año actual.
 */
class SincronizarCumplimientoAnioUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    /** Fecha de hoy (se reemplaza en tests). */
    internal var hoy: () -> LocalDate = { LocalDate.now() }

    suspend operator fun invoke(anio: Int): Result<Unit> {
        if (VentanaVencidas.incluyeAnioAnterior(hoy())) repository.sincronizarCumplimientoDelAnio(anio - 1)
        return repository.sincronizarCumplimientoDelAnio(anio)
    }
}
