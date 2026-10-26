package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ConfigLlantas

interface LlantasRepository {
    /** Descarga los perfiles asignados y reemplaza la caché; si falla, la caché queda como estaba. */
    suspend fun sincronizar(): Result<Unit>

    /** Perfil del vehículo o moto (id de `vehiculos`); null si no tiene. */
    suspend fun configVehiculo(idVehiculo: Int): ConfigLlantas?

    /** Perfil de la máquina; null si no tiene. */
    suspend fun configMaquina(idMaquina: Long): ConfigLlantas?
}
