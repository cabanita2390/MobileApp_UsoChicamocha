package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObtenerEjecucionesPendientesUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(): Flow<List<Ejecucion>> = repository.getPendingEjecuciones()
}
