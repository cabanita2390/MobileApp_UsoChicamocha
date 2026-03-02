package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion

@Entity(tableName = "ubicaciones")
data class UbicacionEntity(
    @PrimaryKey val id: Int,
    val nombreUbicacion: String
)

fun UbicacionEntity.toDomain(): Ubicacion = Ubicacion(id = id, nombreUbicacion = nombreUbicacion)
fun Ubicacion.toEntity(): UbicacionEntity = UbicacionEntity(id = id, nombreUbicacion = nombreUbicacion)
