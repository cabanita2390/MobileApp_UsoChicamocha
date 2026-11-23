package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.CumplimientoDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.ActividadDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.DisciplinaDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Disciplina
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.DisciplinasVigentes
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.capturaMovil
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.usaIntervenido
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Lo que hace cada disciplina lo dice el backend (GET /substation/disciplinas): la app no tiene
 * nombres ni reglas de disciplina escritos en su código.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DisciplinaCapturaTest {

    private fun disciplina(codigo: String, nombre: String, orden: Int, captura: Boolean, conjuntos: String, elementos: Boolean) =
        Disciplina(codigo, nombre, codigo.take(1), orden, captura, conjuntos, elementos, usaEquipos = false, usaControles = false)

    private val delBackend = listOf(
        disciplina("ELECTROMECANICO", "Electromecánico", 3, false, Disciplina.CONJUNTOS_OBLIGATORIO, true),
        disciplina("CIVIL", "Civil", 1, true, Disciplina.CONJUNTOS_NO, false),
        disciplina("ELECTRICO", "Eléctrico", 2, true, Disciplina.CONJUNTOS_OPCIONAL, true)
    )

    @Before
    fun cargar() = DisciplinasVigentes.actualizar(delBackend)

    @After
    fun limpiar() = DisciplinasVigentes.actualizar(emptyList())

    @Test
    fun seRegistraLoQueElBackendHabilita_yLoDemasEsSoloConsulta() {
        val d = DisciplinasVigentes.actual
        assertTrue(d.capturaMovil("CIVIL"))
        assertTrue(d.capturaMovil("ELECTRICO"))
        assertFalse(d.capturaMovil("ELECTROMECANICO"))
        assertFalse("Una disciplina desconocida no se registra", d.capturaMovil("NUEVA"))
    }

    @Test
    fun queSeIntervinoAplicaALasQueUsanConjuntosOElementos() {
        val d = DisciplinasVigentes.actual
        assertFalse(d.usaIntervenido("CIVIL"))
        assertTrue(d.usaIntervenido("ELECTRICO"))
        assertTrue(d.usaIntervenido("ELECTROMECANICO"))
    }

    @Test
    fun quedanEnElOrdenDelBackend() {
        assertEquals(listOf("CIVIL", "ELECTRICO", "ELECTROMECANICO"), DisciplinasVigentes.actual.map { it.codigo })
    }

    @Test
    fun laDisciplinaLlegaDelServidorYSobreviveAlCacheOffline() {
        val dto = CumplimientoDto(
            programacionId = 7, anio = 2026, mes = 10, estacionId = 1, estacionNombre = "Ayalas",
            estacionTipo = "BOMBEO", actividadId = 3, actividadNombre = "Inspección eléctrica",
            disciplina = "ELECTROMECANICO", ejecutado = 0, cumple = false
        )
        assertEquals("ELECTROMECANICO", dto.toDomain().disciplina)
        assertEquals("ELECTROMECANICO", dto.toEntity().toDomain().disciplina)
    }

    @Test
    fun laDisciplinaDelBackendSeGuardaYUnaIncompletaSeDescarta() {
        val dto = DisciplinaDto("ELECTRICO", "Eléctrico", "E", 2, true, "OPCIONAL", true, false, false)
        assertEquals(delBackend.first { it.codigo == "ELECTRICO" }, dto.toEntity()!!.toDomain())
        assertNull(DisciplinaDto(null, "Sin código", null, null, null, null, null, null, null).toEntity())
    }

    @Test
    fun unaActividadSinDisciplinaSeDescartaEnVezDeSuponerCivil() {
        assertNull(ActividadDto(id = 5, nombre = "Pintura", disciplina = null).toEntity())
        assertEquals("ELECTRICO", ActividadDto(id = 6, nombre = "Inspección", disciplina = "ELECTRICO").toEntity()!!.disciplina)
    }

    @Test
    fun etiquetasYMensajeEnEspanol() {
        assertEquals("Civil", nombreDisciplina("CIVIL"))
        assertEquals("Eléctrico", nombreDisciplina("ELECTRICO"))
        assertEquals("Electromecánico", nombreDisciplina("ELECTROMECANICO"))
        assertEquals("NUEVA", nombreDisciplina("NUEVA"))
        assertTrue(mensajeCapturaEnDesarrollo("ELECTROMECANICO").startsWith("La captura de Electromecánico aún está en desarrollo"))
    }
}
