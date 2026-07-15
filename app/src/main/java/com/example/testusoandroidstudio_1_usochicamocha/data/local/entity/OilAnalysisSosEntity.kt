package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "oil_analysis_sos",
    indices = [Index(value = ["machine_id"], name = "idx_oil_analysis_sos_machine_id")]
)
data class OilAnalysisSosEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "machine_id")
    val machineId: Long,

    @ColumnInfo(name = "machine_name")
    val machineName: String,

    @ColumnInfo(name = "analysis_date")
    val analysisDate: String,

    @ColumnInfo(name = "oil_type")
    val oilType: String,

    @ColumnInfo(name = "hours_at_analysis")
    val hoursAtAnalysis: Int,

    @ColumnInfo(name = "next_change_hours")
    val nextChangeHours: Int,

    @ColumnInfo(name = "sos_report_url")
    val sosReportUrl: String,

    @ColumnInfo(name = "approved_by_mechanic")
    val approvedByMechanic: String,

    @ColumnInfo(name = "observations")
    val observations: String? = null,

    @ColumnInfo(name = "is_approved")
    val isApproved: Boolean = false,

    @ColumnInfo(name = "extended_hours")
    val extendedHours: Int = 0,

    @ColumnInfo(name = "authorizes_extension")
    val authorizesExtension: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "local_created_at")
    val localCreatedAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "sync_error")
    val syncError: String? = null
)
