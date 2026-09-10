package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

class ObtenerDetalleEjecucionUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(id: Long): Result<EjecucionDetalle> = repository.getDetalleEjecucion(id)
}
