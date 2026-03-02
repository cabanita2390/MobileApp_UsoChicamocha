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
    var isSynced: Boolean = false,
    var isSyncing: Boolean = false
)

fun InspeccionMotoEntity.toDomain() = InspeccionMotoPendiente(
    localId = localId,
    serverId = serverId,
    uuid = uuid,
    timestamp = timestamp,
    idVehiculo = idVehiculo,
    idUbicacion = idUbicacion,
    kilometrajeReportado = kilometrajeReportado,
    estadoGeneral = estadoGeneral,
    observacionesFinales = observacionesFinales,
    vigenciaSoat = vigenciaSoat,
    estadoSoat = estadoSoat,
    vigenciaRevision = vigenciaRevision,
    estadoRevision = estadoRevision,
    vigenciaLicencia = vigenciaLicencia,
    estadoLicencia = estadoLicencia,
    imagenSoat = imagenSoat,
    imagenRevision = imagenRevision,
    imagenLicencia = imagenLicencia,
    isSynced = isSynced,
    isSyncing = isSyncing
)

fun InspeccionMotoPendiente.toEntity() = InspeccionMotoEntity(
    localId = localId,
    serverId = serverId,
    uuid = uuid,
    timestamp = timestamp,
    idVehiculo = idVehiculo,
    idUbicacion = idUbicacion,
    kilometrajeReportado = kilometrajeReportado,
    estadoGeneral = estadoGeneral,
    observacionesFinales = observacionesFinales,
    vigenciaSoat = vigenciaSoat,
    estadoSoat = estadoSoat,
    vigenciaRevision = vigenciaRevision,
    estadoRevision = estadoRevision,
    vigenciaLicencia = vigenciaLicencia,
    estadoLicencia = estadoLicencia,
    imagenSoat = imagenSoat,
    imagenRevision = imagenRevision,
    imagenLicencia = imagenLicencia,
    isSynced = isSynced,
    isSyncing = isSyncing
)
