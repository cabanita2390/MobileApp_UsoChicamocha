package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Espejo mínimo de org.springframework.data.domain.Page (backend): "content" y "last" (para
 * recorrer todas las páginas); el resto de campos del JSON Gson los ignora sin declararlos.
 */
data class PageDto<T>(
    @SerializedName("content") val content: List<T> = emptyList(),
    /** null en respuestas que no lo traen: se trata como última página. */
    @SerializedName("last") val last: Boolean? = null
)
