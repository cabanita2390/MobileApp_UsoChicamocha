package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ActividadCacheDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.CumplimientoCacheDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.EjecucionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.EjecucionDetalleCacheDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.EstacionCacheDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toCacheEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EjecucionEditRequestDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EjecucionRequestDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ActividadCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionEdicion
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EstacionCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ProgramacionCita
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class SubestacionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ejecucionDao: EjecucionDao,
    private val estacionCacheDao: EstacionCacheDao,
    private val actividadCacheDao: ActividadCacheDao,
    private val imageDao: ImageDao,
    private val apiService: ApiService,
    private val cumplimientoCacheDao: CumplimientoCacheDao,
    private val ejecucionDetalleCacheDao: EjecucionDetalleCacheDao
) : SubestacionRepository {

    companion object {
        private const val TAG = "SubestacionRepositoryImpl"
        private const val DISCIPLINA = "CIVIL"
    }

    // --- Captura offline ---

    override fun getPendingEjecuciones(): Flow<List<Ejecucion>> {
        return ejecucionDao.getPendingFlow().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getEjecucionesCola(): Flow<List<Ejecucion>> {
        return ejecucionDao.getColaFlow().map { filas -> filas.map { it.entity.toDomain().copy(fotosCount = it.fotosCount) } }
    }

    override suspend fun saveEjecucionLocally(ejecucion: Ejecucion, imageUris: List<String>) {
        val entity = ejecucion.toEntity()
        ejecucionDao.insertEjecucion(entity)

        if (imageUris.isNotEmpty()) {
            val imageEntities = imageUris.map { uri ->
                ImageEntity(
                    ejecucionUUID = ejecucion.uuidCliente,
                    localUri = uri,
                    isSynced = false
                )
            }
            imageDao.insertImages(imageEntities)
        }
    }

    /**
     * Mismo flujo de idempotencia + lock atómico que FormRepositoryImpl.syncForm:
     * 1) si ya está sincronizada localmente, no hace nada; 2) intenta el lock atómico
     * (0 = otro proceso ya la está subiendo); 3) POST; 4) marca sincronizada en éxito,
     * libera el lock en fallo; 5) siempre libera el lock al final.
     */
    override suspend fun syncEjecucion(ejecucion: Ejecucion): Result<Unit> {
        Log.d(TAG, "🔄 Starting sync for ejecucion: ${ejecucion.uuidCliente}")
        return try {
            val isAlreadySynced = ejecucionDao.isAlreadySynced(ejecucion.uuidCliente)
            if (isAlreadySynced == true) {
                Log.d(TAG, "✅ Ejecucion ${ejecucion.uuidCliente} already synced, skipping")
                return Result.success(Unit)
            }

            val lockResult = ejecucionDao.acquireLock(ejecucion.uuidCliente)
            if (lockResult == 0) {
                Log.d(TAG, "🔒 Ejecucion ${ejecucion.uuidCliente} is being synced by another process")
                return Result.success(Unit)
            }

            val request = EjecucionRequestDto(
                fecha = ejecucion.fecha,
                mesEjecucion = ejecucion.mesEjecucion,
                semanaEjecucion = ejecucion.semanaEjecucion,
                estacionId = ejecucion.estacionId,
                disciplina = DISCIPLINA,
                tipoMantenimiento = ejecucion.tipoMantenimiento,
                tipoActividad = ejecucion.tipoActividad,
                actividadId = ejecucion.actividadId,
                programacionId = ejecucion.programacionId,
                motivoNoCatalogado = ejecucion.motivoNoCatalogado,
                resultado = ejecucion.resultado,
                observaciones = ejecucion.observaciones,
                descripcionLibre = ejecucion.descripcionLibre,
                uuidCliente = ejecucion.uuidCliente
            )
            val response = apiService.registrarEjecucionSubestacion(request)

            if (response.isSuccessful && response.body() != null) {
                val serverId = response.body()!!.id
                ejecucionDao.markAsSynced(ejecucion.uuidCliente, serverId)
                Log.d(TAG, "✅ Ejecucion ${ejecucion.uuidCliente} synced with serverId: $serverId")
                Result.success(Unit)
            } else {
                ejecucionDao.markSyncFailed(ejecucion.uuidCliente)
                Log.e(TAG, "❌ Ejecucion ${ejecucion.uuidCliente} sync failed: ${response.code()}")
                Result.failure(Exception("Error del servidor al sincronizar ejecución: ${response.code()}"))
            }
        } catch (e: Exception) {
            ejecucionDao.markSyncFailed(ejecucion.uuidCliente)
            Log.e(TAG, "❌ Exception syncing ejecucion ${ejecucion.uuidCliente}", e)
            Result.failure(e)
        }
    }

    // --- Evidencia ---

    /** Mismo flujo que FormRepositoryImpl.syncImage, pero con nombre de parte "file" (no "imagen"). */
    override suspend fun syncEvidencia(ejecucionServerId: Long, imageUri: String): Result<Unit> {
        return try {
            val uri = Uri.parse(imageUri)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("No se pudo abrir el URI: $imageUri"))

            val tempFile = File(context.cacheDir, "temp_ejecucion_${System.currentTimeMillis()}.jpg")
            inputStream.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }

            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            val response = apiService.subirEvidenciaSubestacion(ejecucionServerId, filePart)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error del servidor al subir evidencia: ${response.code()} - $errorBody")
                Result.failure(Exception("Error del servidor al subir evidencia: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception subiendo evidencia", e)
            Result.failure(e)
        }
    }

    // --- Catálogos cacheados ---

    override fun getEstacionesCache(): Flow<List<EstacionCatalogo>> {
        return estacionCacheDao.getAllFlow().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getActividadesCache(): Flow<List<ActividadCatalogo>> {
        return actividadCacheDao.getAllFlow().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun sincronizarCatalogos(): Result<Unit> {
        return try {
            val estacionesResponse = apiService.getEstacionesSubestacion()
            val actividadesResponse = apiService.getActividadesSubestacion(DISCIPLINA)

            if (estacionesResponse.isSuccessful && estacionesResponse.body() != null) {
                estacionCacheDao.clearAndInsert(estacionesResponse.body()!!.map { it.toEntity() })
            } else {
                return Result.failure(Exception("Error al obtener estaciones: ${estacionesResponse.code()}"))
            }

            if (actividadesResponse.isSuccessful && actividadesResponse.body() != null) {
                actividadCacheDao.clearAndInsert(actividadesResponse.body()!!.map { it.toEntity() })
            } else {
                return Result.failure(Exception("Error al obtener actividades: ${actividadesResponse.code()}"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception sincronizando catálogos de subestaciones", e)
            Result.failure(e)
        }
    }

    // --- Consultas online ---

    override suspend fun getProgramacion(estacionId: Long, anio: Int, mes: Int): Result<List<ProgramacionCita>> {
        return try {
            val response = apiService.getProgramacionSubestacion(estacionId, anio, mes, DISCIPLINA)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error al obtener programación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCumplimientoPorMes(anio: Int, mes: Int): Result<List<CitaProgramada>> {
        return try {
            val response = apiService.getCumplimientoSubestacion(anio = anio, mes = mes, disciplina = DISCIPLINA)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error al obtener cumplimiento: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCumplimientoPorEstacion(estacionId: Long, anio: Int): Result<List<CitaProgramada>> {
        return try {
            val response = apiService.getCumplimientoSubestacion(estacionId = estacionId, anio = anio, disciplina = DISCIPLINA)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Error al obtener cumplimiento: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * No existe un endpoint "todo el año, todas las estaciones" en el backend — se
     * hace un llamado por mes (1..mesActual) y se combinan los resultados. Si un mes
     * falla, se aborta y se retorna el error (mejor que mostrar datos incompletos sin
     * avisar).
     */
    override suspend fun getPendientesDelAnio(anio: Int, mesActual: Int): Result<List<CitaProgramada>> {
        val todas = mutableListOf<CitaProgramada>()
        for (mes in 1..mesActual) {
            val result = getCumplimientoPorMes(anio, mes)
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Error al obtener pendientes del mes $mes"))
            }
            todas.addAll(result.getOrDefault(emptyList()))
        }
        return Result.success(todas)
    }

    override suspend fun getDetalleEjecucion(id: Long): Result<EjecucionDetalle> {
        return try {
            val response = apiService.getEjecucionSubestacion(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al obtener el detalle de la ejecución: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEjecucionPorProgramacion(programacionId: Long): Result<EjecucionDetalle?> {
        return try {
            val response = apiService.getEjecucionPorProgramacion(programacionId)
            when {
                response.isSuccessful && response.body() != null -> Result.success(response.body()!!.toDomain())
                response.code() == 404 -> Result.success(null)
                else -> Result.failure(Exception("Error al buscar la ejecución de la cita: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editarEjecucion(id: Long, edicion: EjecucionEdicion): Result<EjecucionDetalle> {
        return try {
            val request = EjecucionEditRequestDto(
                fecha = edicion.fecha,
                mesEjecucion = edicion.mesEjecucion,
                semanaEjecucion = edicion.semanaEjecucion,
                tipoMantenimiento = edicion.tipoMantenimiento,
                tipoActividad = edicion.tipoActividad,
                actividadId = edicion.actividadId,
                motivoNoCatalogado = edicion.motivoNoCatalogado,
                resultado = edicion.resultado,
                observaciones = edicion.observaciones,
                descripcionLibre = edicion.descripcionLibre,
                motivoEdicion = edicion.motivoEdicion
            )
            val response = apiService.editarEjecucionSubestacion(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error al editar la ejecución: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Cumplimiento cacheado (offline-first: Cronograma/Pendientes/Home) ---

    override fun getCumplimientoLocalPorMesFlow(anio: Int, mes: Int): Flow<List<CitaProgramada>> {
        return cumplimientoCacheDao.getPorMesFlow(anio, mes).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getCumplimientoLocalDelAnioFlow(anio: Int, mesActual: Int): Flow<List<CitaProgramada>> {
        return cumplimientoCacheDao.getDelAnioFlow(anio, mesActual).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun sincronizarCumplimientoMes(anio: Int, mes: Int): Result<Unit> {
        return try {
            val response = apiService.getCumplimientoSubestacion(anio = anio, mes = mes, disciplina = DISCIPLINA)
            if (response.isSuccessful && response.body() != null) {
                cumplimientoCacheDao.reemplazarMes(anio, mes, response.body()!!.map { it.toEntity() })
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al sincronizar cumplimiento: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception sincronizando cumplimiento $anio-$mes", e)
            Result.failure(e)
        }
    }

    /** Mismo criterio que la vieja `getPendientesDelAnio`: mes por mes, se aborta en el primer fallo. */
    override suspend fun sincronizarCumplimientoDelAnio(anio: Int, mesActual: Int): Result<Unit> {
        for (mes in 1..mesActual) {
            val result = sincronizarCumplimientoMes(anio, mes)
            if (result.isFailure) return result
        }
        return Result.success(Unit)
    }

    // --- Detalle de ejecución cacheado bajo demanda (offline-first: pantalla Detalle) ---

    override fun getDetalleLocalFlow(id: Long): Flow<EjecucionDetalle?> {
        return ejecucionDetalleCacheDao.getByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun sincronizarDetalle(id: Long): Result<Unit> {
        return try {
            val response = apiService.getEjecucionSubestacion(id)
            if (response.isSuccessful && response.body() != null) {
                ejecucionDetalleCacheDao.upsert(response.body()!!.toCacheEntity())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al obtener el detalle de la ejecución: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception sincronizando detalle de ejecución $id", e)
            Result.failure(e)
        }
    }
}
