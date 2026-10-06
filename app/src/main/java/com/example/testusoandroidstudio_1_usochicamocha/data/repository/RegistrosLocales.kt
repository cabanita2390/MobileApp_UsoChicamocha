package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada

/**
 * Marca como cumplidas las citas que ya tienen un registro hecho en este equipo, aunque el
 * servidor todavía no lo sepa (registro en cola, o enviado pero sin refrescar el caché).
 * Una cita que el servidor ya da por cumplida no se toca.
 */
internal fun marcarRegistrosLocales(
    citas: List<CitaProgramada>,
    locales: List<EjecucionEntity>
): List<CitaProgramada> {
    if (locales.isEmpty()) return citas
    val porCita = locales.groupBy { it.programacionId }
    return citas.map { cita ->
        val propios = porCita[cita.programacionId]
        if (propios.isNullOrEmpty() || cita.cumple) {
            cita
        } else {
            val serverId = propios.firstNotNullOfOrNull { it.serverId }
            cita.copy(
                cumple = true,
                ejecutado = maxOf(cita.ejecutado, propios.size),
                ejecucionId = serverId,
                enCola = serverId == null
            )
        }
    }
}
