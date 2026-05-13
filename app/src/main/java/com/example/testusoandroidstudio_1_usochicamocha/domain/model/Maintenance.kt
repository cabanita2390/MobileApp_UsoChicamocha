package com.example.testusoandroidstudio_1_usochicamocha.domain.model

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MaintenanceEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.MaintenanceDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.OilChangeRequest
import java.text.SimpleDateFormat
import java.util.*

data class Maintenance(
    val id: Int = 0,
    val machineId: Int,
    val dateTime: Long,
    val brand: String,
    val brandId: Int,
    val quantity: Double,
    val currentHourMeter: Int,
    val averageHoursChange: Int,
    val type: String,
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

fun Maintenance.toEntity(): MaintenanceEntity {
    return MaintenanceEntity(
        id = this.id,
        machineId = this.machineId,
        dateTime = this.dateTime,
        brand = this.brand,
        brandId = this.brandId,
        quantity = this.quantity,
        currentHourMeter = this.currentHourMeter,
        averageHoursChange = this.averageHoursChange,
        type = this.type,
        isSynced = this.isSynced,
        isSyncing = this.isSyncing,
        syncError = this.syncError
    )
}
fun Maintenance.toDto(): MaintenanceDto {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val isoDateString = sdf.format(Date(this.dateTime))

    return MaintenanceDto(
        machineId = this.machineId,
        dateTime = isoDateString,
        brand = this.brand,
        quantity = this.quantity,
        currentHourMeter = this.currentHourMeter,
        averageHoursChange = this.averageHoursChange
    )
}
fun Maintenance.toOilChangeRequest(): OilChangeRequest {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val isoDateString = sdf.format(Date(this.dateTime))

    return OilChangeRequest(
        machineId = this.machineId,
        dateTime = isoDateString,
        brandId = this.brandId.toLong(),
        quantity = this.quantity.toDouble(),
        currentHourMeter = this.currentHourMeter.toDouble(),
        averageHoursChange = this.averageHoursChange
    )
}
