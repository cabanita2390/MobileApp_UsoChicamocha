package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.llantas

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.LlantasRepository
import javax.inject.Inject

class SincronizarPerfilesLlantasUseCase @Inject constructor(
    private val repository: LlantasRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.sincronizar()
}
