package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.InspeccionMotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingInspeccionesMotoUseCase @Inject constructor(
    private val repository: InspeccionMotoRepository
) {
    operator fun invoke(): Flow<List<InspeccionMotoPendiente>> = repository.getPending()

    suspend fun asList(): List<InspeccionMotoPendiente> = repository.getPendingList()
}
