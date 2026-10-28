package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ItemIntervenidoCacheEntity
import com.google.gson.annotations.SerializedName

/** Espejo de ItemCatalogoResponse (GET substation/conjuntos y substation/elementos). */
data class ItemCatalogoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("disciplina") val disciplina: String,
    /** Solo conjuntos: código del ícono elegido en la web; null = genérico. */
    @SerializedName("icono") val icono: String? = null
)

fun ItemCatalogoDto.toEntity(tipo: String) =
    ItemIntervenidoCacheEntity(tipo = tipo, id = id, nombre = nombre, disciplina = disciplina, icono = icono)
