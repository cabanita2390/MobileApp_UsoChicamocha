package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MachineOilChangeRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingMachineOilChangeFormsUseCase @Inject constructor(
    private val repository: MachineOilChangeRepository
) {
    operator fun invoke(): Flow<List<MachineOilChangeForm>> {
        return repository.getPendingMachineOilChangeForms()
    }
}
