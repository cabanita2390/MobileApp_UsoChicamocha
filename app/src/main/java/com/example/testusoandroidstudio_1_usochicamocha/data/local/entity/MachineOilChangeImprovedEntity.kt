package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "machine_oil_changes_improved",
    indices = [Index(value = ["machine_id"], name = "idx_machine_oil_changes_improved_machine_id")]
)
data class MachineOilChangeImprovedEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "machine_id")
    val machineId: Long,

    @ColumnInfo(name = "oil_type")
    val oilType: String,

    @ColumnInfo(name = "brand_name")
    val brandName: String?,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "hour_stamp")
    val hourStamp: Int,

    @ColumnInfo(name = "next_change_hours")
    val nextChangeHours: Int,

    @ColumnInfo(name = "percentage_used")
    val percentageUsed: Int,

    @ColumnInfo(name = "motor_oil")
    val motorOil: Boolean = true,

    @ColumnInfo(name = "hydraulic_oil")
    val hydraulicOil: Boolean = false,

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
