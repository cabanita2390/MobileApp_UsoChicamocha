package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OilAnalysisSosDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("machineId")
    val machineId: Long,

    @SerializedName("machineName")
    val machineName: String,

    @SerializedName("analysisDate")
    val analysisDate: String,

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
    val observations: String?,

    @SerializedName("isApproved")
    val isApproved: Boolean,

    @SerializedName("extendedHours")
    val extendedHours: Int,

    @SerializedName("authorizesExtension")
    val authorizesExtension: Boolean,

    @SerializedName("createdAt")
    val createdAt: String
)
