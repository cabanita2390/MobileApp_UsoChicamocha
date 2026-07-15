package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "vehicle_oil_changes_improved",
    indices = [Index(value = ["placa"], name = "idx_vehicle_oil_changes_improved_placa")]
)
data class VehicleOilChangeImprovedEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "placa")
    val placa: String,

    @ColumnInfo(name = "oil_type")
    val oilType: String,

    @ColumnInfo(name = "brand_name")
    val brandName: String?,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "km_at_change")
    val kmAtChange: Int,

    @ColumnInfo(name = "next_change_km")
    val nextChangeKm: Int,

    @ColumnInfo(name = "percentage_used")
    val percentageUsed: Int,

    @ColumnInfo(name = "air_filter_changed")
    val airFilterChanged: Boolean = false,

    @ColumnInfo(name = "date_stamp")
    val dateStamp: String,

    @ColumnInfo(name = "oil_durability")
    val oilDurability: String,

    @ColumnInfo(name = "requirement_id")
    val requirementId: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "sync_error")
    val syncError: String? = null
)
