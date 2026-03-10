package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("id")
    val userId: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("jwt")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("fullName")
    val fullName: String?,

    @SerializedName("role")
    val role: String?,

    @SerializedName("status")
    val status: Boolean
)

