package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm

@Entity(tableName = "maintenance_forms")
data class MachineOilChangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val machineId: Int,
    val brandId: Int,
    val dateTime: Long,
    val brand: String,
    val quantity: Double,
    val currentHourMeter: Int,
    val averageHoursChange: Int,
    val type: String, // "motor" o "hydraulic"
    val isSynced: Boolean,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

fun MachineOilChangeEntity.toDomain(): MachineOilChangeForm {
    return MachineOilChangeForm(
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
