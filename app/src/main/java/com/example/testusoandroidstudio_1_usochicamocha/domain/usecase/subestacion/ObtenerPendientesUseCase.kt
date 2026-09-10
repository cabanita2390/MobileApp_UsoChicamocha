package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

class ObtenerPendientesUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(anio: Int, mesActual: Int): Result<List<CitaProgramada>> {
        return repository.getPendientesDelAnio(anio, mesActual)
    }
}
