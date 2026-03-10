package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MotoRepository
import javax.inject.Inject

class SyncMotosUseCase @Inject constructor(
    private val motoRepository: MotoRepository
) {
    suspend operator fun invoke(): Result<Unit> = motoRepository.syncMotos()
}
