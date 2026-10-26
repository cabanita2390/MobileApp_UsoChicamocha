package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.llantas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PosicionLlanta
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ResultadoPresionLlanta

private val ColorBueno = Color(0xFF4CAF50)
private val ColorMalo = Color(0xFFD32F2F)
private val ColorRegular = Color(0xFFFFA000)

private fun Double.psi(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

private fun PosicionLlanta.rango(): String {
    val objetivo = psiObjetivo?.let { " · recomendado ${it.psi()}" }.orEmpty()
    return "Rango ${psiMin.psi()} – ${psiMax.psi()} PSI$objetivo"
}

/**
 * Sección "Presión de llantas (PSI)": un campo por cada llanta del perfil del activo. Muestra al
 * momento si la presión está baja o alta, pero nunca bloquea: calibrar es tarea del operario.
 * No dibuja nada si el activo no tiene perfil asignado. Con [mostrarTitulo] = false va dentro de
 * una SectionCard que ya pone el título.
 */
@Composable
fun PresionLlantasSection(
    state: PresionLlantasState,
    onValorChange: (codigo: String, texto: String) -> Unit,
    modifier: Modifier = Modifier,
    mostrarTitulo: Boolean = true
) {
    val config = state.config ?: return
    if (config.posiciones.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (mostrarTitulo) {
            Text(
                "Presión de Llantas (PSI) (*)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp)
            )
        }
        Text(
            "Perfil: ${config.perfilNombre}. Mida cada llanta con el calibrador.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        config.posiciones.forEach { posicion ->
            val resultado = state.resultado(posicion)
            val (mensaje, color) = when (resultado) {
                ResultadoPresionLlanta.BAJA -> "Presión baja: debe calibrarla" to ColorMalo
                ResultadoPresionLlanta.ALTA -> "Presión alta: debe ajustarla" to ColorRegular
                ResultadoPresionLlanta.OK -> "Presión correcta" to ColorBueno
                null -> posicion.rango() to MaterialTheme.colorScheme.onSurfaceVariant
            }
            OutlinedTextField(
                value = state.valores[posicion.codigo].orEmpty(),
                onValueChange = { onValorChange(posicion.codigo, it) },
                label = { Text(posicion.etiqueta, fontWeight = FontWeight.Bold) },
                suffix = { Text("PSI") },
                singleLine = true,
                isError = resultado == ResultadoPresionLlanta.BAJA,
                supportingText = {
                    Text(
                        if (resultado != null && resultado != ResultadoPresionLlanta.OK) "$mensaje · ${posicion.rango()}" else mensaje,
                        color = color
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Aviso al guardar con llantas fuera de rango. No bloquea: el operario puede guardar y calibrar
 * después; la inspección queda registrada con la presión medida.
 */
@Composable
fun PresionLlantasAvisoDialog(
    fueraDeRango: List<LlantaFueraDeRango>,
    onGuardarIgual: () -> Unit,
    onRevisar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onRevisar,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = ColorRegular, modifier = Modifier.size(40.dp))
        },
        title = { Text("Llantas por calibrar", fontWeight = FontWeight.Bold, color = ColorRegular) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                fueraDeRango.forEach { f ->
                    val baja = f.resultado == ResultadoPresionLlanta.BAJA
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "• ${f.posicion.etiqueta}: ${f.psi.psi()} PSI",
                            fontWeight = FontWeight.Bold,
                            color = if (baja) ColorMalo else ColorRegular
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (baja) "(mínimo ${f.posicion.psiMin.psi()})" else "(máximo ${f.posicion.psiMax.psi()})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.size(4.dp))
                Text(
                    "Puede guardar la inspección: la presión queda registrada. Calibre las llantas " +
                        "antes de operar o en cuanto pueda.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onGuardarIgual, colors = ButtonDefaults.buttonColors(containerColor = ColorRegular)) {
                Text("Guardar y calibrar después")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onRevisar) { Text("Revisar") }
        }
    )
}
