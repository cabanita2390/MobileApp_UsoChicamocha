package com.example.testusoandroidstudio_1_usochicamocha.data.local

import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.LecturaPresionLlantaDto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.LecturaPresionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PosicionLlanta
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * (De)serialización de las listas de llantas que Room guarda como texto: las posiciones del perfil
 * en la caché y las lecturas en las colas de inspecciones pendientes ("" = sin lecturas).
 */
object PresionLlantasJson {
    private val gson = Gson()
    private val tipoLecturas = object : TypeToken<List<LecturaPresionLlanta>>() {}.type
    private val tipoPosiciones = object : TypeToken<List<PosicionLlanta>>() {}.type

    fun lecturasToJson(lecturas: List<LecturaPresionLlanta>): String =
        if (lecturas.isEmpty()) "" else gson.toJson(lecturas, tipoLecturas)

    fun lecturasFromJson(json: String?): List<LecturaPresionLlanta> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson<List<LecturaPresionLlanta>>(json, tipoLecturas).orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun posicionesToJson(posiciones: List<PosicionLlanta>): String = gson.toJson(posiciones, tipoPosiciones)

    fun posicionesFromJson(json: String?): List<PosicionLlanta> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson<List<PosicionLlanta>>(json, tipoPosiciones).orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Lecturas guardadas en la cola → campo `presionesLlantas` del request (null si no hay). */
    fun lecturasParaEnvio(json: String?): List<LecturaPresionLlantaDto>? =
        lecturasFromJson(json).map {
            LecturaPresionLlantaDto(it.codigoPosicion, it.psi, it.etiquetaPosicion, it.psiMinReferencia, it.psiMaxReferencia)
        }.ifEmpty { null }
}
