package com.example.testusoandroidstudio_1_usochicamocha.data.remote.request

import com.google.gson.annotations.SerializedName

data class CreateVehicleOilChangeRequest(
    @SerializedName("placa")
    val placa: String,

    @SerializedName("oilType")
    val oilType: String,

    @SerializedName("brandId")
    val brandId: Long,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("kmAtChange")
    val kmAtChange: Int,

    @SerializedName("airFilterChanged")
    val airFilterChanged: Boolean = false,

    @SerializedName("requirementId")
    val requirementId: Long
)

data class CreateMachineOilChangeRequest(
    @SerializedName("machineId")
    val machineId: Long,

    @SerializedName("oilType")
    val oilType: String,

    @SerializedName("motorOil")
    val motorOil: Boolean = true,

    @SerializedName("hydraulicOil")
    val hydraulicOil: Boolean = false,

    @SerializedName("brandId")
    val brandId: Long,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("hourStamp")
    val hourStamp: Int,

    @SerializedName("requirementId")
    val requirementId: Long
)

data class CreateOilAnalysisSosRequest(
    @SerializedName("machineId")
    val machineId: Long,

    @SerializedName("machineName")
    val machineName: String,

    @SerializedName("oilType")
    val oilType: String,

    @SerializedName("hoursAtAnalysis")
    val hoursAtAnalysis: Int,

    @SerializedName("nextChangeHours")
    val nextChangeHours: Int,

    @SerializedName("sosReportUrl")
    val sosReportUrl: String,

    @SerializedName("approvedByMechanic")
    val approvedByMechanic: String,

    @SerializedName("observations")
    val observations: String? = null
)
