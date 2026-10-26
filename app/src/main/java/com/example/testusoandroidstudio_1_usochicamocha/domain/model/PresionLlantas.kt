package com.example.testusoandroidstudio_1_usochicamocha.domain.model

/** Una llanta del perfil del activo con su rango en PSI (viene de `GET v1/tire/asignaciones`). */
data class PosicionLlanta(
    val codigo: String,
    val etiqueta: String,
    val orden: Int,
    val psiMin: Double,
    val psiObjetivo: Double?,
    val psiMax: Double
)

/** Perfil de llantas vigente de un vehículo, moto o máquina (caché local). */
data class ConfigLlantas(
    val perfilNombre: String,
    val posiciones: List<PosicionLlanta>
)

/**
 * Presión medida en una llanta, tal como se guarda en la cola y se envía al backend.
 * La etiqueta y el rango de referencia viajan por si el perfil cambió en el servidor mientras
 * la inspección estaba pendiente: el backend evalúa contra el perfil vigente y solo usa estos
 * valores si la posición ya no existe.
 */
data class LecturaPresionLlanta(
    val codigoPosicion: String,
    val psi: Double,
    val etiquetaPosicion: String? = null,
    val psiMinReferencia: Double? = null,
    val psiMaxReferencia: Double? = null
)

enum class ResultadoPresionLlanta { OK, BAJA, ALTA }

/**
 * Mismas reglas que EvaluadorPresionLlanta del backend: por debajo del mínimo es BAJA, por
 * encima del máximo ALTA; los extremos del rango son OK. Solo informa: nunca bloquea la inspección.
 */
object EvaluadorPresionLlanta {

    /** Lecturas por fuera de esto son errores de digitación (el backend las descarta). */
    const val PSI_MAXIMO = 300.0

    fun evaluar(psi: Double, posicion: PosicionLlanta): ResultadoPresionLlanta = when {
        psi < posicion.psiMin -> ResultadoPresionLlanta.BAJA
        psi > posicion.psiMax -> ResultadoPresionLlanta.ALTA
        else -> ResultadoPresionLlanta.OK
    }

    /** Convierte lo digitado ("28", "28,5") a PSI; null si está vacío o no es una presión válida. */
    fun parsear(texto: String): Double? {
        val valor = texto.trim().replace(',', '.').toDoubleOrNull() ?: return null
        return if (valor > 0 && valor <= PSI_MAXIMO) valor else null
    }
}
