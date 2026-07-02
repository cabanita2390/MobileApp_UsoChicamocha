package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Diálogos de alerta idénticos (o casi) entre VehiculoScreen.kt y MotocicletaScreen.kt.
 * Los diálogos de "Consciente de responsabilidad" y "Vehículo aprobado" NO tienen
 * equivalente en moto — se quedan definidos solo en VehiculoScreen.kt. */

private val ColorBueno = Color(0xFF4CAF50)
private val ColorMalo  = Color(0xFFD32F2F)
private val ColorRegular = Color(0xFFFFA000)

/** "¡Inspección Guardada!" — texto y comportamiento al confirmar difieren un poco
 * por pantalla (Moto fuerza click en Aceptar y navega distinto; Vehículo permite
 * cerrar tocando afuera), por eso se parametrizan onDismissRequest/onConfirm. */
@Composable
fun InspectionSavedDialog(
    assetLabel: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonModifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ColorBueno,
                modifier = Modifier.size(40.dp)
            )
        },
        title = { Text("¡Inspección Guardada!", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "La inspección del $assetLabel fue registrada correctamente en el sistema.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, modifier = confirmButtonModifier) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Aceptar")
            }
        }
    )
}

/** Kilometraje menor al registrado — bloqueante, idéntico en ambas pantallas. */
@Composable
fun KmBlockingAlertDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = ColorMalo,
                modifier = Modifier.size(40.dp)
            )
        },
        title = { Text("Kilometraje Incorrecto", fontWeight = FontWeight.Bold, color = ColorMalo) },
        text = {
            Text(
                message + "\n\nDebe corregir el valor antes de poder guardar la inspección.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ColorMalo)) {
                Text("Corregir")
            }
        }
    )
}

/** Kilometraje con incremento alto o igual — no bloqueante, idéntico en ambas pantallas
 * salvo qué callback dispara cada una al tocar afuera del diálogo. */
@Composable
fun KmWarningAlertDialog(
    message: String,
    onDismissRequest: () -> Unit,
    onConfirmException: () -> Unit,
    onCorrect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = ColorRegular,
                modifier = Modifier.size(40.dp)
            )
        },
        title = { Text("Verificación de Kilometraje", fontWeight = FontWeight.Bold, color = ColorRegular) },
        text = {
            Text(
                message + "\n\nPor favor, verifica si el número es correcto o corrígelo.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirmException, colors = ButtonDefaults.buttonColors(containerColor = ColorRegular)) {
                Text("Confirmar Excepción")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCorrect) { Text("Corregir") }
        }
    )
}
