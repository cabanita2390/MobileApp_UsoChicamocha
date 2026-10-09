package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.CumplimientoDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** El móvil muestra las citas de todas las disciplinas, pero solo registra las de Civil por ahora. */
class DisciplinaCapturaTest {

    private fun cita(disciplina: String) = CitaProgramada(
        programacionId = 1, anio = 2026, mes = 10, estacionId = 1, estacionNombre = "Ayalas",
        estacionTipo = "BOMBEO", actividadId = 1, actividadNombre = "Inspección eléctrica",
        ejecutado = 0, cumple = false, disciplina = disciplina
    )

    @Test
    fun soloCivilSeRegistra_lasDemasSonSoloConsulta() {
        assertTrue(cita("CIVIL").capturaHabilitada)
        assertFalse(cita("ELECTRICO").capturaHabilitada)
        assertFalse(cita("ELECTROMECANICO").capturaHabilitada)
    }

    @Test
    fun laDisciplinaLlegaDelServidorYSobreviveAlCacheOffline() {
        val dto = CumplimientoDto(
            programacionId = 7, anio = 2026, mes = 10, estacionId = 1, estacionNombre = "Ayalas",
            estacionTipo = "BOMBEO", actividadId = 3, actividadNombre = "Inspección eléctrica",
            disciplina = "ELECTRICO", ejecutado = 0, cumple = false
        )
        assertEquals("ELECTRICO", dto.toDomain().disciplina)
        assertEquals("ELECTRICO", dto.toEntity().toDomain().disciplina)
        assertFalse(dto.toEntity().toDomain().capturaHabilitada)
    }

    @Test
    fun etiquetasYMensajeEnEspanol() {
        assertEquals("Civil", nombreDisciplina("CIVIL"))
        assertEquals("Eléctrico", nombreDisciplina("ELECTRICO"))
        assertEquals("Electromecánico", nombreDisciplina("ELECTROMECANICO"))
        assertTrue(mensajeCapturaEnDesarrollo("ELECTRICO").startsWith("La captura de Eléctrico aún está en desarrollo"))
    }
}
