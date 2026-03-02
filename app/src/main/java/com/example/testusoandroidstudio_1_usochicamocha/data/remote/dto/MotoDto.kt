package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

data class MotoPlacaDto(val id: Int, val placa: String)

data class UbicacionDto(val id: Int, val nombreUbicacion: String)

data class DocumentoExistenteDto(
    val id: Int,
    val tipoDocumento: String,
    val fechaVencimiento: String?,
    val mesyear: String?,
    val imagenUrl: String?
)

data class InspeccionMotoRequest(
    val idVehiculo: Int,
    val idUbicacion: Int,
    val kilometrajeReportado: Int,
    val estadoGeneral: String,
    val observacionesFinales: String,
    val vigenciaSoat: String?,
    val estadoSoat: String?,
    val vigenciaRevision: String?,
    val estadoRevision: String?,
    val vigenciaLicencia: String?,
    val estadoLicencia: String?,
    val imagenSoat: String?,
    val imagenRevision: String?,
    val imagenLicencia: String?
)
