package com.example.testusoandroidstudio_1_usochicamocha.domain.model

data class InspeccionMotoPendiente(
    val localId: Int = 0,
    val serverId: Long? = null,
    val uuid: String,
    val timestamp: Long,
    
    // Cabecera
    val idVehiculo: Int,
    val kilometrajeReportado: Int,
    val estadoVehiculo: String,
    val observacionesFinales: String,

    // Documentos
    val checkSoat: String,
    val checkTecno: String,
    val checkLicencia: String,
    val checkExtintor: String,

    // Fechas para persistencia/sincronización
    val fechaSoat: String,
    val fechaTecno: String,
    val fechaLicencia: String,

    // Ubicacion
    val idUbicacion: Int,

    val isSynced: Boolean = false,
    val isSyncing: Boolean = false
)
