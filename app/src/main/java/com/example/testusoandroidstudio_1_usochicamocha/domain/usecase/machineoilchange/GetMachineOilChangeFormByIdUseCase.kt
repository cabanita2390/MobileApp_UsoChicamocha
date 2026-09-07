package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MachineOilChangeRepository
import javax.inject.Inject

class GetMachineOilChangeFormByIdUseCase @Inject constructor(
    private val repository: MachineOilChangeRepository
) {
    suspend operator fun invoke(id: Int): MachineOilChangeForm? {
        return repository.getMachineOilChangeById(id)
    }
}
