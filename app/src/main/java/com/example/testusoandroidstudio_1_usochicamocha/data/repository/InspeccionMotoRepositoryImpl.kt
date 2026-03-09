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
                val stillPending = !dao.isAlreadySynced(inspeccion.uuid)!!
                if (stillPending) {
                    Log.w(TAG, "🔒 Inspección ${inspeccion.uuid} ya está en proceso de sync por otro hilo. Abortando este intento.")
                    // Devolvemos success porque alguien más lo está haciendo (o lo hará el worker)
                    return Result.success(Unit)
                } else {
                    Log.d(TAG, "✅ Inspección ${inspeccion.uuid} terminó de sincronizarse justo ahora.")
                    return Result.success(Unit)
                }
            }

            Log.i(TAG, "🚀 [syncOne] Iniciando POST a la API para inspección: ${inspeccion.uuid}")

            // 3. Construir el request y llamar a la API
            val request = InspeccionMotoRequest(
                idVehiculo = inspeccion.idVehiculo,
                kilometrajeReportado = inspeccion.kilometrajeReportado,
                estadoVehiculo = inspeccion.estadoVehiculo,
                observacionesFinales = inspeccion.observacionesFinales,
                checkSoat = inspeccion.checkSoat,
                checkTecno = inspeccion.checkTecno,
                checkLicencia = inspeccion.checkLicencia,
                checkExtintor = inspeccion.checkExtintor,
                fechaSoat = inspeccion.fechaSoat,
                fechaTecno = inspeccion.fechaTecno,
                fechaLicencia = inspeccion.fechaLicencia,
                idUbicacion = inspeccion.idUbicacion,
            )

            val response = apiService.saveInspeccionMoto(request)

            if (response.isSuccessful) {
                val serverId = response.body() ?: 0L
                dao.markAsSynced(inspeccion.uuid, serverId)
                Log.d(TAG, "✅ [syncOne] ÉXITO: Inspección ${inspeccion.uuid} sincronizada. ServerId: $serverId")
                Result.success(Unit)
            } else {
                dao.releaseLock(inspeccion.uuid)
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ [syncOne] ERROR API: ${inspeccion.uuid} - Code: $errorCode - Body: $errorBody")
                
                // Log específico para problemas comunes
                when (errorCode) {
                    401 -> Log.e(TAG, "🔑 [syncOne] Error de autenticación: Token expirado o inválido")
                    403 -> Log.e(TAG, "🚫 [syncOne] Error de autorización: Usuario no tiene permisos (MECANIC/ADMIN)")
                    404 -> Log.e(TAG, "📍 [syncOne] Error 404: Endpoint no encontrado")
                }
                
                Result.failure(Exception("Error servidor ($errorCode): $errorBody"))
            }
        } catch (e: Exception) {
            dao.releaseLock(inspeccion.uuid)
            Log.e(TAG, "❌ [syncOne] EXCEPCIÓN: ${inspeccion.uuid}", e)
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
