package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import javax.inject.Inject

class SaveVehiculoInspectionUseCase @Inject constructor(
    private val repository: VehiculoInspectionRepository
) {
    suspend operator fun invoke(entity: VehiculoInspectionEntity): Long {
        return repository.saveInspectionLocally(entity)
    }
}
