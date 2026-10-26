package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.PresionLlantasJson
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.LlantaConfigCacheDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.LlantaConfigCacheEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.ConfigLlantasActivoDto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ConfigLlantas
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PosicionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.LlantasRepository
import javax.inject.Inject

class LlantasRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: LlantaConfigCacheDao
) : LlantasRepository {

    override suspend fun sincronizar(): Result<Unit> {
        return try {
            val response = apiService.getAsignacionesLlantas()
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                return Result.failure(Exception("Error al obtener los perfiles de llantas (${response.code()})"))
            }
            dao.clearAndInsert(body.mapNotNull { it.toCacheEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron sincronizar los perfiles de llantas", e)
            Result.failure(e)
        }
    }

    override suspend fun configVehiculo(idVehiculo: Int): ConfigLlantas? =
        dao.get(LlantaConfigCacheEntity.TIPO_VEHICULO, idVehiculo.toLong())?.toDomain()

    override suspend fun configMaquina(idMaquina: Long): ConfigLlantas? =
        dao.get(LlantaConfigCacheEntity.TIPO_MAQUINA, idMaquina)?.toDomain()

    private fun ConfigLlantasActivoDto.toCacheEntity(): LlantaConfigCacheEntity? {
        val (tipo, id) = when {
            vehicleId != null -> LlantaConfigCacheEntity.TIPO_VEHICULO to vehicleId.toLong()
            machineId != null -> LlantaConfigCacheEntity.TIPO_MAQUINA to machineId
            else -> return null
        }
        val posiciones = posiciones.orEmpty().mapIndexedNotNull { i, p ->
            val codigo = p.codigo?.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
            val min = p.psiMin ?: return@mapIndexedNotNull null
            val max = p.psiMax ?: return@mapIndexedNotNull null
            PosicionLlanta(codigo, p.etiqueta?.takeIf { it.isNotBlank() } ?: codigo, p.orden ?: (i + 1), min, p.psiObjetivo, max)
        }.sortedBy { it.orden }
        if (posiciones.isEmpty()) return null
        return LlantaConfigCacheEntity(tipo, id, perfilNombre.orEmpty(), PresionLlantasJson.posicionesToJson(posiciones))
    }

    private fun LlantaConfigCacheEntity.toDomain(): ConfigLlantas? {
        val posiciones = PresionLlantasJson.posicionesFromJson(posicionesJson)
        return if (posiciones.isEmpty()) null else ConfigLlantas(perfilNombre, posiciones)
    }

    private companion object {
        const val TAG = "LlantasRepository"
    }
}
