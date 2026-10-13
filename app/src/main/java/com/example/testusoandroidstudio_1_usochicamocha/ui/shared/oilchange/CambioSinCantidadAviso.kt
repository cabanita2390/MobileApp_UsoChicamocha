package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Aviso para un cambio de aceite pendiente guardado SIN cantidad (versiones anteriores de
 * la app la marcaban "opcional"). El backend lo rechaza siempre, así que no se envía:
 * hay que eliminarlo y registrarlo de nuevo con la cantidad.
 */
@Composable
fun CambioSinCantidadAviso(placa: String, onDelete: () -> Unit) {
    var confirmar by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(
            "No se puede enviar: falta la cantidad de aceite. Elimínelo y regístrelo de nuevo con la cantidad.",
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(onClick = { confirmar = true }, modifier = Modifier.padding(top = 4.dp)) {
            Text("Eliminar", color = MaterialTheme.colorScheme.error)
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Eliminar cambio de aceite") },
            text = { Text("Se eliminará el cambio de aceite pendiente de la placa $placa. Después regístrelo de nuevo con la cantidad.") },
            confirmButton = {
                TextButton(onClick = { confirmar = false; onDelete() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}
