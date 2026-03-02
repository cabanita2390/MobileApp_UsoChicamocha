package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import kotlinx.coroutines.flow.Flow

interface MotoRepository {
    suspend fun syncMotos(): Result<Unit>
    fun getLocalMotos(): Flow<List<Moto>>
    suspend fun syncUbicaciones(): Result<Unit>
    fun getLocalUbicaciones(): Flow<List<Ubicacion>>
}
