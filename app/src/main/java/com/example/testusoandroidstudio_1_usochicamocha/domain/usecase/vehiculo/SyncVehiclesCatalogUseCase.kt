package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import javax.inject.Inject

class SyncVehiclesCatalogUseCase @Inject constructor(
    private val repository: VehiculoInspectionRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncVehiclesCatalog()
    }
}
