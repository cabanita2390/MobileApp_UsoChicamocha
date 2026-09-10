package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionEdicion
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

class EditarEjecucionUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(id: Long, edicion: EjecucionEdicion): Result<EjecucionDetalle> {
        return repository.editarEjecucion(id, edicion)
    }
}
