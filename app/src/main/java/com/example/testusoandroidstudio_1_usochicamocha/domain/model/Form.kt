package com.example.testusoandroidstudio_1_usochicamocha.domain.model

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity

/**
 * Representa una inspección en la capa de Dominio.
 * Es el objeto de datos "limpio" que usa la lógica de negocio y la UI.
 */
data class Form(
    val localId: Int,
    // CORRECCIÓN: Se añade la propiedad serverId, que puede ser nula.
    val serverId: Long?,
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
    val isSynced: Boolean,
    val isSyncing: Boolean,
    /** Presión por llanta en JSON (ver PresionLlantasJson); "" = sin lecturas. */
    val presionesLlantasJson: String = ""
)

/**
 * Función de extensión para mapear el objeto de Dominio a una Entidad de base de datos.
 */
fun Form.toEntity(): FormEntity {
    return FormEntity(
        localId = this.localId,
        // CORRECCIÓN: Se añade el mapeo de serverId.
        serverId = this.serverId,
        UUID = this.UUID,
        timestamp = this.timestamp,
        machineId = this.machineId,
        userId = this.userId,
        hourmeter = this.hourmeter,
        leakStatus = this.leakStatus,
        brakeStatus = this.brakeStatus,
        beltsPulleysStatus = this.beltsPulleysStatus,
        tireLanesStatus = this.tireLanesStatus,
        carIgnitionStatus = this.carIgnitionStatus,
        electricalStatus = this.electricalStatus,
        mechanicalStatus = this.mechanicalStatus,
        temperatureStatus = this.temperatureStatus,
        oilStatus = this.oilStatus,
        hydraulicStatus = this.hydraulicStatus,
        coolantStatus = this.coolantStatus,
        structuralStatus = this.structuralStatus,
        expirationDateFireExtinguisher = this.expirationDateFireExtinguisher,
        observations = this.observations,
        greasingAction = this.greasingAction,
        greasingObservations = this.greasingObservations,
        isUnexpected = this.isUnexpected,
        isSynced = this.isSynced,
        isSyncing = this.isSyncing,
        presionesLlantasJson = this.presionesLlantasJson
    )
}

