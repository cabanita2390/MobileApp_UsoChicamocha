package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ItemIntervenido

/**
 * Conjuntos y elementos de «¿Qué se intervino?» (Eléctrico), cacheados para registrar sin señal.
 * Una sola tabla para los dos catálogos: [tipo] es CONJUNTO o ELEMENTO (los ids se repiten entre ellos).
 */
@Entity(tableName = "mant_intervenido_cache", primaryKeys = ["tipo", "id"])
data class ItemIntervenidoCacheEntity(
    val tipo: String,
    val id: Long,
    val nombre: String,
    val disciplina: String,
    /** Solo conjuntos: código del ícono elegido en la web; null = genérico. */
    val icono: String? = null
)

fun ItemIntervenidoCacheEntity.toDomain() =
    ItemIntervenido(id = id, nombre = nombre, disciplina = disciplina, tipo = tipo, icono = icono)
