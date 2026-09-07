package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MachineOilChangeRepository
import javax.inject.Inject

class SaveMachineOilChangeFormUseCase @Inject constructor(
    private val repository: MachineOilChangeRepository
) {
    suspend operator fun invoke(machineOilChange: MachineOilChangeForm): Result<Unit> {

        return repository.saveMachineOilChangeLocally(machineOilChange)
    }
}
