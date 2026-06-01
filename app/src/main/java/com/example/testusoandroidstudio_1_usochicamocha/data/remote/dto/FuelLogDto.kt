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
    @SerializedName("isFullTank") val isFullTank: Boolean,
    @SerializedName("discountAmount") val discountAmount: Double?,
    @SerializedName("invoicePhotoUrl") val invoicePhotoUrl: String?,
    @SerializedName("voucherNumber") val voucherNumber: String?,
    @SerializedName("notes") val notes: String?
)

data class FuelLogResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("assetType") val assetType: String,
    @SerializedName("assetId") val assetId: Long,
    @SerializedName("assetPlate") val assetPlate: String?,
    @SerializedName("fuelDateTime") val fuelDateTime: String,
    @SerializedName("odometerKm") val odometerKm: Double?,
    @SerializedName("hourMeter") val hourMeter: Double?,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("quantityUnit") val quantityUnit: String,
    @SerializedName("quantityGallons") val quantityGallons: Double,
    @SerializedName("pricePerUnit") val pricePerUnit: Double,
    @SerializedName("totalCostCalculated") val totalCostCalculated: Double,
    @SerializedName("fuelType") val fuelType: String,
    @SerializedName("serviceStation") val serviceStation: String?,
    @SerializedName("isFullTank") val isFullTank: Boolean,
    @SerializedName("invoiceStatus") val invoiceStatus: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("registeredBy") val registeredBy: String?,
    @SerializedName("efficiencyValue") val efficiencyValue: Double?,
    @SerializedName("efficiencyUnit") val efficiencyUnit: String?,
    @SerializedName("isAnomaly") val isAnomaly: Boolean = false
)
