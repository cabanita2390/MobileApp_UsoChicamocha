package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FuelLogRequest(
    @SerializedName("syncId") val syncId: String?,
    @SerializedName("assetType") val assetType: String,
    @SerializedName("assetId") val assetId: Long,
    @SerializedName("assetPlate") val assetPlate: String?,
    @SerializedName("fuelDateTime") val fuelDateTime: String,
    @SerializedName("odometerKm") val odometerKm: Double?,
    @SerializedName("hourMeter") val hourMeter: Double?,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("quantityUnit") val quantityUnit: String,
    @SerializedName("pricePerUnit") val pricePerUnit: Double,
    @SerializedName("totalCostActual") val totalCostActual: Double?,
    @SerializedName("fuelType") val fuelType: String,
    @SerializedName("serviceStation") val serviceStation: String?,
    @SerializedName("discountAmount") val discountAmount: Double?,
    @SerializedName("invoicePhotoUrl") val invoicePhotoUrl: String?,
    @SerializedName("invoicePhotoBase64") val invoicePhotoBase64: String? = null,
    @SerializedName("invoiceFileName") val invoiceFileName: String? = null,
    @SerializedName("voucherNumber") val voucherNumber: String?,
    @SerializedName("notes") val notes: String?
)

data class FuelLogResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("assetType") val assetType: String? = null,
    @SerializedName("assetId") val assetId: Long? = null,
    @SerializedName("assetPlate") val assetPlate: String? = null,
    @SerializedName("fuelDateTime") val fuelDateTime: String? = null,
    @SerializedName("odometerKm") val odometerKm: Double? = null,
    @SerializedName("hourMeter") val hourMeter: Double? = null,
    @SerializedName("quantity") val quantity: Double? = null,
    @SerializedName("quantityUnit") val quantityUnit: String? = null,
    @SerializedName("quantityGallons") val quantityGallons: Double? = null,
    @SerializedName("pricePerUnit") val pricePerUnit: Double? = null,
    @SerializedName("totalCostCalculated") val totalCostCalculated: Double? = null,
    @SerializedName("fuelType") val fuelType: String? = null,
    @SerializedName("serviceStation") val serviceStation: String? = null,
    @SerializedName("invoiceStatus") val invoiceStatus: String? = null,
    @SerializedName("invoicePhotoUrl") val invoicePhotoUrl: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("registeredBy") val registeredBy: String? = null,
    @SerializedName("efficiencyValue") val efficiencyValue: Double? = null,
    @SerializedName("efficiencyUnit") val efficiencyUnit: String? = null,
    @SerializedName("isAnomaly") val isAnomaly: Boolean = false
)
