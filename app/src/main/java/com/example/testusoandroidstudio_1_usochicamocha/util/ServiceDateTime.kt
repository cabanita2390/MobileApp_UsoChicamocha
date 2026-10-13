package com.example.testusoandroidstudio_1_usochicamocha.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Fecha/hora de un servicio (cambio de aceite) tal como la espera el backend: un
 * LocalDateTime en hora LOCAL, sin zona ("2026-10-05T10:30:00").
 *
 * No enviar UTC con 'Z': el backend lo lee como LocalDateTime y guarda la hora UTC
 * como si fuera la de Colombia (+5h); un cambio registrado después de las 19:00
 * quedaba con el día siguiente en el Consolidado.
 */
object ServiceDateTime {

    fun toBackendLocal(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(Date(millis))
    }

    /**
     * Para el DatePicker de Material3, que entrega cada día como la medianoche UTC de
     * ese día: es seleccionable si ese día (año/mes/día) no es posterior al día de hoy
     * en hora local. Comparar contra System.currentTimeMillis() a secas dejaba elegir
     * "mañana" desde las 19:00 en Colombia (a esa hora ya es mañana en UTC).
     */
    fun isSelectableServiceDay(
        utcDayMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Boolean {
        val picked = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcDayMillis }
        val today = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
        val pickedKey = picked.get(Calendar.YEAR) * 10000 + picked.get(Calendar.MONTH) * 100 + picked.get(Calendar.DAY_OF_MONTH)
        val todayKey = today.get(Calendar.YEAR) * 10000 + today.get(Calendar.MONTH) * 100 + today.get(Calendar.DAY_OF_MONTH)
        return pickedKey <= todayKey
    }

    /** El DatePicker también filtra años; el año actual local es el último permitido. */
    fun isSelectableServiceYear(
        year: Int,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Boolean = year <= Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }.get(Calendar.YEAR)
}
