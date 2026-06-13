package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MachineOilChangeResponseDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("machineId")
    val machineId: Long,

    @SerializedName("oilType")
    val oilType: String,

    @SerializedName("brandName")
    val brandName: String?,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("hourStamp")
    val hourStamp: Int,

    @SerializedName("nextChangeHours")
    val nextChangeHours: Int,

    @SerializedName("percentageUsed")
    val percentageUsed: Int,

    @SerializedName("motorOil")
    val motorOil: Boolean?,

    @SerializedName("hydraulicOil")
    val hydraulicOil: Boolean?,

    @SerializedName("dateStamp")
    val dateStamp: String,

    @SerializedName("oilDurability")
    val oilDurability: String
)
