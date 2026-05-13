package com.example.testusoandroidstudio_1_usochicamocha.domain.model

data class Moto(
    val id: Int,
    val placa: String,
    val idUbicacionBase: Int = 0,
    val ubicacionBase: String = ""
)
