package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EjecucionResponseDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.google.gson.Gson

/**
 * Caché "bajo demanda" del detalle de una ejecución ya consultado (pantalla Detalle):
 * no se prefetchea todo, solo se guarda la respuesta del backend la primera vez que se
 * pide con éxito, para poder volver a verla sin señal. Se guarda el JSON crudo de
 * [EjecucionResponseDto] (espejo exacto del backend, con listas anidadas de evidencias/
 * ediciones) en vez de modelar tablas relacionales para un caché de solo lectura.
 */
@Entity(tableName = "mant_ejecucion_detalle_cache")
data class EjecucionDetalleCacheEntity(
    @PrimaryKey val id: Long,
    val json: String,
    val cachedAt: Long
)

private val gson = Gson()

fun EjecucionResponseDto.toCacheEntity(): EjecucionDetalleCacheEntity {
    return EjecucionDetalleCacheEntity(
        id = id,
        json = gson.toJson(this),
        cachedAt = System.currentTimeMillis()
    )
}

fun EjecucionDetalleCacheEntity.toDomain(): EjecucionDetalle {
    return gson.fromJson(json, EjecucionResponseDto::class.java).toDomain()
}
