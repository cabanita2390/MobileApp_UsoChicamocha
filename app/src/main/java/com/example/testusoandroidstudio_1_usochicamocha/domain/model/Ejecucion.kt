package com.example.testusoandroidstudio_1_usochicamocha.domain.model

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity

/**
 * Ejecución de mantenimiento civil de subestaciones en la capa de Dominio — el objeto
 * "limpio" que usan ViewModel/UI, análogo a Form pero para el módulo Subestaciones.
 */
data class Ejecucion(
    val localId: Int = 0,
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
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false,
    val syncFallido: Boolean = false,
    /** Solo poblado por `SubestacionRepository.getColaFlow()` (para el detalle de la Cola) — 0 en el resto de usos. */
    val fotosCount: Int = 0
)

fun Ejecucion.toEntity(): EjecucionEntity {
    return EjecucionEntity(
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
