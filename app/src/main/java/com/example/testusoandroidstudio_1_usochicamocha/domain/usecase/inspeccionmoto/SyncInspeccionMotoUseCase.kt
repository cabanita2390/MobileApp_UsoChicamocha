package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.InspeccionMotoRepository
import javax.inject.Inject

class SyncInspeccionMotoUseCase @Inject constructor(
    private val repository: InspeccionMotoRepository
) {
    suspend operator fun invoke(inspeccion: InspeccionMotoPendiente): Result<Unit> =
        repository.syncOne(inspeccion)
}
