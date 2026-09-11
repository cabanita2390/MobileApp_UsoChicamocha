package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Espejo mínimo de org.springframework.data.domain.Page (backend): solo se mapea
 * "content" — el resto de campos del JSON (totalElements, totalPages, number, size,
 * etc.) no se necesitan hoy y Gson los ignora sin declararlos.
 */
data class PageDto<T>(
    @SerializedName("content") val content: List<T> = emptyList()
)
