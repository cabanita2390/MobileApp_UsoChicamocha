package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity

/**
 * Caché del perfil de llantas de cada activo, para pedir y evaluar la presión sin conexión.
 * [tipoActivo] es VEHICULO (vehículos y motos, id de `vehiculos`) o MAQUINA (id de `machines`).
 * Las posiciones se guardan como JSON: solo se leen completas, nunca se consultan por campo.
 */
@Entity(tableName = "llanta_config_cache", primaryKeys = ["tipoActivo", "activoId"])
data class LlantaConfigCacheEntity(
    val tipoActivo: String,
    val activoId: Long,
    val perfilNombre: String,
    val posicionesJson: String
) {
    companion object {
        const val TIPO_VEHICULO = "VEHICULO"
        const val TIPO_MAQUINA = "MAQUINA"
    }
}
