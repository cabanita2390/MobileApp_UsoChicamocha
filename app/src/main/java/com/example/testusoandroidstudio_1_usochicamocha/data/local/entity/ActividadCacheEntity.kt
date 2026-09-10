package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ActividadCatalogo

/** Catálogo de actividades civiles habilitadas para captura móvil, cacheado (misma razón que EstacionCacheEntity). */
@Entity(tableName = "mant_actividad_cache")
data class ActividadCacheEntity(
    @PrimaryKey val id: Long,
    val nombre: String
)

fun ActividadCacheEntity.toDomain(): ActividadCatalogo {
    return ActividadCatalogo(id = id, nombre = nombre)
}
