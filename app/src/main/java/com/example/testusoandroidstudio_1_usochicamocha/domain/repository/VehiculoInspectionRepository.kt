package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity
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
    
    // Caché de documentos
    suspend fun getCachedDocuments(placa: String): List<DocumentoVehiculoEntity>
    suspend fun refreshCachedDocuments(placa: String, documentos: List<DocumentoVehiculoEntity>)

    suspend fun updateVehicleMileage(placa: String, km: Int)

    // Sincronización masiva de documentos
    suspend fun syncAllVehiclesDocuments(): Result<Unit>
}
