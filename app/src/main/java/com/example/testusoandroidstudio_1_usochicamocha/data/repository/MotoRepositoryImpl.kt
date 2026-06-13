package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.DocumentoMotoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MotoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.UbicacionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.UbicacionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MotoRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val motoDao: MotoDao,
    private val ubicacionDao: UbicacionDao,
    private val documentoMotoDao: DocumentoMotoDao
) : MotoRepository {

    override suspend fun syncMotos(): Result<Unit> {
        return try {
            android.util.Log.d("MotoRepo", "🔄 Iniciando sincronización de motos desde v1/moto/placas")
            val response = apiService.getMotocicletas()
            if (response.isSuccessful && response.body() != null) {
                val motos = response.body()!!
                android.util.Log.d("MotoRepo", "📡 Servidor devolvió ${motos.size} motos")

                if (motos.isEmpty()) {
                    android.util.Log.w("MotoRepo", "⚠️ Lista de motos vacía del servidor")
                    // No sobreescribir la BD local si el servidor devuelve vacío
                    // (puede ser error del servidor o tipo sin configurar)
                    return if (motoDao.count() == 0)
                        Result.failure(Exception("No se encontraron motocicletas en el servidor."))
                    else
                        Result.success(Unit)
                }

                val entities = motos.map { moto ->
                    android.util.Log.d("MotoRepo", "   Moto recibida: ID=${moto.id}, Placa=${moto.placa}, Marca=${moto.marca}")
                    if (moto.id == null || moto.id == 0) {
                        android.util.Log.e("MotoRepo", "   ❌ ERROR: Moto ${moto.placa} tiene ID=${moto.id} (nulo o cero)")
                    }
                    MotoEntity(id = moto.id ?: 0, placa = moto.placa, marca = moto.marca, idUbicacionBase = moto.idUbicacionBase ?: 0, ubicacionBase = moto.ubicacionBase ?: "")
                }

                motoDao.clearAndInsert(entities)
                android.util.Log.d("MotoRepo", "✅ ${entities.size} motos guardadas en BD local")

                entities.forEach { entity ->
                    if (entity.id == 0) {
                        android.util.Log.e("MotoRepo", "   ⚠️ Moto guardada con ID=0: ${entity.placa}")
                    }
                }

                Result.success(Unit)
            } else {
                val code = response.code()
                android.util.Log.e("MotoRepo", "❌ Error sincronizando motos: HTTP $code")
                Result.failure(Exception("Error al obtener motos del servidor: $code"))
            }
        } catch (e: Exception) {
            android.util.Log.e("MotoRepo", "❌ Excepción sincronizando motos: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getLocalMotos(): Flow<List<Moto>> {
        return motoDao.getAllMotos().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun syncUbicaciones(): Result<Unit> {
        return try {
            val response = apiService.getUbicaciones()
            if (response.isSuccessful && response.body() != null) {
                val ubicaciones = response.body()!!
                if (ubicaciones.isEmpty() && ubicacionDao.count() == 0) {
                    return Result.failure(Exception("No se encontraron ubicaciones en el servidor."))
                }
                val entities = ubicaciones.map { UbicacionEntity(id = it.id, nombreUbicacion = it.nombreUbicacion) }
                ubicacionDao.clearAndInsert(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al obtener ubicaciones del servidor."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncDocumentos(): Result<Unit> {
        return try {
            val motosLocal = motoDao.getAllMotos().first()
            if (motosLocal.isEmpty()) return Result.success(Unit)

            motosLocal.forEach { moto ->
                try {
                    val resp = apiService.getDocumentosByPlaca(moto.placa)
                    if (resp.isSuccessful && resp.body() != null) {
                        val apiDocs = resp.body()!!
                        val entities = apiDocs.mapNotNull { apiDoc ->
                            val fechaApi = apiDoc.fechaVencimiento ?: apiDoc.mesyear ?: ""
                            if (fechaApi.isNotBlank()) {
                                DocumentoMotoEntity(
                                    tipoDocumento = apiDoc.tipoDocumento,
                                    placa = moto.placa,
                                    vigencia = fechaApi,
                                    kilometrajeActual = apiDoc.vehiculoKilometrajeActual ?: 0,
                                    imagenUrl = apiDoc.imagenUrl
                                )
                            } else null
                        }
                        if (entities.isNotEmpty()) {
                            documentoMotoDao.refreshForPlaca(moto.placa, entities)
                        }
                    }
                } catch (e: Exception) {
                    // Continuar con la siguiente moto si falla una
                    android.util.Log.e("MotoRepository", "Error sincronizando documentos para ${moto.placa}: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLocalUbicaciones(): Flow<List<Ubicacion>> {
        return ubicacionDao.getAllUbicaciones().map { entities -> entities.map { it.toDomain() } }
    }
}
