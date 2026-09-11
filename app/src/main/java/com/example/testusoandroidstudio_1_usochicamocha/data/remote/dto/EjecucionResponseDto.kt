package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionNoProgramadaCacheEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EdicionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EvidenciaDetalle
import com.google.gson.annotations.SerializedName

data class EvidenciaSubestacionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("rutaArchivo") val rutaArchivo: String,
    @SerializedName("nombreOriginal") val nombreOriginal: String,
    @SerializedName("subidoEn") val subidoEn: String?
)

data class EdicionSubestacionDto(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("motivo") val motivo: String,
    @SerializedName("editadoEn") val editadoEn: String?
)

/** Espejo exacto de EjecucionResponse del backend. */
data class EjecucionResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("mesEjecucion") val mesEjecucion: Int,
    @SerializedName("semanaEjecucion") val semanaEjecucion: Int,
    @SerializedName("estacionId") val estacionId: Long,
    @SerializedName("estacionNombre") val estacionNombre: String,
    @SerializedName("disciplina") val disciplina: String,
    @SerializedName("tipoMantenimiento") val tipoMantenimiento: String,
    @SerializedName("tipoActividad") val tipoActividad: String,
    @SerializedName("actividadId") val actividadId: Long?,
    @SerializedName("actividadNombre") val actividadNombre: String?,
    @SerializedName("esProgramada") val esProgramada: Boolean,
    @SerializedName("motivoNoCatalogado") val motivoNoCatalogado: String?,
    @SerializedName("resultado") val resultado: String,
    @SerializedName("observaciones") val observaciones: String,
    @SerializedName("descripcionLibre") val descripcionLibre: String?,
    @SerializedName("responsable") val responsable: String,
    @SerializedName("uuidCliente") val uuidCliente: String,
    @SerializedName("evidencias") val evidencias: List<EvidenciaSubestacionDto>,
    @SerializedName("evidenciaPendiente") val evidenciaPendiente: Boolean,
    @SerializedName("ediciones") val ediciones: List<EdicionSubestacionDto>
)

/** Usado tras un POST/GET por uuid: refleja el resultado ya confirmado por el servidor en la fila local. */
fun EjecucionResponseDto.toEntity(localId: Int): EjecucionEntity {
    return EjecucionEntity(
        localId = localId,
        serverId = id,
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
        programacionId = null,
        esProgramada = esProgramada,
        motivoNoCatalogado = motivoNoCatalogado,
        resultado = resultado,
        observaciones = observaciones,
        descripcionLibre = descripcionLibre,
        isSynced = true,
        isSyncing = false
    )
}

fun EjecucionResponseDto.toDomain(): EjecucionDetalle {
    return EjecucionDetalle(
        id = id,
        fecha = fecha,
        mesEjecucion = mesEjecucion,
        semanaEjecucion = semanaEjecucion,
        estacionId = estacionId,
        estacionNombre = estacionNombre,
        disciplina = disciplina,
        tipoMantenimiento = tipoMantenimiento,
        tipoActividad = tipoActividad,
        actividadId = actividadId,
        actividadNombre = actividadNombre,
        esProgramada = esProgramada,
        motivoNoCatalogado = motivoNoCatalogado,
        resultado = resultado,
        observaciones = observaciones,
        descripcionLibre = descripcionLibre,
        responsable = responsable,
        uuidCliente = uuidCliente,
        evidencias = evidencias.map { EvidenciaDetalle(it.id, it.rutaArchivo, it.nombreOriginal, it.subidoEn) },
        evidenciaPendiente = evidenciaPendiente,
        ediciones = ediciones.map { EdicionDetalle(it.usuario, it.motivo, it.editadoEn) }
    )
}

/**
 * Para el caché de "Realizadas" (ver EjecucionNoProgramadaCacheEntity): `anio` se deriva de
 * `fecha` (formato ISO "yyyy-MM-dd" del backend) porque EjecucionResponse no trae un campo
 * de año explícito; `mes` usa `mesEjecucion` (el campo pensado para "en qué mes cuenta" un
 * registro tardío, igual que en CumplimientoDto). El nombre a mostrar prioriza la actividad
 * del catálogo y cae a la descripción libre cuando no vino de una cita/catálogo.
 */
fun EjecucionResponseDto.toNoProgramadaCacheEntity(): EjecucionNoProgramadaCacheEntity {
    return EjecucionNoProgramadaCacheEntity(
        ejecucionId = id,
        anio = fecha.substring(0, 4).toInt(),
        mes = mesEjecucion,
        estacionId = estacionId,
        estacionNombre = estacionNombre,
        actividadNombre = actividadNombre ?: descripcionLibre ?: "Actividad no catalogada"
    )
}
