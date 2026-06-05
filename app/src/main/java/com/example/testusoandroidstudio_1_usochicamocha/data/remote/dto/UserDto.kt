package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")          val id: Int,
    @SerializedName("username")    val username: String?,
    @SerializedName("fullName")    val fullName: String?,
    @SerializedName("email")       val email: String?,
    @SerializedName("role")        val role: String?,
    @SerializedName("licenseCategory")    val licenseCategory: String?,
    @SerializedName("licenseExpiry")      val licenseExpiry: String?,
    @SerializedName("licenseDocumentUrl") val licenseDocumentUrl: String?
)
