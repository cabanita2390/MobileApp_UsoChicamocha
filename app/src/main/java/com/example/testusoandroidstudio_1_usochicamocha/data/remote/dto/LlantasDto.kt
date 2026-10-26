package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

/** Respuesta de `GET v1/tire/asignaciones`: perfil vigente de un activo (vehicleId o machineId). */
data class ConfigLlantasActivoDto(
    val vehicleId: Int? = null,
    val machineId: Long? = null,
    val perfilId: Long = 0,
    val perfilNombre: String? = null,
    val aplicaA: String? = null,
    val posiciones: List<PosicionLlantaDto>? = null
)

data class PosicionLlantaDto(
    val codigo: String? = null,
    val etiqueta: String? = null,
    val orden: Int? = null,
    val psiMin: Double? = null,
    val psiObjetivo: Double? = null,
    val psiMax: Double? = null
)

/** Lectura enviada en `presionesLlantas` de las tres inspecciones (campos de LecturaPresionLlantaRequest.java). */
data class LecturaPresionLlantaDto(
    val codigoPosicion: String,
    val psi: Double,
    val etiquetaPosicion: String? = null,
    val psiMinReferencia: Double? = null,
    val psiMaxReferencia: Double? = null
)
