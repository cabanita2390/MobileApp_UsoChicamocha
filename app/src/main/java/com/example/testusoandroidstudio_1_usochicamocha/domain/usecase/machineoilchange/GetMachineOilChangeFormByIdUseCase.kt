package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MaintenanceRepository
import javax.inject.Inject

class GetMaintenanceByIdUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(id: Int): Maintenance? {
        return repository.getMaintenanceById(id)
    }
}
