package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente

@Entity(tableName = "pending_inspecciones_moto")
data class InspeccionMotoEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
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

    // Ubicacion
    val idUbicacion: Int,

    var isSynced: Boolean = false,
    var isSyncing: Boolean = false,

    // Fechas (Al final para evitar problemas de constructor en Room)
    val fechaSoat: String = "",
    val fechaTecno: String = "",
    val fechaLicencia: String = "",

    // Inspección Mecánica Moto (Bueno / Regular / Malo) — default "" por retrocompatibilidad
    val checkNivelAceite: String = "",
    val checkEstadoLlantas: String = "",
    val checkEstadoLuces: String = "",

    // Para visualización en lista (Motos vs Vehículos)
    val placaVehiculo: String = "",
    val tipoVehiculo: String = ""
)

fun InspeccionMotoEntity.toDomain() = InspeccionMotoPendiente(
    localId = localId,
    serverId = serverId,
    uuid = uuid,
    timestamp = timestamp,
    idVehiculo = idVehiculo,
    kilometrajeReportado = kilometrajeReportado,
    estadoVehiculo = estadoVehiculo,
    conscienteResponsabilidad = conscienteResponsabilidad,
    aprobadoRuta = aprobadoRuta,
    observacionesFinales = observacionesFinales,
    checkSoat = checkSoat,
    checkTecno = checkTecno,
    checkLicencia = checkLicencia,
    checkExtintor = checkExtintor,
    checkNivelAceite = checkNivelAceite,
    checkEstadoLlantas = checkEstadoLlantas,
    checkEstadoLuces = checkEstadoLuces,
    fechaSoat = fechaSoat,
    fechaTecno = fechaTecno,
    fechaLicencia = fechaLicencia,
    idUbicacion = idUbicacion,
    placaVehiculo = placaVehiculo,
    tipoVehiculo = tipoVehiculo,
    isSynced = isSynced,
    isSyncing = isSyncing
)

fun InspeccionMotoPendiente.toEntity() = InspeccionMotoEntity(
    localId = localId,
    serverId = serverId,
    uuid = uuid,
    timestamp = timestamp,
    idVehiculo = idVehiculo,
    kilometrajeReportado = kilometrajeReportado,
    estadoVehiculo = estadoVehiculo,
    conscienteResponsabilidad = conscienteResponsabilidad,
    aprobadoRuta = aprobadoRuta,
    observacionesFinales = observacionesFinales,
    checkSoat = checkSoat,
    checkTecno = checkTecno,
    checkLicencia = checkLicencia,
    checkExtintor = checkExtintor,
    checkNivelAceite = checkNivelAceite,
    checkEstadoLlantas = checkEstadoLlantas,
    checkEstadoLuces = checkEstadoLuces,
    fechaSoat = fechaSoat,
    fechaTecno = fechaTecno,
    fechaLicencia = fechaLicencia,
    idUbicacion = idUbicacion,
    placaVehiculo = placaVehiculo,
    tipoVehiculo = tipoVehiculo,
    isSynced = isSynced,
    isSyncing = isSyncing
)
