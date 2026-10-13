package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GuardarEjecucionLocalUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(ejecucion: Ejecucion, imageUris: List<String>) {
        repository.saveEjecucionLocally(ejecucion, imageUris)
    }

    fun observarEnvio(uuidCliente: String): Flow<Ejecucion?> = repository.observarEjecucionLocal(uuidCliente)
}
