package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import javax.inject.Inject

class GetPendingVehiculoInspectionsUseCase @Inject constructor(
    private val repository: VehiculoInspectionRepository
) {
    suspend operator fun invoke(): List<VehiculoInspectionEntity> {
        return repository.getPendingInspections()
    }
}
