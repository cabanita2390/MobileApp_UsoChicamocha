package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto

@Entity(tableName = "motos")
data class MotoEntity(
    @PrimaryKey val id: Int,
    val placa: String,
    @androidx.room.ColumnInfo(defaultValue = "0") val idUbicacionBase: Int = 0,
    @androidx.room.ColumnInfo(defaultValue = "") val ubicacionBase: String = ""
)

fun MotoEntity.toDomain(): Moto = Moto(id = id, placa = placa, idUbicacionBase = idUbicacionBase, ubicacionBase = ubicacionBase)
fun Moto.toEntity(): MotoEntity = MotoEntity(id = id, placa = placa, idUbicacionBase = idUbicacionBase, ubicacionBase = ubicacionBase)
