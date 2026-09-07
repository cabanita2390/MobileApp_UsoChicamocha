package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MaintenanceRepository
import javax.inject.Inject

class SaveMaintenanceFormUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(maintenance: Maintenance): Result<Unit> {

        return repository.saveMaintenanceLocally(maintenance)
    }
}