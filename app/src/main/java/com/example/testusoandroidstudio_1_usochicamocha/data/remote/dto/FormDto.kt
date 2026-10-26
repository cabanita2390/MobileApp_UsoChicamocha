package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.squareup.moshi.Json


data class FormDto(
    @field:Json(name = "UUID")
    val UUID: String,

    @field:Json(name = "dateStamp")
    val dateStamp: String,
    @field:Json(name = "hourmeter")
    val hourMeter: String,
    @field:Json(name = "leakStatus")
    val leakStatus: String,

    @field:Json(name = "brakeStatus")
    val brakeStatus: String,

    @field:Json(name = "beltsPulleysStatus")
    val beltsPulleysStatus: String,

    @field:Json(name = "tireLanesStatus")
    val tireLanesStatus: String,

    @field:Json(name = "carIgnitionStatus")
    val carIgnitionStatus: String,

    @field:Json(name = "electricalStatus")
    val electricalStatus: String,

    @field:Json(name = "mechanicalStatus")
    val mechanicalStatus: String,

    @field:Json(name = "temperatureStatus")
    val temperatureStatus: String,

    @field:Json(name = "oilStatus")
    val oilStatus: String,

    @field:Json(name = "hydraulicStatus")
    val hydraulicStatus: String,

    @field:Json(name = "coolantStatus")
    val coolantStatus: String,

    @field:Json(name = "structuralStatus")
    val structuralStatus: String,

    @field:Json(name = "expirationDateFireExtinguisher")
    val expirationDateFireExtinguisher: String,

    @field:Json(name = "observations")
    val observations: String,

    @field:Json(name = "userId")
    val userId: Long,

    @field:Json(name = "machineId")
    val machineId: Long,

    @field:Json(name = "greasingAction")
    val greasingAction: String,

    @field:Json(name = "greasingObservations")
    val greasingObservations: String,

    @field:Json(name = "isUnexpected")
    val isUnexpected: Boolean,

    @field:Json(name = "presionesLlantas")
    val presionesLlantas: List<LecturaPresionLlantaDto>? = null
)
