package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

data class NewAccessTokenResponse(
    val accessToken: String,
    /** Credencial para ver archivos de /uploads (ver ArchivosInterceptor). null con backends viejos. */
    val archivosToken: String? = null
)