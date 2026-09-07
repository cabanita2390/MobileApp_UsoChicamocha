package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import kotlinx.coroutines.flow.Flow

interface MachineOilChangeRepository {
    suspend fun saveMachineOilChangeLocally(machineOilChange: MachineOilChangeForm): Result<Unit>
    fun getPendingMachineOilChangeForms(): Flow<List<MachineOilChangeForm>>

    suspend fun syncMachineOilChangeForm(machineOilChange: MachineOilChangeForm): Result<Unit>
    suspend fun deleteMachineOilChangeForm(id: Int): Result<Unit>
    suspend fun getMachineOilChangeById(id: Int): MachineOilChangeForm?
}
