package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DisciplinaCacheEntity
import com.google.gson.annotations.SerializedName

/** Espejo de DisciplinaResponse del backend (GET /substation/disciplinas). */
data class DisciplinaDto(
    @SerializedName("codigo") val codigo: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("sigla") val sigla: String?,
    @SerializedName("orden") val orden: Int?,
    @SerializedName("capturaMovil") val capturaMovil: Boolean?,
    @SerializedName("conjuntos") val conjuntos: String?,
    @SerializedName("usaElementos") val usaElementos: Boolean?,
    @SerializedName("usaEquipos") val usaEquipos: Boolean?,
    @SerializedName("usaControles") val usaControles: Boolean?
)

/** null (con aviso en el log) si le falta el código o el nombre: no se inventa una disciplina. */
fun DisciplinaDto.toEntity(): DisciplinaCacheEntity? {
    if (codigo.isNullOrBlank() || nombre.isNullOrBlank()) {
        Log.w("DisciplinaDto", "Disciplina sin código o nombre descartada: $this")
        return null
    }
    return DisciplinaCacheEntity(
        codigo = codigo,
        nombre = nombre,
        sigla = sigla?.takeIf { it.isNotBlank() } ?: codigo.take(1),
        orden = orden ?: Int.MAX_VALUE,
        capturaMovil = capturaMovil == true,
        conjuntos = conjuntos ?: "NO",
        usaElementos = usaElementos == true,
        usaEquipos = usaEquipos == true,
        usaControles = usaControles == true
    )
}
