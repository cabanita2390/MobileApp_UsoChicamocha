package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.maintenance

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MaintenanceRepository
import javax.inject.Inject

class SyncMaintenanceFormsUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(maintenanceForm: Maintenance): Result<Unit> {
        // 1. Intenta sincronizar el formulario con el backend
        val syncResult = repository.syncMaintenanceForm(maintenanceForm)

        // 2. Si la sincronización es exitosa, lo borra de la base de datos local
        if (syncResult.isSuccess) {
            return repository.deleteMaintenanceForm(maintenanceForm.id)
        }

        // 3. Si la sincronización falla, devuelve el error original
        return syncResult
    }
}