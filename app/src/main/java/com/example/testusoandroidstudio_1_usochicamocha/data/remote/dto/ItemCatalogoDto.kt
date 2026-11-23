package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ItemIntervenidoCacheEntity
import com.google.gson.annotations.SerializedName

/** Espejo de ItemCatalogoResponse (GET substation/conjuntos y substation/elementos). */
data class ItemCatalogoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("disciplina") val disciplina: String,
    /** Solo conjuntos: código del ícono elegido en la web; null = genérico. */
    @SerializedName("icono") val icono: String? = null,
    /** Solo conjuntos: lo que tiene adentro, en orden (backend V54); null en elementos y backends anteriores. */
    @SerializedName("elementos") val elementos: List<ElementoDeConjuntoDto>? = null
)

/** Espejo de ElementoDeConjuntoResponse: un elemento dentro de un conjunto. */
data class ElementoDeConjuntoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("pideDetalle") val pideDetalle: Boolean? = null
)

/** Contenido de un conjunto para la caché, en el orden que llegó. */
fun ItemCatalogoDto.elementosCache() = elementos.orEmpty().mapIndexed { i, e ->
    com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ConjuntoElementoCacheEntity(
        conjuntoId = id, elementoId = e.id, nombre = e.nombre, pideDetalle = e.pideDetalle == true, orden = i
    )
}

fun ItemCatalogoDto.toEntity(tipo: String) =
    ItemIntervenidoCacheEntity(tipo = tipo, id = id, nombre = nombre, disciplina = disciplina, icono = icono)
