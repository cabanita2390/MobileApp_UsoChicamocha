package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Disciplina

/** Disciplinas y sus características (GET /substation/disciplinas), para trabajar sin señal. */
@Entity(tableName = "mant_disciplina_cache")
data class DisciplinaCacheEntity(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val sigla: String,
    val orden: Int,
    val capturaMovil: Boolean,
    val conjuntos: String,
    val usaElementos: Boolean,
    val usaEquipos: Boolean,
    val usaControles: Boolean
)

fun DisciplinaCacheEntity.toDomain() = Disciplina(
    codigo = codigo, nombre = nombre, sigla = sigla, orden = orden, capturaMovil = capturaMovil,
    conjuntos = conjuntos, usaElementos = usaElementos, usaEquipos = usaEquipos, usaControles = usaControles
)
