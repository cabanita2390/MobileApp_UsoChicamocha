package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrosLocalesTest {

    private fun cita(id: Long, cumple: Boolean = false) = CitaProgramada(
        programacionId = id,
        anio = 2026,
        mes = 10,
        estacionId = 1,
        estacionNombre = "Cuche",
        estacionTipo = "BOMBEO",
        actividadId = 1,
        actividadNombre = "Pintura muros estaciones",
        ejecutado = if (cumple) 1 else 0,
        cumple = cumple
    )

    private fun local(programacionId: Long?, serverId: Long? = null) = EjecucionEntity(
        serverId = serverId,
        uuidCliente = "uuid-$programacionId-$serverId",
        fecha = "2026-10-06",
        mesEjecucion = 10,
        semanaEjecucion = 1,
        estacionId = 1,
        estacionNombre = "Cuche",
        tipoMantenimiento = "PREVENTIVO",
        tipoActividad = "MANTENIMIENTO",
        actividadId = 1,
        programacionId = programacionId,
        esProgramada = programacionId != null,
        resultado = "CONFORME",
        observaciones = "ok",
        isSynced = serverId != null
    )

    @Test
    fun `cita registrada sin señal queda cumplida y en cola`() {
        val r = marcarRegistrosLocales(listOf(cita(84)), listOf(local(84))).single()
        assertTrue(r.cumple)
        assertTrue(r.enCola)
        assertNull(r.ejecucionId)
        assertEquals(1, r.ejecutado)
    }

    @Test
    fun `cita enviada pero con el caché sin refrescar abre el detalle del servidor`() {
        val r = marcarRegistrosLocales(listOf(cita(84)), listOf(local(84, serverId = 70))).single()
        assertTrue(r.cumple)
        assertFalse(r.enCola)
        assertEquals(70L, r.ejecucionId)
    }

    @Test
    fun `no toca citas sin registro local ni las que el servidor ya da por cumplidas`() {
        val citas = listOf(cita(84), cita(85, cumple = true))
        val r = marcarRegistrosLocales(citas, listOf(local(85), local(null)))
        assertEquals(citas, r)
    }
}
