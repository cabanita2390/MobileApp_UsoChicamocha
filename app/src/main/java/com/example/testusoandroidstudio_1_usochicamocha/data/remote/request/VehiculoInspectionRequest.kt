package com.example.testusoandroidstudio_1_usochicamocha.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * DTO que se envía al backend POST /api/v1/vehicle-inspection.
 * Los nombres de los campos coinciden EXACTAMENTE con VehiculoInspectionRequest.java
 * del backend (Spring Boot).
 *
 * Origen de datos: VehiculoInspectionFormData en VehiculoViewModel.kt
 */
data class VehiculoInspectionRequest(

    // ── Inspección_pre_operativa ─────────────────────────────────────────────
    val placaVehiculo: String,
    val marca: String = "",          // informativo, ya está en BD
    val tipoVehiculo: String = "",   // informativo, ya está en BD
    val kilometrajeReportado: Int,
    val responsableInspeccion: String,
    val aprobadoRuta: Boolean,
    val observacionesFinales: String,

    // ── insp_detalle_mecanico ────────────────────────────────────────────────
    val nivelAceite: String,
    val nivelRefrigerante: String,
    val nivelFrenos: String,
    val estadoLlantas: String,
    val lucesGeneral: String,
    val estadoVisual: String,
    val limpiezaGeneral: String,

    // ── insp_detalle_documentos + documentacion_y_elementos ──────────────────
    val checkSoat: String,
    val checkTecno: String,
    val checkLicencia: String,
    val checkExtintor: String,
    val vigenciaExtintor: String,        // "YYYY-MM"
    val fechaVencSoat: String,           // "YYYY-MM-DD"
    val fechaVencTecno: String,          // "YYYY-MM-DD"
    val fechaVencLicencia: String,       // "YYYY-MM-DD"

    // ── insp_detalle_elementos ────────────────────────────────────────────────
    val tieneBotiquin: Boolean,

    // Nota: el JSON se serializa como "tieneSeñalizacion" (con ñ)
    // Si el servidor da error de null, cambiar a @SerializedName("tieneSenalizacion")
    @SerializedName("tieneSeñalizacion")
    val tieneSeñalizacion: Boolean,

    val tieneLineasEmergencia: Boolean,
    val tieneLlantaRepuesto: Boolean,
    val tieneGatoHidraulico: Boolean,

    // ── insp_detalle_salud ───────────────────────────────────────────────────
    val saludFisica: Boolean,
    val saludMental: Boolean,
    val sobrio: Boolean,
    val medicamentos: Boolean,
    val conscienteResponsabilidad: Boolean,
    val condicionParaConducir: Boolean
)
