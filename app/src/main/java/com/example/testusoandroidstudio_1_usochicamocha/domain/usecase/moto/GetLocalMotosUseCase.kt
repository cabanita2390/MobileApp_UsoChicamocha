package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalMotosUseCase @Inject constructor(
    private val motoRepository: MotoRepository
) {
    operator fun invoke(): Flow<List<Moto>> = motoRepository.getLocalMotos()
}
