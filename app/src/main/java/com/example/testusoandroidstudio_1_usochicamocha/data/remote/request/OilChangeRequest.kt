package com.example.testusoandroidstudio_1_usochicamocha.data.remote.request

import com.google.gson.annotations.SerializedName

data class OilChangeRequest(
    @SerializedName("machineId")
    val machineId: Int,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("brandId")
    val brandId: Long,
    @SerializedName("quantity")
    val quantity: Double,
    @SerializedName("currentHourMeter")
    val currentHourMeter: Double,
    @SerializedName("averageHoursChange")
    val averageHoursChange: Int
)