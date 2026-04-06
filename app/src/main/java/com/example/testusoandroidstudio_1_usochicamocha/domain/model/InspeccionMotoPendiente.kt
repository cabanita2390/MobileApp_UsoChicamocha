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
    val conscienteResponsabilidad: String = "",
    val aprobadoRuta: String = "",
    val observacionesFinales: String,

    // Documentos
    val checkSoat: String,
    val checkTecno: String,
    val checkLicencia: String,
    val checkExtintor: String,

    // Inspección Mecánica Moto (Bueno / Regular / Malo)
    val checkNivelAceite: String = "",
    val checkEstadoLlantas: String = "",
    val checkEstadoLuces: String = "",

    // Fechas para persistencia/sincronización
    val fechaSoat: String,
    val fechaTecno: String,
    val fechaLicencia: String,

    // Ubicacion
    val idUbicacion: Int,

    // Datos para visualización en lista (igual que vehículos)
    val placaVehiculo: String = "",
    val tipoVehiculo: String = "",

    val isSynced: Boolean = false,
    val isSyncing: Boolean = false
)
