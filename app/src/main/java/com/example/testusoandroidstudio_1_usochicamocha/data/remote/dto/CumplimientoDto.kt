package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.CumplimientoCacheEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.google.gson.annotations.SerializedName

data class CumplimientoDto(
    @SerializedName("programacionId") val programacionId: Long,
    @SerializedName("anio") val anio: Int,
    @SerializedName("mes") val mes: Int,
    @SerializedName("estacionId") val estacionId: Long,
    @SerializedName("estacionNombre") val estacionNombre: String,
    @SerializedName("estacionTipo") val estacionTipo: String,
    @SerializedName("actividadId") val actividadId: Long,
    @SerializedName("actividadNombre") val actividadNombre: String,
    @SerializedName("disciplina") val disciplina: String,
    @SerializedName("ejecutado") val ejecutado: Int,
    @SerializedName("cumple") val cumple: Boolean
)

fun CumplimientoDto.toDomain(): CitaProgramada {
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

/** Para el caché offline-first (ver CumplimientoCacheEntity) — se guarda tal cual la calculó el backend. */
fun CumplimientoDto.toEntity(): CumplimientoCacheEntity {
    return CumplimientoCacheEntity(
        programacionId = programacionId,
        anio = anio,
        mes = mes,
        estacionId = estacionId,
        estacionNombre = estacionNombre,
        estacionTipo = estacionTipo,
        actividadId = actividadId,
        actividadNombre = actividadNombre,
        disciplina = disciplina,
        ejecutado = ejecutado,
        cumple = cumple
    )
}
