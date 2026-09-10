package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

class SincronizarEjecucionUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(ejecucion: Ejecucion): Result<Unit> {
        return repository.syncEjecucion(ejecucion)
    }
}
