package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_logs_local")
data class FuelLogEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val remoteId: Long? = null,
    val syncId: String,
    val assetType: String,              // MACHINE / VEHICLE / MOTO
    val assetId: Long,
    val assetPlate: String? = null,
    val fuelDateTime: String,           // ISO 8601 con hora (ej. 2026-06-05T14:32:00)
    val odometerKm: Double? = null,     // vehículos y motos
    val hourMeter: Double? = null,      // maquinaria
    val quantity: Double,               // cantidad en la unidad elegida por el usuario
    val quantityUnit: String = "LITERS", // LITERS | GALLONS
    val quantityLiters: Double,         // normalizado a litros para cálculos
    val pricePerUnit: Double,           // precio en la unidad elegida
    val totalCostCalculated: Double,    // quantity × pricePerUnit — calculado automáticamente
    val totalCostActual: Double? = null, // total del tiquete físico — ingresado por el operario
    val totalCostMismatch: Boolean = false,
    val fuelType: String,               // GASOLINA_CORRIENTE / GASOLINA_EXTRA / DIESEL / ACPM / GAS_NATURAL
    val serviceStation: String? = null,
    val discountAmount: Double? = null,
    val invoicePhotoPath: String? = null, // ruta local antes de upload
    val invoicePhotoUrl: String? = null,  // URL en servidor tras sync
    val invoiceStatus: String = "PENDING_REVIEW",
    val voucherNumber: String? = null,
    val notes: String? = null,
    val efficiencyValue: Double? = null,  // devuelto por backend tras sync
    val efficiencyUnit: String? = null,   // KM_PER_LITER | LITER_PER_HOUR
    val isAnomaly: Boolean = false,
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false,
    val createdAt: Long                   // epoch ms
)
