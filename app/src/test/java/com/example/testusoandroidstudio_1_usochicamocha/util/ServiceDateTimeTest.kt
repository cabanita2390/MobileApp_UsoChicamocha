package com.example.testusoandroidstudio_1_usochicamocha.util

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toOilChangeRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone

class ServiceDateTimeTest {

    private val bogota = TimeZone.getTimeZone("America/Bogota")

    private fun bogotaMillis(y: Int, m: Int, d: Int, h: Int, min: Int) =
        LocalDateTime.of(y, m, d, h, min).atZone(ZoneId.of("America/Bogota")).toInstant().toEpochMilli()

    private fun utcMidnight(y: Int, m: Int, d: Int) =
        LocalDateTime.of(y, m, d, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test
    fun `envia la hora local sin zona, no UTC`() {
        // 08/10 a las 20:30 en Colombia = 09/10 01:30 UTC — antes se mandaba eso y quedaba el día 9
        assertEquals("2026-10-08T20:30:00", ServiceDateTime.toBackendLocal(bogotaMillis(2026, 10, 8, 20, 30), bogota))
    }

    @Test
    fun `el request de maquinaria usa la hora local`() {
        val default = TimeZone.getDefault()
        TimeZone.setDefault(bogota)
        try {
            val form = MachineOilChangeForm(
                machineId = 1, dateTime = bogotaMillis(2026, 10, 5, 10, 30), brand = "Shell", brandId = 2,
                quantity = 20.0, currentHourMeter = 1400, averageHoursChange = 250, type = "motor"
            )
            assertEquals("2026-10-05T10:30:00", form.toOilChangeRequest().dateTime)
        } finally {
            TimeZone.setDefault(default)
        }
    }

    @Test
    fun `de noche no deja elegir mañana`() {
        val now = bogotaMillis(2026, 10, 13, 20, 0) // ya es 14/10 en UTC
        assertTrue(ServiceDateTime.isSelectableServiceDay(utcMidnight(2026, 10, 13), now, bogota))
        assertTrue(ServiceDateTime.isSelectableServiceDay(utcMidnight(2026, 10, 1), now, bogota))
        assertFalse(ServiceDateTime.isSelectableServiceDay(utcMidnight(2026, 10, 14), now, bogota))
    }

    @Test
    fun `de mañana hoy sigue siendo seleccionable`() {
        val now = bogotaMillis(2026, 10, 13, 0, 10)
        assertTrue(ServiceDateTime.isSelectableServiceDay(utcMidnight(2026, 10, 13), now, bogota))
        assertFalse(ServiceDateTime.isSelectableServiceDay(utcMidnight(2026, 10, 14), now, bogota))
    }

    @Test
    fun `no deja años futuros`() {
        val now = bogotaMillis(2026, 12, 31, 21, 0)
        assertTrue(ServiceDateTime.isSelectableServiceYear(2026, now, bogota))
        assertFalse(ServiceDateTime.isSelectableServiceYear(2027, now, bogota))
    }
}
