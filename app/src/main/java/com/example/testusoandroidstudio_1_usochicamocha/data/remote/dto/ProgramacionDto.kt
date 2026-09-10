package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ProgramacionCita
import com.google.gson.annotations.SerializedName

data class ProgramacionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("anio") val anio: Int,
    @SerializedName("mes") val mes: Int,
    @SerializedName("estacionId") val estacionId: Long,
    @SerializedName("actividadId") val actividadId: Long,
    @SerializedName("actividadNombre") val actividadNombre: String
)

fun ProgramacionDto.toDomain(): ProgramacionCita {
    return ProgramacionCita(
        id = id,
        anio = anio,
        mes = mes,
        estacionId = estacionId,
        actividadId = actividadId,
        actividadNombre = actividadNombre
    )
}
