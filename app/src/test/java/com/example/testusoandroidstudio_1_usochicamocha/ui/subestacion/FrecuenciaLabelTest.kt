package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion

import org.junit.Assert.assertEquals
import org.junit.Test

class FrecuenciaLabelTest {

    @Test
    fun `las 5 frecuencias del catalogo se muestran en espanol`() {
        assertEquals("Mensual", frecuenciaLabel("MENSUAL"))
        assertEquals("Bimestral", frecuenciaLabel("BIMESTRAL"))
        assertEquals("Trimestral", frecuenciaLabel("TRIMESTRAL"))
        assertEquals("Semestral", frecuenciaLabel("SEMESTRAL"))
        assertEquals("Anual", frecuenciaLabel("ANUAL"))
    }

    @Test
    fun `un valor desconocido se muestra tal cual y null queda vacio`() {
        assertEquals("QUINCENAL", frecuenciaLabel("QUINCENAL"))
        assertEquals("", frecuenciaLabel(null))
    }
}
