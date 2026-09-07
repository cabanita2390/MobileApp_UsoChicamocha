package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.squareup.moshi.Json

data class MaintenanceDto(
    @field:Json(name = "machineId")
    val machineId: Int,
    @field:Json(name = "dateTime")
    val dateTime: String,
    @field:Json(name = "brand")
    val brand: String,
    @field:Json(name = "quantity")
    val quantity: Double,
    @field:Json(name = "currentHourMeter")
    val currentHourMeter: Int,
    @field:Json(name = "averageHoursChange")
    val averageHoursChange: Int
)
