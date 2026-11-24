package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.llantas

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ConfigLlantas
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EvaluadorPresionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.LecturaPresionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PosicionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ResultadoPresionLlanta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Test
import java.time.LocalDate

class PresionLlantasStateTest {

    private val del = PosicionLlanta("DEL", "Delantera", 1, 28.0, 30.0, 33.0)
    private val tra = PosicionLlanta("TRA", "Trasera", 2, 30.0, null, 36.0)
    private val perfil = ConfigLlantas("Moto 2 llantas", listOf(del, tra))
    private val conPerfil = PresionLlantasState(config = perfil, tocaMedir = true)

    private val lunes = LocalDate.of(2026, 11, 23)

    @After
    fun relojReal() {
        DiaMedicionLlantas.hoy = { LocalDate.now() }
    }

    @Test
    fun `evaluar respeta los bordes del rango igual que el backend`() {
        assertEquals(ResultadoPresionLlanta.BAJA, EvaluadorPresionLlanta.evaluar(27.9, del))
        assertEquals(ResultadoPresionLlanta.OK, EvaluadorPresionLlanta.evaluar(28.0, del))
        assertEquals(ResultadoPresionLlanta.OK, EvaluadorPresionLlanta.evaluar(33.0, del))
        assertEquals(ResultadoPresionLlanta.ALTA, EvaluadorPresionLlanta.evaluar(33.1, del))
    }

    @Test
    fun `parsear acepta coma decimal y rechaza valores imposibles`() {
        assertEquals(28.5, EvaluadorPresionLlanta.parsear("28,5")!!, 0.0)
        assertEquals(30.0, EvaluadorPresionLlanta.parsear(" 30 ")!!, 0.0)
        assertNull(EvaluadorPresionLlanta.parsear(""))
        assertNull(EvaluadorPresionLlanta.parsear("0"))
        assertNull(EvaluadorPresionLlanta.parsear("301"))
        // Un decimal, como el backend: 31,95 es 32,0 (OK con mínimo 32) en el teléfono y en el servidor.
        assertEquals(32.0, EvaluadorPresionLlanta.parsear("31,95")!!, 0.0)
        assertEquals(300.0, EvaluadorPresionLlanta.parsear("300.04")!!, 0.0)
        assertNull(EvaluadorPresionLlanta.parsear("0,04"))
        assertNull(EvaluadorPresionLlanta.parsear("abc"))
    }

    @Test
    fun `sin perfil la seccion no aplica y no frena el guardado`() {
        val sinPerfil = PresionLlantasState()
        assertFalse(sinPerfil.aplica)
        assertFalse(sinPerfil.seMuestra)
        assertFalse(sinPerfil.requiereAviso)
        assertTrue(sinPerfil.lecturas().isEmpty())
    }

    @Test
    fun `se pide solo los lunes`() {
        DiaMedicionLlantas.hoy = { lunes }
        assertTrue(DiaMedicionLlantas.tocaMedir())
        assertTrue(PresionLlantasState().paraActivo(perfil).seMuestra)
        for (dias in 1L..6L) {
            DiaMedicionLlantas.hoy = { lunes.plusDays(dias) }
            val s = PresionLlantasState().paraActivo(perfil)
            assertTrue("el activo sigue teniendo perfil", s.aplica)
            assertFalse("${lunes.plusDays(dias).dayOfWeek} no se pide", s.seMuestra)
        }
        DiaMedicionLlantas.hoy = { lunes.plusWeeks(1) }
        assertTrue(PresionLlantasState().paraActivo(perfil).seMuestra)
    }

    @Test
    fun `nunca bloquea: con llantas sin medir envia solo las medidas`() {
        assertTrue(conPerfil.lecturas().isEmpty())
        assertFalse(conPerfil.requiereAviso)
        val una = conPerfil.conValor("DEL", "30").conValor("TRA", "")
        assertEquals(listOf("DEL"), una.lecturas().map { it.codigoPosicion })
        assertFalse(una.requiereAviso)
    }

    @Test
    fun `presion baja pide aviso una vez y no bloquea`() {
        val s = conPerfil.conValor("DEL", "22").conValor("TRA", "40")
        assertEquals(listOf(ResultadoPresionLlanta.BAJA, ResultadoPresionLlanta.ALTA), s.fueraDeRango().map { it.resultado })
        assertTrue(s.requiereAviso)
        assertFalse(s.copy(avisoConfirmado = true).requiereAviso)
        // Si corrige un valor después de confirmar, se vuelve a avisar.
        assertTrue(s.copy(avisoConfirmado = true).conValor("DEL", "21").requiereAviso)
    }

    @Test
    fun `lecturas llevan etiqueta y rango de referencia`() {
        val lecturas = conPerfil.conValor("DEL", "22").conValor("TRA", "31,5").lecturas()
        assertEquals(
            listOf(
                LecturaPresionLlanta("DEL", 22.0, "Delantera", 28.0, 33.0),
                LecturaPresionLlanta("TRA", 31.5, "Trasera", 30.0, 36.0)
            ),
            lecturas
        )
    }

    @Test
    fun `conValor descarta caracteres que no son numero`() {
        assertEquals("28.5", conPerfil.conValor("DEL", "28.5 psi-").valores["DEL"])
    }

    @Test
    fun `paraActivo descarta lo digitado del activo anterior`() {
        val s = conPerfil.conValor("DEL", "22").paraActivo(null)
        assertFalse(s.aplica)
        assertTrue(s.valores.isEmpty())
    }
}
