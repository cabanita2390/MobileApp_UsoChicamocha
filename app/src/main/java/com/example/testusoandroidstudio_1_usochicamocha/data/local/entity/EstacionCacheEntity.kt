package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EstacionCatalogo

/** Catálogo de estaciones de subestaciones, cacheado para que el wizard de captura funcione sin señal. */
@Entity(tableName = "mant_estacion_cache")
data class EstacionCacheEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val tipo: String,
    val frecuenciaBase: String
)

fun EstacionCacheEntity.toDomain(): EstacionCatalogo {
    return EstacionCatalogo(id = id, nombre = nombre, tipo = tipo, frecuenciaBase = frecuenciaBase)
}
