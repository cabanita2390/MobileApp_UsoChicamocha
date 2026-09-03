package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotoHubScreen(
    networkStatus: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToForm: () -> Unit,
    onNavigateToCambioAceite: () -> Unit = {},
    viewModel: MotocicletaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isOperario = uiState.userRole == "OPERARIO"
    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar(isConnected = networkStatus)
                TopAppBar(
                    title = { Text("Menú Motocicletas") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        val isSyncingAny = uiState.isSyncingMotos || uiState.isSyncingUbicaciones || uiState.isSyncingDocumentos
                        IconButton(
                            onClick = { viewModel.onSyncAllClicked() },
                            enabled = !isSyncingAny
                        ) {
                            if (isSyncingAny) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = "Actualizar datos")
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección 1: Formularios Disponibles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Formularios Disponibles", style = MaterialTheme.typography.titleLarge)
                    Button(
                        onClick = onNavigateToForm,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Inspección Motocicleta", fontSize = 18.sp)
                    }
                    if (!isOperario) {
                        OutlinedButton(
                            onClick = onNavigateToCambioAceite,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Cambio de Aceite", fontSize = 18.sp)
                        }
                    }

                }
            }

            // Sección 2: Inspecciones Pendientes
            PendingInspeccionesCard(
                pendingList = uiState.pendingInspecciones,
                isSyncing = uiState.isSyncingPending,
                onSyncClicked = { viewModel.onSyncPendingClicked() }
            )

            // Sección 2.5: Cambios de Aceite Pendientes
            PendingOilChangesCard(
                pendingOilChanges = uiState.pendingOilChanges,
                isSyncing = uiState.isSyncingOilChanges,
                onSyncClicked = { viewModel.onSyncOilChangesClicked() }
            )

            // Sección 3: Sincronización de Datos (2 botones)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sincronización de Datos", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Botón 1: Placas + Unidades juntos
                        val isSyncingPlacasUnidades = uiState.isSyncingMotos || uiState.isSyncingUbicaciones
                        Button(
                            onClick = { viewModel.onSyncPlacasYUnidadesClicked() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSyncingPlacasUnidades,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSyncingPlacasUnidades) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text("Sincronizando...", fontSize = 16.sp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Actualizar Placas y Unidades", fontSize = 16.sp)
                            }
                        }

                        // Botón 2: Documentos (separado)
                        Button(
                            onClick = { viewModel.onSyncDocumentosClicked() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSyncingDocumentos,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isSyncingDocumentos) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text("Sincronizando...", fontSize = 16.sp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sincronizar Documentos", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PendingInspeccionesCard(
    pendingList: List<InspeccionMotoPendiente>,
    isSyncing: Boolean,
    onSyncClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Inspecciones Pendientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onSyncClicked, enabled = !isSyncing) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (pendingList.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No hay inspecciones de motos pendientes.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingList.forEach { inspeccion ->
                        PendingInspeccionItem(inspeccion)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingInspeccionItem(inspeccion: InspeccionMotoPendiente) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(inspeccion.timestamp))

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Placa: ${inspeccion.placaVehiculo}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text("Tipo: ${inspeccion.tipoVehiculo}", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Estado: ", style = MaterialTheme.typography.bodySmall)
            Text(
                "Pendiente 🔄",
                color = Color(0xFFFFA000),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PendingOilChangesCard(
    pendingOilChanges: List<com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity>,
    isSyncing: Boolean,
    onSyncClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Cambio Aceite Pendientes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSyncClicked, enabled = !isSyncing) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar cambios de aceite")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pendingOilChanges.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No hay cambios de aceite pendientes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingOilChanges.forEach { oilChange ->
                        PendingOilChangeItem(oilChange)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingOilChangeItem(oilChange: com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(oilChange.timestamp))

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Placa: ${oilChange.placa}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text("Marca: ${oilChange.oilBrandName}", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Estado: ", style = MaterialTheme.typography.bodySmall)
            Text("Pendiente 🔄", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}
