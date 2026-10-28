package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ActividadCatalogo

/** Catálogo de actividades habilitadas para captura móvil (todas las disciplinas), cacheado (misma razón que EstacionCacheEntity). */
@Entity(tableName = "mant_actividad_cache")
data class ActividadCacheEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    @ColumnInfo(defaultValue = "CIVIL") val disciplina: String = "CIVIL"
)

fun ActividadCacheEntity.toDomain(): ActividadCatalogo {
    return ActividadCatalogo(id = id, nombre = nombre, disciplina = disciplina)
}
