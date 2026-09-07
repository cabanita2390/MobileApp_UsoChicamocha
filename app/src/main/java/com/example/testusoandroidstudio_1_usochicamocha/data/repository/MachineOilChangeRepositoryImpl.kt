package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MachineOilChangeDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toOilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MachineOilChangeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MachineOilChangeRepositoryImpl @Inject constructor(
    private val machineOilChangeDao: MachineOilChangeDao,
    private val apiService: ApiService
) : MachineOilChangeRepository {

    override suspend fun saveMachineOilChangeLocally(machineOilChange: MachineOilChangeForm): Result<Unit> {
        return try {
            val entity = machineOilChange.toEntity()
            machineOilChangeDao.insert(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPendingMachineOilChangeForms(): Flow<List<MachineOilChangeForm>> {
        return machineOilChangeDao.getPendingMachineOilChangeForms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncMachineOilChangeForm(machineOilChange: MachineOilChangeForm): Result<Unit> {
        return try {
            // Mark as syncing
            machineOilChangeDao.markAsSyncing(machineOilChange.id)

            val request = machineOilChange.toOilChangeRequest()
            val response = when (machineOilChange.type) {
                "motor" -> apiService.syncMotorOilChange(request)
                "hydraulic" -> apiService.syncHydraulicOilChange(request)
                else -> {
                    machineOilChangeDao.markAsNotSyncing(machineOilChange.id)
                    return Result.failure(IllegalArgumentException("Tipo de mantenimiento desconocido: ${machineOilChange.type}"))
                }
            }

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    if (!errorBody.isNullOrEmpty()) {
                        org.json.JSONObject(errorBody).optString("message", errorBody)
                    } else {
                        "Error ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    errorBody ?: "Error ${response.code()}: ${response.message()}"
                }

                machineOilChangeDao.markAsSyncFailed(machineOilChange.id, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            machineOilChangeDao.markAsNotSyncing(machineOilChange.id)
            Result.failure(e)
        }
    }

    override suspend fun deleteMachineOilChangeForm(id: Int): Result<Unit> {
        return try {
            machineOilChangeDao.deleteById(id)
            Result.success(Unit)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMachineOilChangeById(id: Int): MachineOilChangeForm? {
        return machineOilChangeDao.getMachineOilChangeById(id)?.toDomain()
    }
}
