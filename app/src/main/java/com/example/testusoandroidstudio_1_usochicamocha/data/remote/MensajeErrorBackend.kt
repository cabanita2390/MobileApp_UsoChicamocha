package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Mensaje legible de una respuesta de error del backend. El backend responde en dos
 * formatos según la excepción: JSON `{"error": "...", "status": 400}` o el texto plano
 * del motivo (p. ej. "La edición no modifica ningún campo"). Si el cuerpo no trae nada
 * usable (vacío, HTML de un proxy, demasiado largo), se usa [generico].
 */
fun mensajeErrorBackend(cuerpo: String?, generico: String): String {
    val texto = cuerpo?.trim().orEmpty()
    if (texto.isEmpty()) return generico
    if (texto.startsWith("{")) {
        return runCatching {
            val json = Gson().fromJson(texto, JsonObject::class.java)
            listOf("error", "message", "mensaje")
                .firstNotNullOfOrNull { campo ->
                    json.get(campo)?.takeIf { it.isJsonPrimitive() }?.getAsString()?.trim()?.takeIf { it.isNotEmpty() }
                }
        }.getOrNull() ?: generico
    }
    if (texto.startsWith("<") || texto.length > 300) return generico
    return texto
}
