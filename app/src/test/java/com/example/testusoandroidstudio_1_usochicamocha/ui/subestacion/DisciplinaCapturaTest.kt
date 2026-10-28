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

/** El móvil muestra las citas de todas las disciplinas, y registra las de Civil y Eléctrico. */
class DisciplinaCapturaTest {

    private fun cita(disciplina: String) = CitaProgramada(
        programacionId = 1, anio = 2026, mes = 10, estacionId = 1, estacionNombre = "Ayalas",
        estacionTipo = "BOMBEO", actividadId = 1, actividadNombre = "Inspección eléctrica",
        ejecutado = 0, cumple = false, disciplina = disciplina
    )

    @Test
    fun civilYElectricoSeRegistran_electromecanicoEsSoloConsulta() {
        assertTrue(cita("CIVIL").capturaHabilitada)
        assertTrue(cita("ELECTRICO").capturaHabilitada)
        assertFalse(cita("ELECTROMECANICO").capturaHabilitada)
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
        assertFalse(dto.toEntity().toDomain().capturaHabilitada)
    }

    @Test
    fun etiquetasYMensajeEnEspanol() {
        assertEquals("Civil", nombreDisciplina("CIVIL"))
        assertEquals("Eléctrico", nombreDisciplina("ELECTRICO"))
        assertEquals("Electromecánico", nombreDisciplina("ELECTROMECANICO"))
        assertTrue(mensajeCapturaEnDesarrollo("ELECTROMECANICO").startsWith("La captura de Electromecánico aún está en desarrollo"))
    }
}
