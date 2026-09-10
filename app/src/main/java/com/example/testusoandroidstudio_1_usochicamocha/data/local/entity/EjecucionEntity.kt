package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion

@Entity(tableName = "pending_mant_ejecucion")
data class EjecucionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val serverId: Long? = null,
    val uuidCliente: String,
    val fecha: String,
    val mesEjecucion: Int,
    val semanaEjecucion: Int,
    val estacionId: Long,
    val estacionNombre: String,
    val tipoMantenimiento: String,
    val tipoActividad: String,
    val actividadId: Long? = null,
    val actividadNombre: String? = null,
    val programacionId: Long? = null,
    val esProgramada: Boolean,
    val motivoNoCatalogado: String? = null,
    val resultado: String,
    val observaciones: String,
    val descripcionLibre: String? = null,
    var isSynced: Boolean = false,
    var isSyncing: Boolean = false,
    var syncFallido: Boolean = false
)

fun EjecucionEntity.toDomain(): Ejecucion {
    return Ejecucion(
        localId = localId,
        serverId = serverId,
        uuidCliente = uuidCliente,
        fecha = fecha,
        mesEjecucion = mesEjecucion,
        semanaEjecucion = semanaEjecucion,
        estacionId = estacionId,
        estacionNombre = estacionNombre,
        tipoMantenimiento = tipoMantenimiento,
        tipoActividad = tipoActividad,
        actividadId = actividadId,
        actividadNombre = actividadNombre,
        programacionId = programacionId,
        esProgramada = esProgramada,
        motivoNoCatalogado = motivoNoCatalogado,
        resultado = resultado,
        observaciones = observaciones,
        descripcionLibre = descripcionLibre,
        isSynced = isSynced,
        isSyncing = isSyncing,
        syncFallido = syncFallido
    )
}
