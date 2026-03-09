package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the storage and retrieval of session data (tokens and userId)
 * safely using Jetpack DataStore.
 * Integrated with Hilt to be a Singleton throughout the app.
 */
@Singleton
class TokenManager @Inject constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        // --- SESSION DATA KEYS ---
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = intPreferencesKey("user_id")
        val USERNAME_KEY = stringPreferencesKey("username")
    }

    // --- SAVE METHODS ---

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveUserId(id: Int) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id
        }
    }

    suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }

    // --- READ METHODS ---

    fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    fun getUserId(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    fun getUsername(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }
    }

    // --- DELETE METHOD ---

    suspend fun clearSessionData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
