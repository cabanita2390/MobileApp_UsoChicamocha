package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MaintenanceEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MaintenanceRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingMaintenanceFormsUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(): Flow<List<Maintenance>> {
        return repository.getPendingMaintenanceForms()
    }
}
