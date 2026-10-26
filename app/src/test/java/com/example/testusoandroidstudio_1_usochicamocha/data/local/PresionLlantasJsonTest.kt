package com.example.testusoandroidstudio_1_usochicamocha.data.local

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.LecturaPresionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PosicionLlanta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PresionLlantasJsonTest {

    @Test
    fun `lecturas ida y vuelta`() {
        val lecturas = listOf(LecturaPresionLlanta("DEL", 22.0, "Delantera", 28.0, 33.0))
        val json = PresionLlantasJson.lecturasToJson(lecturas)
        assertEquals(lecturas, PresionLlantasJson.lecturasFromJson(json))
    }

    @Test
    fun `sin lecturas se guarda vacio y no se envia el campo`() {
        assertEquals("", PresionLlantasJson.lecturasToJson(emptyList()))
        assertTrue(PresionLlantasJson.lecturasFromJson("").isEmpty())
        assertNull(PresionLlantasJson.lecturasParaEnvio(""))
        assertNull(PresionLlantasJson.lecturasParaEnvio(null))
    }

    @Test
    fun `json corrupto no tumba la sincronizacion`() {
        assertTrue(PresionLlantasJson.lecturasFromJson("{no es json").isEmpty())
        assertTrue(PresionLlantasJson.posicionesFromJson("[{").isEmpty())
    }

    @Test
    fun `lecturasParaEnvio mapea al dto del backend`() {
        val json = PresionLlantasJson.lecturasToJson(listOf(LecturaPresionLlanta("TRA", 31.5, "Trasera", 30.0, 36.0)))
        val dto = PresionLlantasJson.lecturasParaEnvio(json)!!.single()
        assertEquals("TRA", dto.codigoPosicion)
        assertEquals(31.5, dto.psi, 0.0)
        assertEquals(30.0, dto.psiMinReferencia!!, 0.0)
    }

    @Test
    fun `posiciones ida y vuelta`() {
        val posiciones = listOf(PosicionLlanta("DEL", "Delantera", 1, 28.0, null, 33.0))
        assertEquals(posiciones, PresionLlantasJson.posicionesFromJson(PresionLlantasJson.posicionesToJson(posiciones)))
    }
}
