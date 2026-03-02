package com.example.testusoandroidstudio_1_usochicamocha.domain.model

data class InspeccionMotoPendiente(
    val localId: Int = 0,
    val serverId: Long? = null,
    val uuid: String,
    val timestamp: Long,
    val idVehiculo: Int,
    val idUbicacion: Int,
    val kilometrajeReportado: Int,
    val estadoGeneral: String,
    val observacionesFinales: String,
    val vigenciaSoat: String?,
    val estadoSoat: String?,
    val vigenciaRevision: String?,
    val estadoRevision: String?,
    val vigenciaLicencia: String?,
    val estadoLicencia: String?,
    val imagenSoat: String?,
    val imagenRevision: String?,
    val imagenLicencia: String?,
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false
)
