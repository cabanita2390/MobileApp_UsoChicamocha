package com.example.testusoandroidstudio_1_usochicamocha.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Campo de "fecha del servicio" reutilizable para cambios de aceite (vehículo, moto,
 * maquinaria) — permite registrar hoy un cambio que en realidad ocurrió antes (p. ej. si
 * no se alcanzó a capturar el mismo día), sin tener que forzar el kilometraje/horómetro
 * actual como si el cambio hubiera sido hoy.
 *
 * Solo elige el día (calendario); conserva la hora del momento en que se abrió el
 * formulario. No permite fechas futuras (no se puede "cambiar el aceite mañana").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDateField(
    label: String,
    dateTimeMillis: Long,
    onDateTimeSelected: (Long) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val displayFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayFormat.format(Date(dateTimeMillis)),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label, fontWeight = FontWeight.Bold) },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (enabled) {
            Spacer(modifier = Modifier.matchParentSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showPicker = true })
        }
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateTimeMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis <= System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val pickedUtcMillis = datePickerState.selectedDateMillis
                    if (pickedUtcMillis != null) {
                        // El DatePicker de Material3 trabaja en UTC (medianoche del día
                        // elegido). Extraemos año/mes/día en UTC y los combinamos con la
                        // hora actual del calendario LOCAL, para no correr el día por el
                        // desfase de huso horario.
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        utcCal.timeInMillis = pickedUtcMillis
                        val localCal = Calendar.getInstance()
                        localCal.timeInMillis = dateTimeMillis
                        localCal.set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                        localCal.set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                        localCal.set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                        onDateTimeSelected(localCal.timeInMillis)
                    }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
