package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ActividadCacheEntity
import com.google.gson.annotations.SerializedName

data class ActividadDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("disciplina") val disciplina: String? = null
)

/** null (con aviso en el log) si llega sin disciplina: es un error del servidor, no una actividad de Civil. */
fun ActividadDto.toEntity(): ActividadCacheEntity? {
    if (disciplina.isNullOrBlank()) {
        android.util.Log.w("ActividadDto", "Actividad $id sin disciplina: se descarta")
        return null
    }
    return ActividadCacheEntity(id = id, nombre = nombre, disciplina = disciplina)
}
