package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.SesionActualEntity

/** Ver PropietarioColas. */
@Dao
interface PropietarioColasDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fijarSesion(sesion: SesionActualEntity)

    @Query("DELETE FROM sesion_actual")
    suspend fun limpiarSesion()

    @Query("SELECT usuarioId FROM sesion_actual WHERE id = 1")
    suspend fun usuarioEnSesion(): Long?

    @Query("UPDATE pending_forms SET propietarioId = :usuarioId WHERE propietarioId IS NULL AND isSynced = 0")
    suspend fun asignarFormularios(usuarioId: Long)

    @Query("UPDATE maintenance_forms SET propietarioId = :usuarioId WHERE propietarioId IS NULL AND isSynced = 0")
    suspend fun asignarAceiteMaquinaria(usuarioId: Long)

    @Query("UPDATE pending_inspecciones_moto SET propietarioId = :usuarioId WHERE propietarioId IS NULL AND isSynced = 0")
    suspend fun asignarInspeccionesMoto(usuarioId: Long)

    @Query("UPDATE vehiculo_inspections SET propietarioId = :usuarioId WHERE propietarioId IS NULL AND isSynced = 0")
    suspend fun asignarInspeccionesVehiculo(usuarioId: Long)

    @Query("UPDATE vehiculo_oil_changes SET propietarioId = :usuarioId WHERE propietarioId IS NULL AND isSynced = 0")
    suspend fun asignarAceiteVehiculo(usuarioId: Long)

    @Query("UPDATE moto_oil_changes SET propietarioId = :usuarioId WHERE propietarioId IS NULL AND isSynced = 0")
    suspend fun asignarAceiteMoto(usuarioId: Long)

    @Query("UPDATE pending_mant_ejecucion SET propietarioId = :usuarioId WHERE propietarioId IS NULL AND isSynced = 0")
    suspend fun asignarEjecuciones(usuarioId: Long)

    @Transaction
    suspend fun asignarPendientesSinDueno(usuarioId: Long) {
        asignarFormularios(usuarioId)
        asignarAceiteMaquinaria(usuarioId)
        asignarInspeccionesMoto(usuarioId)
        asignarInspeccionesVehiculo(usuarioId)
        asignarAceiteVehiculo(usuarioId)
        asignarAceiteMoto(usuarioId)
        asignarEjecuciones(usuarioId)
    }
}
