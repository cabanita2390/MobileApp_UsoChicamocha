package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada

/**
 * Caché de la respuesta de cumplimiento del backend (`ejecutado`/`cumple` ya calculados
 * server-side, nunca recalculados en el cliente — perderíamos visibilidad de ejecuciones
 * hechas por otros técnicos/dispositivos). Alimenta Cronograma, Pendientes y los KPIs del
 * Home sin depender de la red — mismo espíritu que EstacionCacheEntity/ActividadCacheEntity.
 *
 * Una fila por `programacionId` (igual que el backend), con año/mes/estación/actividad para
 * poder filtrar en el DAO como antes se filtraba en memoria.
 */
@Entity(tableName = "mant_cumplimiento_cache")
data class CumplimientoCacheEntity(
    @PrimaryKey val programacionId: Long,
    val anio: Int,
    val mes: Int,
    val estacionId: Long,
    val estacionNombre: String,
    val estacionTipo: String,
    val actividadId: Long,
    val actividadNombre: String,
    val disciplina: String,
    val ejecutado: Int,
    val cumple: Boolean
)

fun CumplimientoCacheEntity.toDomain(): CitaProgramada {
    return CitaProgramada(
        programacionId = programacionId,
        anio = anio,
        mes = mes,
        estacionId = estacionId,
        estacionNombre = estacionNombre,
        estacionTipo = estacionTipo,
        actividadId = actividadId,
        actividadNombre = actividadNombre,
        ejecutado = ejecutado,
        cumple = cumple
    )
}
