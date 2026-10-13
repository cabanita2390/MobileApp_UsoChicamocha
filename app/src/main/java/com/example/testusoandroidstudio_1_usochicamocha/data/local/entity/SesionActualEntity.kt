package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una sola fila (id = 1) con el usuario de la sesión abierta. Las colas de sincronización la
 * consultan en SQL para enviar solo los registros de ese usuario (ver PropietarioColas).
 */
@Entity(tableName = "sesion_actual")
data class SesionActualEntity(
    @PrimaryKey val id: Int = 1,
    val usuarioId: Long?
)
