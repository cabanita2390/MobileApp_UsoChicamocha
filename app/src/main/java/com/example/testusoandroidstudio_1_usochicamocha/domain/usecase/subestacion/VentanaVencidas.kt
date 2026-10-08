package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import java.time.LocalDate

/**
 * Cambio de año: en enero y febrero las citas NO ejecutadas del año anterior siguen saliendo
 * como "Vencidas", para que el técnico pueda cerrarlas tarde contra su cita (si no, una cita de
 * diciembre hecha en enero quedaba como imprevisto y la de diciembre "No ejecutada" para siempre).
 * Decisión de negocio (David, 2026-10-08).
 */
object VentanaVencidas {
    const val MESES_DE_GRACIA = 2

    fun incluyeAnioAnterior(hoy: LocalDate): Boolean = hoy.monthValue <= MESES_DE_GRACIA
}
