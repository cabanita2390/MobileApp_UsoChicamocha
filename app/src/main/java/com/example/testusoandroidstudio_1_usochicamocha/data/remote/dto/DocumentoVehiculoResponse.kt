package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

/**
 * Respuesta del GET /api/v1/vehicle-inspection/documentos/{placa}
 * El backend calcula el estado automáticamente (Vigente / Próximo a Vencer / Vencido).
 */
data class DocumentoVehiculoResponse(
    val idVehiculo: Int,
    val fechaVencSoat: String?,
    val estadoSoat: String?,
    val urlImagenSoat: String?,
    val fechaVencTecno: String?,
    val estadoTecno: String?,
    val urlImagenTecno: String?,
    val urlImagenTarjetaPropiedad: String?,
    val fechaVencExtintor: String?,
    val estadoExtintor: String?,
    val urlImagenExtintor: String?
)
