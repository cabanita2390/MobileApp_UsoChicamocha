package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.InspeccionMotoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.InspeccionMotoRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.InspeccionMotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InspeccionMotoRepositoryImpl @Inject constructor(
    private val dao: InspeccionMotoDao,
    private val apiService: ApiService
) : InspeccionMotoRepository {

    companion object {
        private const val TAG = "InspeccionMotoRepo"
    }

    override suspend fun saveLocally(inspeccion: InspeccionMotoPendiente) {
        dao.insertInspeccion(inspeccion.toEntity())
        Log.d(TAG, "✅ Inspección guardada localmente: uuid=${inspeccion.uuid}")
    }

    override suspend fun syncOne(inspeccion: InspeccionMotoPendiente): Result<Unit> {
        return try {
            // 1. Verificar si ya fue sincronizada (evita duplicados)
            val alreadySynced = dao.isAlreadySynced(inspeccion.uuid)
            if (alreadySynced == true) {
                Log.d(TAG, "✅ Inspección ${inspeccion.uuid} ya sincronizada, omitiendo")
                return Result.success(Unit)
            }

            // 2. Obtener lock atómico (evita doble sincronización simultánea)
            val lockResult = dao.acquireLock(inspeccion.uuid)
            if (lockResult == 0) {
                Log.d(TAG, "🔒 Inspección ${inspeccion.uuid} ya siendo sincronizada por otro proceso")
                return Result.success(Unit)
            }

            Log.d(TAG, "🔄 Sincronizando inspección ${inspeccion.uuid}...")

            // 3. Construir el request y llamar a la API
            val request = InspeccionMotoRequest(
                idVehiculo = inspeccion.idVehiculo,
                idUbicacion = inspeccion.idUbicacion,
                kilometrajeReportado = inspeccion.kilometrajeReportado,
                estadoGeneral = inspeccion.estadoGeneral,
                observacionesFinales = inspeccion.observacionesFinales,
                vigenciaSoat = inspeccion.vigenciaSoat,
                estadoSoat = inspeccion.estadoSoat,
                vigenciaRevision = inspeccion.vigenciaRevision,
                estadoRevision = inspeccion.estadoRevision,
                vigenciaLicencia = inspeccion.vigenciaLicencia,
                estadoLicencia = inspeccion.estadoLicencia,
                imagenSoat = inspeccion.imagenSoat,
                imagenRevision = inspeccion.imagenRevision,
                imagenLicencia = inspeccion.imagenLicencia
            )

            val response = apiService.saveInspeccionMoto(request)

            if (response.isSuccessful && response.body() != null) {
                val serverId = response.body()!!
                dao.markAsSynced(inspeccion.uuid, serverId)
                Log.d(TAG, "✅ Inspección ${inspeccion.uuid} sincronizada con serverId=$serverId")
                Result.success(Unit)
            } else {
                dao.releaseLock(inspeccion.uuid)
                Log.e(TAG, "❌ Error al sincronizar inspección ${inspeccion.uuid}: ${response.code()}")
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            dao.releaseLock(inspeccion.uuid)
            Log.e(TAG, "❌ Excepción al sincronizar inspección ${inspeccion.uuid}", e)
            Result.failure(e)
        }
    }

    override fun getPending(): Flow<List<InspeccionMotoPendiente>> =
        dao.getPendingInspecciones().map { list -> list.map { it.toDomain() } }

    override suspend fun getPendingList(): List<InspeccionMotoPendiente> =
        dao.getPendingInspeccionesList().map { it.toDomain() }

    override suspend fun resetStuckSyncing() {
        dao.resetStuckSyncing()
        Log.d(TAG, "🔁 Locks colgados de inspecciones moto limpiados")
    }
}
