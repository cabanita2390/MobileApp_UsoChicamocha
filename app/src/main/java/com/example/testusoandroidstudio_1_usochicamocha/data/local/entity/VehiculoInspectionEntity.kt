package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehiculo_inspections")
data class VehiculoInspectionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val UUID: String,
    val serverId: Long? = null,
    val timestamp: Long,
    
    // ── Inspección_pre_operativa ─────────────────────────────────────────────
    val placaVehiculo: String,
    val marca: String,
    val tipoVehiculo: String,
    val kilometrajeReportado: Int,
    val responsableInspeccion: String,
    val aprobadoRuta: Boolean,
    val observacionesFinales: String,

    // ── insp_detalle_mecanico ─────────────────────────────────────────────
    val nivelAceite: String,
    val nivelRefrigerante: String,
    val nivelFrenos: String,
    val estadoLlantas: String,
    val lucesGeneral: String,
    val estadoVisual: String,
    val limpiezaGeneral: String,

    // ── insp_detalle_documentos ───────────────────────────────────────────
    val checkSoat: String,
    val checkTecno: String,
    val checkLicencia: String,
    val checkExtintor: String,
    val vigenciaExtintor: String,
    val fechaVencSoat: String,
    val fechaVencTecno: String,
    val fechaVencLicencia: String,

    // ── insp_detalle_elementos ────────────────────────────────────────────
    val tieneBotiquin: Boolean,
    val tieneSeñalizacion: Boolean,
    val tieneLineasEmergencia: Boolean,
    val tieneLlantaRepuesto: Boolean,
    val tieneGatoHidraulico: Boolean,

    // ── insp_detalle_salud ────────────────────────────────────────────────
    val saludFisica: Boolean,
    val saludMental: Boolean,
    val sobrio: Boolean,
    val medicamentos: Boolean,
    val conscienteResponsabilidad: Boolean,
    val condicionParaConducir: Boolean,

    // ── Sync Status ──────────────────────────────────────────────────────
    var isSynced: Boolean = false,
    var isSyncing: Boolean = false
)
