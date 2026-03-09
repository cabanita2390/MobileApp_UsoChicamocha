package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import kotlinx.coroutines.flow.Flow

interface VehiculoInspectionRepository {
    suspend fun saveInspectionLocally(entity: VehiculoInspectionEntity): Long
    suspend fun getPendingInspections(): List<VehiculoInspectionEntity>
    suspend fun syncInspection(inspection: VehiculoInspectionEntity): Result<Unit>
    fun getAllInspectionsFlow(): Flow<List<VehiculoInspectionEntity>>

    // Catálogo de vehículos
    fun getLocalVehiclesFlow(): Flow<List<VehiculoEntity>>
    suspend fun syncVehiclesCatalog(): Result<Unit>
}
