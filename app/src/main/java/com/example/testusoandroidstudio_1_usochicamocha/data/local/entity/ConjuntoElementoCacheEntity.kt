package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity

/**
 * Lo que tiene adentro cada conjunto de «¿Qué se intervino?» (backend V54), cacheado para registrar
 * sin señal: el conjunto es la caja y estos son su contenido. El mismo elemento puede estar en varios
 * conjuntos ("Fusibles" en el Transformador y en la Subestación).
 */
@Entity(tableName = "mant_conjunto_elemento_cache", primaryKeys = ["conjuntoId", "elementoId"])
data class ConjuntoElementoCacheEntity(
    val conjuntoId: Long,
    val elementoId: Long,
    val nombre: String,
    /** Al marcarlo el operario escribe cómo se encuentra (ej. "Estado físico"). */
    val pideDetalle: Boolean,
    val orden: Int
)
