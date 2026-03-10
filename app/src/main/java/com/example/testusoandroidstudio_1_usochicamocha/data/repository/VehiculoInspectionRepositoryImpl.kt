package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.DocumentoVehiculoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoInspectionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toVehiculoItem
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.VehiculoInspectionRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VehiculoInspectionRepositoryImpl @Inject constructor(
    private val vehiculoInspectionDao: VehiculoInspectionDao,
    private val vehiculoDao: VehiculoDao,
    private val documentoVehiculoDao: DocumentoVehiculoDao,
    private val apiService: ApiService
) : VehiculoInspectionRepository {

    companion object {
        private const val TAG = "VehiculoInspRepo"
    }

    override suspend fun saveInspectionLocally(entity: VehiculoInspectionEntity): Long {
        Log.d(TAG, "Saving inspection locally for plate: ${entity.placaVehiculo}")
        return vehiculoInspectionDao.insertInspection(entity)
    }

    override suspend fun getPendingInspections(): List<VehiculoInspectionEntity> {
        return vehiculoInspectionDao.getPendingInspections()
    }

    override suspend fun syncInspection(inspection: VehiculoInspectionEntity): Result<Unit> {
        Log.d(TAG, "🔄 Starting sync for vehicle inspection: ${inspection.UUID}")
        
        return try {
            // 1. Verificar si ya está sincronizado
            val isAlreadySynced = vehiculoInspectionDao.isInspectionAlreadySynced(inspection.UUID)
            if (isAlreadySynced == true) {
                Log.d(TAG, "✅ Inspection ${inspection.UUID} already synced locally, skipping")
                return Result.success(Unit)
            }

            // 2. Intentar obtener lock
            val lockResult = vehiculoInspectionDao.acquireInspectionLock(inspection.UUID)
            if (lockResult == 0) {
                Log.d(TAG, "🔒 Inspection ${inspection.UUID} is being synced by another process")
                return Result.success(Unit)
            }

            // 3. Realizar sincronización
            val request = inspection.toRequest()
            val response = apiService.submitVehiculoInspection(request)

            if (response.isSuccessful && response.body() != null) {
                val serverId = response.body()!!.id
                vehiculoInspectionDao.markAsSynced(inspection.UUID, serverId)
                Log.d(TAG, "✅ Vehicle inspection ${inspection.UUID} synced successfully with serverId: $serverId")
                Result.success(Unit)
            } else {
                vehiculoInspectionDao.markAsNotSyncing(inspection.UUID)
                val errorMsg = "Error ${response.code()} syncing inspection ${inspection.UUID}"
                Log.e(TAG, "❌ $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            vehiculoInspectionDao.markAsNotSyncing(inspection.UUID)
            Log.e(TAG, "❌ Exception syncing vehicle inspection ${inspection.UUID}", e)
            Result.failure(e)
        }
    }

    override fun getAllInspectionsFlow(): Flow<List<VehiculoInspectionEntity>> {
        return vehiculoInspectionDao.getAllInspectionsFlow()
    }

    override fun getLocalVehiclesFlow(): Flow<List<VehiculoEntity>> {
        return vehiculoDao.getAllVehicles()
    }

    override suspend fun syncVehiclesCatalog(): Result<Unit> {
        Log.d(TAG, "🔄 Syncing vehicles catalog...")
        return try {
            val response = apiService.getVehicles()
            if (response.isSuccessful && response.body() != null) {
                val vehicles = response.body()!!.map { it.toVehiculoItem().toEntity() }
                vehiculoDao.insertVehicles(vehicles)
                Log.d(TAG, "✅ Vehicles catalog synced: ${vehicles.size} vehicles")
                Result.success(Unit)
            } else {
                Log.e(TAG, "❌ Error syncing vehicles catalog: ${response.code()}")
                Result.failure(Exception("Error ${response.code()} syncing vehicles catalog"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception syncing vehicles catalog", e)
            Result.failure(e)
        }
    }

    override suspend fun getCachedDocuments(placa: String): List<DocumentoVehiculoEntity> {
        return documentoVehiculoDao.getByPlaca(placa)
    }

    override suspend fun refreshCachedDocuments(placa: String, documentos: List<DocumentoVehiculoEntity>) {
        documentoVehiculoDao.refreshForPlaca(placa, documentos)
    }

    override suspend fun updateVehicleMileage(placa: String, km: Int) {
        Log.d(TAG, "Updating mileage for vehicle $placa in catalog: $km")
        vehiculoDao.updateKilometraje(placa, km)
    }

    private fun VehiculoInspectionEntity.toRequest(): VehiculoInspectionRequest {
        return VehiculoInspectionRequest(
            placaVehiculo = this.placaVehiculo,
            marca = this.marca,
            tipoVehiculo = this.tipoVehiculo,
            kilometrajeReportado = this.kilometrajeReportado,
            responsableInspeccion = this.responsableInspeccion,
            aprobadoRuta = this.aprobadoRuta,
            observacionesFinales = this.observacionesFinales,
            nivelAceite = this.nivelAceite,
            nivelRefrigerante = this.nivelRefrigerante,
            nivelFrenos = this.nivelFrenos,
            estadoLlantas = this.estadoLlantas,
            lucesGeneral = this.lucesGeneral,
            estadoVisual = this.estadoVisual,
            limpiezaGeneral = this.limpiezaGeneral,
            checkSoat = this.checkSoat,
            checkTecno = this.checkTecno,
            checkLicencia = this.checkLicencia,
            checkExtintor = this.checkExtintor,
            vigenciaExtintor = this.vigenciaExtintor,
            fechaVencSoat = this.fechaVencSoat,
            fechaVencTecno = this.fechaVencTecno,
            fechaVencLicencia = this.fechaVencLicencia,
            tieneBotiquin = this.tieneBotiquin,
            tieneSeñalizacion = this.tieneSeñalizacion,
            tieneLineasEmergencia = this.tieneLineasEmergencia,
            tieneLlantaRepuesto = this.tieneLlantaRepuesto,
            tieneGatoHidraulico = this.tieneGatoHidraulico,
            saludFisica = this.saludFisica,
            saludMental = this.saludMental,
            sobrio = this.sobrio,
            medicamentos = this.medicamentos,
            conscienteResponsabilidad = this.conscienteResponsabilidad,
            condicionParaConducir = this.condicionParaConducir
        )
    }
}
