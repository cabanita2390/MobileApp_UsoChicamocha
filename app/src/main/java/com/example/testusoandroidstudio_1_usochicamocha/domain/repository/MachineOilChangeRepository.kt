package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    suspend fun saveMaintenanceLocally(maintenance: Maintenance): Result<Unit>
    fun getPendingMaintenanceForms(): Flow<List<Maintenance>>

    suspend fun syncMaintenanceForm(maintenance: Maintenance): Result<Unit>
    suspend fun deleteMaintenanceForm(id: Int): Result<Unit>
    suspend fun getMaintenanceById(id: Int): Maintenance?
}
