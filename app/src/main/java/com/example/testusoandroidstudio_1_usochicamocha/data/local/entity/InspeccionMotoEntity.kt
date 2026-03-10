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
    val fechaLicencia: String = ""
)

fun InspeccionMotoEntity.toDomain() = InspeccionMotoPendiente(
    localId = localId,
    serverId = serverId,
    uuid = uuid,
    timestamp = timestamp,
    idVehiculo = idVehiculo,
    kilometrajeReportado = kilometrajeReportado,
    estadoVehiculo = estadoVehiculo,
    observacionesFinales = observacionesFinales,
    checkSoat = checkSoat,
    checkTecno = checkTecno,
    checkLicencia = checkLicencia,
    checkExtintor = checkExtintor,
    fechaSoat = fechaSoat,
    fechaTecno = fechaTecno,
    fechaLicencia = fechaLicencia,
    idUbicacion = idUbicacion,
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
    observacionesFinales = observacionesFinales,
    checkSoat = checkSoat,
    checkTecno = checkTecno,
    checkLicencia = checkLicencia,
    checkExtintor = checkExtintor,
    fechaSoat = fechaSoat,
    fechaTecno = fechaTecno,
    fechaLicencia = fechaLicencia,
    idUbicacion = idUbicacion,
    isSynced = isSynced,
    isSyncing = isSyncing
)
