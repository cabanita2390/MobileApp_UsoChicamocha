package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

data class MotoPlacaDto(
    val id: Int,
    val placa: String,
    val idUbicacionBase: Int? = null,
    val ubicacionBase: String? = null
)

data class UbicacionDto(val id: Int, val nombreUbicacion: String)

data class DocumentoExistenteDto(
    val id: Int? = null,
    val tipoDocumento: String = "",
    val fechaVencimiento: String? = null,
    val mesyear: String? = null,
    val imagenUrl: String? = null,
    val vehiculoKilometrajeActual: Int? = null,
    val estadoCheck: String? = null        // Vigente / Vencido / Próximo a vencer
)

data class InspeccionMotoRequest(
    // Cabecera
    val idVehiculo: Int,
    val kilometrajeReportado: Int,
    val estadoVehiculo: String,
    val observacionesFinales: String,

    // Documentos
    val checkSoat: String,
    val checkTecno: String,
    val checkLicencia: String,
    val checkExtintor: String,

    // Inspección Mecánica Moto (Bueno / Regular / Malo)
    val checkNivelAceite: String,
    val checkEstadoLlantas: String,
    val checkEstadoLuces: String,

    // Ubicacion
    val idUbicacion: Int
)
