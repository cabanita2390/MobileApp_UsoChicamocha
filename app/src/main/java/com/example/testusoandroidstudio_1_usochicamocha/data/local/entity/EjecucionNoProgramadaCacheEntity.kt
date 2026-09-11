package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada

/**
 * Caché de ejecuciones NO programadas (sin cita de cronograma asociada, `esProgramada=false`
 * en el backend) de TODAS las estaciones — alimenta la pestaña "Realizadas" de Pendientes
 * junto con [CumplimientoCacheEntity]. Una actividad civil "no prevista" (ej. un imprevisto
 * atendido fuera de cronograma) queda guardada correctamente en `mant_ejecucion` en el
 * servidor, pero nunca aparece en `v_mant_cumplimiento` porque esa vista arranca desde
 * `mant_programacion` — de ahí que necesite su propio caché en vez de reusar
 * CumplimientoCacheEntity (que es "una fila por programacionId").
 *
 * Igual que CumplimientoCacheEntity, se guarda ya con los campos resueltos que necesita la
 * UI (no el JSON crudo como EjecucionDetalleCacheEntity) para poder filtrar/agrupar por
 * año+mes en el DAO.
 */
@Entity(tableName = "mant_ejecucion_no_programada_cache")
data class EjecucionNoProgramadaCacheEntity(
    @PrimaryKey val ejecucionId: Long,
    val anio: Int,
    val mes: Int,
    val estacionId: Long,
    val estacionNombre: String,
    val actividadNombre: String
)

/**
 * Se modela como una CitaProgramada sintética ya EJECUTADA (ver el comentario de
 * CitaProgramada) para poder reusar `estadoDeCita`/`agrupar` sin duplicar esa lógica en
 * Pendientes. `programacionId = -ejecucionId` es solo una key negativa (nunca colisiona con
 * un id real de programación); la navegación a Detalle usa `ejecucionId` directamente.
 */
fun EjecucionNoProgramadaCacheEntity.toDomain(): CitaProgramada {
    return CitaProgramada(
        programacionId = -ejecucionId,
        anio = anio,
        mes = mes,
        estacionId = estacionId,
        estacionNombre = estacionNombre,
        estacionTipo = "",
        actividadId = -1L,
        actividadNombre = actividadNombre,
        ejecutado = 1,
        cumple = true,
        esProgramada = false,
        ejecucionId = ejecucionId
    )
}
