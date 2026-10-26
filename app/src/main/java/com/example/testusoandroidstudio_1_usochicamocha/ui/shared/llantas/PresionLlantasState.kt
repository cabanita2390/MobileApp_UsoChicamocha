package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.llantas

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ConfigLlantas
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EvaluadorPresionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.LecturaPresionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PosicionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ResultadoPresionLlanta

/** Una llanta fuera de rango, para el aviso de "calibrar después". */
data class LlantaFueraDeRango(
    val posicion: PosicionLlanta,
    val psi: Double,
    val resultado: ResultadoPresionLlanta
)

/**
 * Estado de la sección "Presión de llantas", compartido por los formularios de vehículo, moto y
 * maquinaria. Si el activo no tiene perfil asignado ([config] null) la sección no se muestra y no
 * cuenta para habilitar el guardado.
 */
data class PresionLlantasState(
    val config: ConfigLlantas? = null,
    /** Texto digitado por código de posición. */
    val valores: Map<String, String> = emptyMap(),
    /** Aviso previo al guardado; el operario puede guardar igual (no bloquea). */
    val mostrarAvisoCalibrar: Boolean = false,
    /** El operario ya vio el aviso y decidió guardar. */
    val avisoConfirmado: Boolean = false
) {
    val aplica: Boolean get() = config != null && config.posiciones.isNotEmpty()

    /** Todas las llantas del perfil tienen una presión válida (obligatorio cuando aplica). */
    val completa: Boolean
        get() = !aplica || config!!.posiciones.all { EvaluadorPresionLlanta.parsear(valores[it.codigo].orEmpty()) != null }

    fun resultado(posicion: PosicionLlanta): ResultadoPresionLlanta? =
        EvaluadorPresionLlanta.parsear(valores[posicion.codigo].orEmpty())?.let { EvaluadorPresionLlanta.evaluar(it, posicion) }

    fun fueraDeRango(): List<LlantaFueraDeRango> {
        if (!aplica) return emptyList()
        return config!!.posiciones.mapNotNull { p ->
            val psi = EvaluadorPresionLlanta.parsear(valores[p.codigo].orEmpty()) ?: return@mapNotNull null
            val r = EvaluadorPresionLlanta.evaluar(psi, p)
            if (r == ResultadoPresionLlanta.OK) null else LlantaFueraDeRango(p, psi, r)
        }
    }

    /** True si hay que mostrar el aviso antes de guardar. */
    val requiereAviso: Boolean get() = !avisoConfirmado && fueraDeRango().isNotEmpty()

    fun lecturas(): List<LecturaPresionLlanta> {
        if (!aplica) return emptyList()
        return config!!.posiciones.mapNotNull { p ->
            val psi = EvaluadorPresionLlanta.parsear(valores[p.codigo].orEmpty()) ?: return@mapNotNull null
            LecturaPresionLlanta(p.codigo, psi, p.etiqueta, p.psiMin, p.psiMax)
        }
    }

    fun conValor(codigo: String, texto: String): PresionLlantasState {
        // Solo dígitos y un separador decimal: el teclado numérico a veces deja pasar otros signos.
        val limpio = texto.filter { it.isDigit() || it == '.' || it == ',' }.take(6)
        return copy(valores = valores + (codigo to limpio), avisoConfirmado = false)
    }

    /** Al cambiar de activo se descartan las presiones digitadas del anterior. */
    fun paraActivo(config: ConfigLlantas?): PresionLlantasState = PresionLlantasState(config = config)
}
