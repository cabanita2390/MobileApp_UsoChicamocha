package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OilChangeRequirementDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("oilType")
    val oilType: String,

    @SerializedName("assetType")
    val assetType: String,

    @SerializedName("kmRange")
    val kmRange: Int?,

    @SerializedName("hourRange")
    val hourRange: Int?,

    @SerializedName("description")
    val description: String,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("rangeValue")
    val rangeValue: Int,

    @SerializedName("durabilityLevel")
    val durabilityLevel: String
)
