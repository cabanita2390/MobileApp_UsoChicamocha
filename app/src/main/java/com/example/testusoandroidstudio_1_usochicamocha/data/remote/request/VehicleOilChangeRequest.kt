package com.example.testusoandroidstudio_1_usochicamocha.data.remote.request

/**
 * POST /api/v1/vehicle/oil-change — alineado con VehicleOilChangeRequest.java del backend.
 */
data class VehicleOilChangeRequest(
    val placa: String,
    val dateStamp: String,
    val oilType: String,
    val brandId: Long,
    val quantity: Double?,
    val kmAtChange: Int,
    val intervalKm: Int,
    val airFilterChanged: Boolean?,
)
