package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form

@Entity(tableName = "pending_forms")
data class FormEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    // CORRECCIÓN: El orden de los campos ahora coincide con el modelo de dominio
    val serverId: Long? = null,
    val UUID: String,
    val timestamp: Long,
    val machineId: Long,
    val userId: Long,
    val hourmeter: String,
    val leakStatus: String,
    val brakeStatus: String,
    val beltsPulleysStatus: String,
    val tireLanesStatus: String,
    val carIgnitionStatus: String,
    val electricalStatus: String,
    val mechanicalStatus: String,
    val temperatureStatus: String,
    val oilStatus: String,
    val hydraulicStatus: String,
    val coolantStatus: String,
    val structuralStatus: String,
    val expirationDateFireExtinguisher: String,
    val observations: String,
    val greasingAction: String,
    val greasingObservations: String,
    val isUnexpected: Boolean,
    var isSynced: Boolean = false,
    var isSyncing: Boolean = false,
    /** Usuario que hizo el registro (ver PropietarioColas); null = de la sesión actual. */
    val propietarioId: Long? = null,
    /** Presión por llanta (JSON de LecturaPresionLlanta, ver PresionLlantasJson); "" = sin lecturas. */
    @ColumnInfo(defaultValue = "") val presionesLlantasJson: String = ""
)

fun FormEntity.toDomain(): Form {
    // CORRECCIÓN: El orden de los parámetros en el constructor de Form() ahora es el correcto,
    // asegurando que cada valor de la entidad se mapee a la propiedad correcta en el dominio.
    return Form(
        localId = localId,
        serverId = this.serverId,
        UUID = this.UUID,
        timestamp = timestamp,
        machineId = machineId,
        userId = userId,
        hourmeter = hourmeter,
        leakStatus = leakStatus,
        brakeStatus = brakeStatus,
        beltsPulleysStatus = beltsPulleysStatus,
        tireLanesStatus = tireLanesStatus,
        carIgnitionStatus = carIgnitionStatus,
        electricalStatus = electricalStatus,
        mechanicalStatus = mechanicalStatus,
        temperatureStatus = temperatureStatus,
        oilStatus = oilStatus,
        hydraulicStatus = hydraulicStatus,
        coolantStatus = coolantStatus,
        structuralStatus = structuralStatus,
        expirationDateFireExtinguisher = expirationDateFireExtinguisher,
        observations = observations,
        greasingAction = this.greasingAction,
        greasingObservations = this.greasingObservations,
        isUnexpected = this.isUnexpected,
        isSynced = isSynced,
        isSyncing = isSyncing,
        presionesLlantasJson = presionesLlantasJson
    )
}

