package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ActividadCacheEntity
import com.google.gson.annotations.SerializedName

data class ActividadDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String
)

fun ActividadDto.toEntity(): ActividadCacheEntity {
    return ActividadCacheEntity(id = id, nombre = nombre)
}
