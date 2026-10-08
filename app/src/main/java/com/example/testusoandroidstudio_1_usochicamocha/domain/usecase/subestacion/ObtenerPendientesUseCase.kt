package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Citas del año hasta el mes actual (online). En enero y febrero suma las no ejecutadas del año
 * anterior ([VentanaVencidas]) para que una vencida de diciembre se pueda registrar contra su cita.
 */
class ObtenerPendientesUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    /** Fecha de hoy (se reemplaza en tests). */
    internal var hoy: () -> LocalDate = { LocalDate.now() }

    suspend operator fun invoke(anio: Int, mesActual: Int): Result<List<CitaProgramada>> {
        val actual = repository.getPendientesDelAnio(anio, mesActual)
        if (actual.isFailure || !VentanaVencidas.incluyeAnioAnterior(hoy())) return actual
        // Si el año anterior no se puede traer, se sigue con el actual (mejor que nada).
        val anterior = repository.getPendientesDelAnio(anio - 1, 12).getOrNull().orEmpty().filter { !it.cumple }
        return Result.success(anterior + actual.getOrThrow())
    }
}
