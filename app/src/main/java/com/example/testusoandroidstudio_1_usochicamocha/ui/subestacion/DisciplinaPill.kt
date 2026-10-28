package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion

import androidx.compose.runtime.Composable
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.EstadoPill
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors

/**
 * Píldora con la disciplina de una cita. Con dos disciplinas registrables (Civil y Eléctrico) se
 * muestra en todas las citas para distinguirlas; Eléctrico va en ámbar.
 */
@Composable
fun DisciplinaPill(codigo: String) {
    if (codigo == "ELECTRICO") {
        EstadoPill(nombreDisciplina(codigo), SubestacionColors.AmberBg, SubestacionColors.Amber)
    } else {
        EstadoPill(nombreDisciplina(codigo), SubestacionColors.ChipBg, SubestacionColors.TextTertiary)
    }
}
