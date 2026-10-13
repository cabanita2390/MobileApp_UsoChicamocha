package com.example.testusoandroidstudio_1_usochicamocha.util

import android.util.Log
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JwtUtils {

    /** Milisegundos que le quedan al token antes de vencer (negativo si ya venció o no se puede leer). */
    fun msHastaVencer(token: String): Long = try {
        val partes = token.split(".")
        val payload = JSONObject(String(android.util.Base64.decode(partes[1], android.util.Base64.URL_SAFE), Charsets.UTF_8))
        val exp = payload.optLong("exp")
        if (exp == 0L) -1 else exp * 1000 - System.currentTimeMillis()
    } catch (e: Exception) {
        -1
    }


    fun isTokenExpired(token: String, tokenType: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payloadBytes = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE)
            val payloadJson = String(payloadBytes, Charsets.UTF_8)
            val payload = JSONObject(payloadJson)

            val exp = payload.optLong("exp")
            if (exp == 0L) return true

            val expirationInMillis = exp * 1000
            val currentTimeInMillis = System.currentTimeMillis()

            val isExpired = expirationInMillis < currentTimeInMillis

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expirationDate = sdf.format(Date(expirationInMillis))
            val currentDate = sdf.format(Date(currentTimeInMillis))

            // --- LOGS MEJORADOS ---
            Log.d("JwtUtils", "---------------------------------")
            Log.d("JwtUtils", "Verificando Expiración de: $tokenType")
            Log.d("JwtUtils", "   - Fecha de Expiración: $expirationDate")
            Log.d("JwtUtils", "   - Fecha Actual:        $currentDate")
            Log.d("JwtUtils", "   - ¿Ha expirado?: $isExpired")
            Log.d("JwtUtils", "---------------------------------")

            return isExpired
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }
}