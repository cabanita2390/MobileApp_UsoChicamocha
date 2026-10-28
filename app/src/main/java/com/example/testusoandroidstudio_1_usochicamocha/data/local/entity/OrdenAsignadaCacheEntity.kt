package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.OrdenAsignada

/**
 * Orden de trabajo (OT-EB) asignada al operario: la lista "Prioritarias" del Home, offline.
 * Se guarda con su `usuarioId` porque los teléfonos se comparten: cada operario ve solo las suyas
 * y las que son para cualquiera, aunque no haya señal (mismo criterio que las colas con `sesion_actual`).
 */
@Entity(tableName = "mant_orden_asignada_cache", primaryKeys = ["id", "usuarioId"])
data class OrdenAsignadaCacheEntity(
    // Llave (orden, usuario): una orden para cualquiera le llega a varios operarios del mismo teléfono
    // con el mismo id; con solo `id` la del último que sincronizaba borraba la de los demás.
    val id: Long,
    val usuarioId: Long,
    val consecutivo: String,
    val prioridad: String,
    val fechaLimite: String,
    val vencida: Boolean,
    val estacionId: Long,
    val estacionNombre: String,
    val actividadId: Long,
    val actividadNombre: String,
    val instrucciones: String,
    val hallazgoFecha: String,
    val hallazgoResultado: String,
    val hallazgoObservaciones: String,
    val motivoDevolucion: String?,
    /** Rutas de las fotos del hallazgo, una por línea. */
    val fotosHallazgo: String,
    /** Para cualquier operario: la ven todos y queda a nombre del primero que la registre. */
    @ColumnInfo(defaultValue = "0")
    val paraCualquiera: Boolean = false,
    /** Disciplina del hallazgo (la visita se registra en esa disciplina). */
    @ColumnInfo(defaultValue = "CIVIL")
    val disciplina: String = "CIVIL"
)

fun OrdenAsignadaCacheEntity.toDomain() = OrdenAsignada(
    id = id,
    consecutivo = consecutivo,
    prioridad = prioridad,
    fechaLimite = fechaLimite,
    vencida = vencida,
    estacionId = estacionId,
    estacionNombre = estacionNombre,
    actividadId = actividadId,
    actividadNombre = actividadNombre,
    instrucciones = instrucciones,
    hallazgoFecha = hallazgoFecha,
    hallazgoResultado = hallazgoResultado,
    hallazgoObservaciones = hallazgoObservaciones,
    motivoDevolucion = motivoDevolucion,
    fotosHallazgo = fotosHallazgo.split('\n').filter { it.isNotBlank() },
    paraCualquiera = paraCualquiera,
    disciplina = disciplina
)
