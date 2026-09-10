package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

class GuardarEjecucionLocalUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(ejecucion: Ejecucion, imageUris: List<String>) {
        repository.saveEjecucionLocally(ejecucion, imageUris)
    }
}
