package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Auditoría QA 2026-10-13: /uploads era público; ahora las imágenes llevan ?k=. */
class ArchivosInterceptorTest {

    @Test
    fun `solo las URL de uploads llevan la credencial`() {
        assertTrue(ArchivosInterceptor.esDeArchivos("http://10.0.2.2:8080/uploads/subestaciones/ejecuciones/1/a.jpg".toHttpUrl()))
        assertFalse(ArchivosInterceptor.esDeArchivos("http://10.0.2.2:8080/api/v1/substation/estaciones".toHttpUrl()))
    }

    @Test
    fun `agrega k y reemplaza uno viejo sin tocar el resto de la query`() {
        val url = "http://h/uploads/a.jpg?v=2&k=VIEJO".toHttpUrl()
        val conK = ArchivosInterceptor.conCredencial(url, "K1")
        assertEquals("K1", conK.queryParameter("k"))
        assertEquals("2", conK.queryParameter("v"))
        assertEquals(1, conK.queryParameterValues("k").size)
    }
}
