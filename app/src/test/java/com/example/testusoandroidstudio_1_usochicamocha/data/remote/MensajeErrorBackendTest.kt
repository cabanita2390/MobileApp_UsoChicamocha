package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class MensajeErrorBackendTest {

    private val generico = "Error al editar la ejecución: 400"

    @Test
    fun `texto plano del backend se muestra tal cual`() {
        assertEquals(
            "La edición no modifica ningún campo",
            mensajeErrorBackend("La edición no modifica ningún campo", generico)
        )
    }

    @Test
    fun `json con campo error usa ese mensaje`() {
        assertEquals(
            "motivoEdicion debe tener al menos 15 caracteres.",
            mensajeErrorBackend("""{"error":"motivoEdicion debe tener al menos 15 caracteres.","status":400}""", generico)
        )
    }

    @Test
    fun `sin cuerpo, html o json sin mensaje cae al generico`() {
        assertEquals(generico, mensajeErrorBackend(null, generico))
        assertEquals(generico, mensajeErrorBackend("   ", generico))
        assertEquals(generico, mensajeErrorBackend("<html><body>502 Bad Gateway</body></html>", generico))
        assertEquals(generico, mensajeErrorBackend("""{"status":500}""", generico))
        assertEquals(generico, mensajeErrorBackend("{roto", generico))
    }
}
