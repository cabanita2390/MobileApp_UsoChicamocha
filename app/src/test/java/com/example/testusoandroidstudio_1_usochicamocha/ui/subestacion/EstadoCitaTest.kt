package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class EstadoCitaTest {

    private val hoy = LocalDate.of(2026, 9, 16)

    private fun cita(anio: Int, mes: Int, cumple: Boolean) = CitaProgramada(
        programacionId = 1,
        anio = anio,
        mes = mes,
        estacionId = 1,
        estacionNombre = "Cuche",
        estacionTipo = "BOMBEO",
        actividadId = 1,
        actividadNombre = "Pintura muros estaciones",
        ejecutado = if (cumple) 1 else 0,
        cumple = cumple
    )

    @Test
    fun `cita de mes futuro sin ejecutar es PROGRAMADA`() {
        assertEquals(EstadoCita.PROGRAMADA, estadoDeCita(cita(2026, 10, cumple = false), hoy))
    }

    @Test
    fun `cita de mes futuro ya ejecutada es EJECUTADA, no PROGRAMADA`() {
        // Regresión: antes el chequeo de mes futuro corría antes que `cumple`, así que
        // una cita adelantada (registrada antes de que tocara) se seguía mostrando como
        // "Programada / Sin registrar" en Cronograma y Pendientes.
        assertEquals(EstadoCita.EJECUTADA, estadoDeCita(cita(2026, 10, cumple = true), hoy))
    }

    @Test
    fun `cita del mes actual sin ejecutar es PENDIENTE`() {
        assertEquals(EstadoCita.PENDIENTE, estadoDeCita(cita(2026, 9, cumple = false), hoy))
    }

    @Test
    fun `cita del mes actual ejecutada es EJECUTADA`() {
        assertEquals(EstadoCita.EJECUTADA, estadoDeCita(cita(2026, 9, cumple = true), hoy))
    }

    @Test
    fun `cita de mes pasado sin ejecutar es VENCIDA`() {
        assertEquals(EstadoCita.VENCIDA, estadoDeCita(cita(2026, 8, cumple = false), hoy))
    }

    @Test
    fun `cita de mes pasado ejecutada es EJECUTADA`() {
        assertEquals(EstadoCita.EJECUTADA, estadoDeCita(cita(2026, 8, cumple = true), hoy))
    }
}
