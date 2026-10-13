package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.UserSession

interface AuthRepository {
    suspend fun login(user: String, pass: String): Result<UserSession>
    suspend fun refreshTokenIfNecessary(): Result<String>
    /** Refresca ya si al access token le quedan menos de [margenMs], sin esperar a que venza. */
    suspend fun refrescarSiVencePronto(margenMs: Long): Result<String>
    suspend fun logout()
    }
