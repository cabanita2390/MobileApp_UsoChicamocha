package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EstacionCacheEntity
import com.google.gson.annotations.SerializedName

data class EstacionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("frecuenciaBase") val frecuenciaBase: String
)

fun EstacionDto.toEntity(): EstacionCacheEntity {
    return EstacionCacheEntity(id = id, nombre = nombre, tipo = tipo, frecuenciaBase = frecuenciaBase)
}
