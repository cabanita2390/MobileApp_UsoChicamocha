package com.example.testusoandroidstudio_1_usochicamocha.ui.combustible

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Historial de combustible del activo.
 *
 * El módulo se está reconstruyendo desde cero (ver rama
 * desarrollo-modulo-combustibles): esta pantalla conserva únicamente el
 * diseño/UI ya construido (encabezado, estado vacío, tarjeta de registro
 * de referencia) — no lee de Room ni de ningún backend todavía.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombustibleHistorialScreen(
    assetType: String = "MACHINE",
    assetId: Long = -1L,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Combustible") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay registros de combustible guardados.")
        }
    }
}

/**
 * Tarjeta de referencia visual para un registro de combustible.
 * Sin llamador todavía — queda como diseño para cuando el módulo
 * reconstruya su propia fuente de datos.
 */
@Composable
private fun FuelLogCard(
    fechaLabel: String,
    assetLabel: String,
    isSynced: Boolean,
    isAnomaly: Boolean,
    quantityGallonsLabel: String,
    fuelTypeLabel: String,
    odometerKmLabel: String?,
    hourMeterLabel: String?,
    totalCostLabel: String,
    efficiencyLabel: String?,
    serviceStation: String?
) {
    val syncColor = if (isSynced) Color(0xFF4CAF50) else Color(0xFFFF9800)
    val syncLabel = if (isSynced) "Sincronizado" else "Pendiente"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(fechaLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isAnomaly) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("Anomalía", color = Color.White, fontSize = 9.sp)
                        }
                    }
                    Badge(containerColor = syncColor) {
                        Text(syncLabel, color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Text(assetLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            Text("$quantityGallonsLabel de $fuelTypeLabel", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            odometerKmLabel?.let { Text("Odómetro: $it km", fontSize = 12.sp) }
            hourMeterLabel?.let { Text("Horómetro: $it h", fontSize = 12.sp) }

            Text("Total: \$$totalCostLabel", fontSize = 13.sp, fontWeight = FontWeight.Bold)

            efficiencyLabel?.let {
                Text(
                    "Eficiencia: $it",
                    fontSize = 12.sp,
                    color = if (isAnomaly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            if (!serviceStation.isNullOrBlank()) {
                Text("Estación: $serviceStation", fontSize = 11.sp)
            }
        }
    }
}
