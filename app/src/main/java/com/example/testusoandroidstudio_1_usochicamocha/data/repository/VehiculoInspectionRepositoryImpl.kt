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
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.VehicleOilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.VehiculoInspectionRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
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
                syncVehicleOilIfNeeded(inspection)
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
                val apiVehicles = response.body()!!
                Log.d(TAG, "📡 Received ${apiVehicles.size} vehicles from API")
                
                val entities = apiVehicles.map { it.toVehiculoItem().toEntity() }
                
                // Usamos clearAndInsert (transaccional) para asegurar que el catálogo
                // se actualiza atómicamente y no quedan duplicados ni estados intermedios vacíos.
                vehiculoDao.clearAndInsert(entities)
                
                Log.d(TAG, "✅ Vehicles catalog updated successfully in local DB (${entities.size} entities)")
                Result.success(Unit)
            } else {
                val code = response.code()
                val errorMsg = when (code) {
                    401, 403 -> "Sin permisos para acceder a vehículos (error $code). Contacta al administrador para actualizar tu rol."
                    else -> "Error sincronizando catálogo de vehículos: $code ${response.message()}"
                }
                Log.e(TAG, "❌ $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception syncing vehicles catalog: ${e.message}", e)
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

    override suspend fun syncAllVehiclesDocuments(): Result<Unit> {
        Log.d(TAG, "🔄 Starting massive vehicle documents sync...")
        return try {
            val vehiclesLocal = vehiculoDao.getAllVehicles().first()
            if (vehiclesLocal.isEmpty()) return Result.success(Unit)

            for (vehiculo in vehiclesLocal) {
                try {
                    val resp = apiService.getDocumentosVehiculo(vehiculo.idVehiculo)
                    if (resp.isSuccessful && resp.body() != null) {
                        val doc = resp.body()!!
                        
                        val soatDB  = doc.fechaVencSoat?.take(7)  ?: ""
                        val tecnoDB = doc.fechaVencTecno?.take(7) ?: ""
                        val extDB   = doc.fechaVencExtintor?.take(7) ?: ""
                        // LICENCIA no se sincroniza aquí: viene del perfil del conductor en la pantalla de inspección

                        val docsToCache = listOf(
                            DocumentoVehiculoEntity(placa = vehiculo.placa, tipoDocumento = "SOAT",     vigencia = soatDB,  imagenUrl = doc.urlImagenSoat,     kilometrajeActual = vehiculo.kilometrajeActual),
                            DocumentoVehiculoEntity(placa = vehiculo.placa, tipoDocumento = "TECNO",    vigencia = tecnoDB, imagenUrl = doc.urlImagenTecno,    kilometrajeActual = vehiculo.kilometrajeActual),
                            DocumentoVehiculoEntity(placa = vehiculo.placa, tipoDocumento = "EXTINTOR", vigencia = extDB,   imagenUrl = doc.urlImagenExtintor, kilometrajeActual = vehiculo.kilometrajeActual)
                        )
                        documentoVehiculoDao.refreshForPlaca(vehiculo.placa, docsToCache)
                        Log.d(TAG, "✅ Documents cached for vehicle: ${vehiculo.placa}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Failed to sync documents for ${vehiculo.placa}: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Critical error during massive vehicle document sync", e)
            Result.failure(e)
        }
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
            fechaVencSoat = this.fechaVencSoat,
            fechaVencTecno = this.fechaVencTecno,
            fechaVencLicencia = this.fechaVencLicencia,
            vigenciaExtintor = this.vigenciaExtintor,
            urlImagenSoat = this.urlImagenSoat.ifBlank { null },
            urlImagenTecno = this.urlImagenTecno.ifBlank { null },
            urlImagenLicencia = this.urlImagenLicencia.ifBlank { null },
            urlImagenExtintor = this.urlImagenExtintor.ifBlank { null },
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
            condicionParaConducir = this.condicionParaConducir,
            idUbicacion = null,
        )
    }

    private suspend fun syncVehicleOilIfNeeded(inspection: VehiculoInspectionEntity) {
        if (!inspection.registrarCambioAceite) return
        val brandId = inspection.oilBrandId
        val interval = inspection.oilIntervalKm
        if (brandId == null || interval == null || interval <= 0 || inspection.oilType.isBlank()) {
            Log.w(TAG, "⚠️ Cambio de aceite marcado pero faltan datos (marca/intervalo/tipo). Se omite POST oil-change.")
            return
        }
        try {
            val req = VehicleOilChangeRequest(
                placa = inspection.placaVehiculo,
                dateStamp = LocalDateTime.now().toString(),
                oilType = inspection.oilType.trim(),
                brandId = brandId,
                quantity = inspection.oilQuantity,
                kmAtChange = inspection.kilometrajeReportado,
                intervalKm = interval,
                airFilterChanged = inspection.oilAirFilterChanged,
            )
            val oilResp = apiService.registerVehicleOilChange(req)
            if (oilResp.isSuccessful) {
                Log.d(TAG, "✅ Cambio de aceite registrado para ${inspection.placaVehiculo}")
            } else {
                Log.e(TAG, "❌ Error POST oil-change: ${oilResp.code()} ${oilResp.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al registrar cambio de aceite", e)
        }
    }
}
