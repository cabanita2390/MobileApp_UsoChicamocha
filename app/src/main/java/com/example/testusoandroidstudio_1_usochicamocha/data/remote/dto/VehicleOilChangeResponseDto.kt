package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VehicleOilChangeResponseDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("placa")
    val placa: String,

    @SerializedName("oilType")
    val oilType: String,

    @SerializedName("brandName")
    val brandName: String?,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("kmAtChange")
    val kmAtChange: Int,

    @SerializedName("nextChangeKm")
    val nextChangeKm: Int,

    @SerializedName("percentageUsed")
    val percentageUsed: Int,

    @SerializedName("airFilterChanged")
    val airFilterChanged: Boolean?,

    @SerializedName("dateStamp")
    val dateStamp: String,

    @SerializedName("oilDurability")
    val oilDurability: String
)
