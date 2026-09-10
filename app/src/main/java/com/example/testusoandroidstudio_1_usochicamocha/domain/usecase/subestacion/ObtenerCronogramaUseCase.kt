package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

/**
 * Cronograma de un mes, todas las estaciones (usa el endpoint de cumplimiento sin
 * estacionId, que devuelve todas las citas de ese mes con su estado ya calculado).
 */
class ObtenerCronogramaUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(anio: Int, mes: Int): Result<List<CitaProgramada>> {
        return repository.getCumplimientoPorMes(anio, mes)
    }
}
