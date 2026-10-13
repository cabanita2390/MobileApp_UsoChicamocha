package com.example.testusoandroidstudio_1_usochicamocha.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RedactorLogTest {

    @Test
    fun `oculta la contraseña del login`() {
        assertEquals(
            """{"password":"***","username":"qa_operario"}""",
            redactarSecretos("""{"password":"S3creta!","username":"qa_operario"}""")
        )
    }

    @Test
    fun `oculta los tokens de la respuesta del login y del refresh`() {
        val respuesta = """{"id":2,"jwt":"eyJhbGciOi.abc.def","refreshToken": "eyJr.x.y","accessToken":"eyJa.b.c"}"""
        val limpio = redactarSecretos(respuesta)
        assertFalse(limpio.contains("eyJ"))
        assertEquals("""{"id":2,"jwt":"***","refreshToken": "***","accessToken":"***"}""", limpio)
    }

    @Test
    fun `oculta un Bearer si aparece en una linea de log`() {
        assertEquals("Authorization: Bearer ***", redactarSecretos("Authorization: Bearer eyJhbGc.iOi-J_x.y"))
    }

    @Test
    fun `no toca el resto del mensaje`() {
        val normal = """{"observaciones":"Se inspecciona y no se evidencian novedades","estacionId":13}"""
        assertEquals(normal, redactarSecretos(normal))
    }
}
