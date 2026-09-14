package com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo

/**
 * Un objeto de datos simple (POJO) para encapsular la información necesaria
 * para sincronizar una imagen pendiente.
 */
data class ImageForSync(
    val localId: Int,
    val serverId: Long,
    val localUri: String,
    val tipo: String
)

