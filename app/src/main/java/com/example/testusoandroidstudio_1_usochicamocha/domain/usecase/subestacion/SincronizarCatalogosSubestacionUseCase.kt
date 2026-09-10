package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

class SincronizarCatalogosSubestacionUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.sincronizarCatalogos()
}
