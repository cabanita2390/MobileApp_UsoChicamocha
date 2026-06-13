package com.example.testusoandroidstudio_1_usochicamocha.ui.combustible

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelLogEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombustibleHistorialScreen(
    assetType: String = "MACHINE",
    assetId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: CombustibleViewModel = hiltViewModel()
) {
    val allLogs by viewModel.fuelLogs.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }

    val logs = remember(allLogs, assetType, assetId) {
        if (assetId > 0) {
            allLogs.filter { it.assetType == assetType && it.assetId == assetId }
        } else {
            allLogs
        }
    }

    fun syncFuelLogs() {
        isSyncing = true
        scope.launch {
            try {
                viewModel.syncFuelLogsManually()
                Toast.makeText(context, "✅ Sincronización completada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "❌ Error sincronizando: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSyncing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Combustible") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { syncFuelLogs() }, enabled = !isSyncing) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Sincronizar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay registros de combustible guardados.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    FuelLogCard(log)
                }
            }
        }
    }
}

@Composable
private fun FuelLogCard(log: FuelLogEntity) {
    val syncColor = if (log.isSynced) Color(0xFF4CAF50) else Color(0xFFFF9800)
    val syncLabel = if (log.isSynced) "Sincronizado" else "Pendiente"

    val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
    val formattedDate = try {
        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.format(iso.parse(log.fuelDateTime) ?: Date())
    } catch (_: Exception) {
        log.fuelDateTime
    }
    val quantityGallons = if (log.quantityUnit == "GALLONS") log.quantity
                          else log.quantityLiters / 3.785411784

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
                Text(formattedDate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (log.isAnomaly) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("Anomalía", color = Color.White, fontSize = 9.sp)
                        }
                    }
                    Badge(containerColor = syncColor) {
                        Text(syncLabel, color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Text(
                "${log.assetType} • ${log.assetPlate ?: "ID ${log.assetId}"}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                "${String.format("%.3f", quantityGallons)} Gal de ${log.fuelType.replace("_", " ")}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (log.odometerKm != null) {
                Text("Odómetro: ${String.format("%.1f", log.odometerKm)} km", fontSize = 12.sp)
            }
            if (log.hourMeter != null) {
                Text("Horómetro: ${String.format("%.1f", log.hourMeter)} h", fontSize = 12.sp)
            }

            Text(
                "Total: \$${String.format("%.2f", log.totalCostCalculated)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // Discrepancia silenciosa: se registra en el backend para revisión del admin
            // No se muestra al usuario para evitar alarmar

            if (log.efficiencyValue != null && log.efficiencyUnit != null) {
                val effLabel = when (log.efficiencyUnit) {
                    "KM_PER_GALLON" -> "${String.format("%.2f", log.efficiencyValue)} km/Gal"
                    "GALLON_PER_HOUR" -> "${String.format("%.2f", log.efficiencyValue)} Gal/h"
                    "KM_PER_LITER" -> "${String.format("%.2f", log.efficiencyValue)} km/L"
                    "LITER_PER_HOUR" -> "${String.format("%.2f", log.efficiencyValue)} L/h"
                    else -> "${String.format("%.2f", log.efficiencyValue)} ${log.efficiencyUnit}"
                }
                Text(
                    "Eficiencia: $effLabel",
                    fontSize = 12.sp,
                    color = if (log.isAnomaly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            if (!log.serviceStation.isNullOrBlank()) {
                Text("Estación: ${log.serviceStation}", fontSize = 11.sp)
            }
        }
    }
}
