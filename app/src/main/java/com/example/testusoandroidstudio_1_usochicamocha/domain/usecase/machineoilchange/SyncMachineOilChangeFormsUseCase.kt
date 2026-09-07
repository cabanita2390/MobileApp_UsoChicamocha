package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MachineOilChangeRepository
import javax.inject.Inject

class SyncMachineOilChangeFormsUseCase @Inject constructor(
    private val repository: MachineOilChangeRepository
) {
    suspend operator fun invoke(machineOilChangeForm: MachineOilChangeForm): Result<Unit> {
        // 1. Intenta sincronizar el formulario con el backend
        val syncResult = repository.syncMachineOilChangeForm(machineOilChangeForm)

        // 2. Si la sincronización es exitosa, lo borra de la base de datos local
        if (syncResult.isSuccess) {
            return repository.deleteMachineOilChangeForm(machineOilChangeForm.id)
        }

        // 3. Si la sincronización falla, devuelve el error original
        return syncResult
    }
}
