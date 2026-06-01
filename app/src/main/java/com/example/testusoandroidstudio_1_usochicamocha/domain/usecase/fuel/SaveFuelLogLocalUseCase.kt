package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.fuel

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelLogEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FuelRepository
import javax.inject.Inject

class SaveFuelLogLocalUseCase @Inject constructor(
    private val repository: FuelRepository
) {
    suspend operator fun invoke(entity: FuelLogEntity): Long = repository.saveLocal(entity)
}
