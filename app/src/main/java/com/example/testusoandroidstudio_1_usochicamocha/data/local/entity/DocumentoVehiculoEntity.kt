package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Caché local de documentos (SOAT, REVISION_TECNO, LICENCIA, EXTINTOR) por placa de vehículo.
 * Se actualiza cada vez que se consulta una placa con internet.
 * Permite cargar los documentos en modo sin conexión.
 */
@Entity(tableName = "cache_documentos_vehiculo")
data class DocumentoVehiculoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val placa: String,
    val tipoDocumento: String,
    val vigencia: String,
    val imagenUrl: String?,
    val kilometrajeActual: Int,
    val cachedAt: Long = System.currentTimeMillis()
)
