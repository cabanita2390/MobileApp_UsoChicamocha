package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

class ObtenerEjecucionPorProgramacionUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(programacionId: Long): Result<EjecucionDetalle?> {
        return repository.getEjecucionPorProgramacion(programacionId)
    }
}
