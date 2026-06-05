package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculoMainScreen(
    networkStatus: Boolean,
    viewModel: VehiculoMainViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToForm: () -> Unit,
    onNavigateToCambioAceite: () -> Unit,
    onNavigateToCombustible: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isOperario = uiState.userRole == "OPERARIO"

    LaunchedEffect(uiState.logoutCompleted) {
        if (uiState.logoutCompleted) {
            onLogout()
            viewModel.onLogoutCompleted()
        }
    }

    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                ConnectionStatusTopBar(isConnected = networkStatus)
                TopAppBar(
                    title = { Text("Menú Vehicular") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onLogoutClick() }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
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
            VehiculoAvailableFormsCard(
                onNavigateToForm = onNavigateToForm,
                onNavigateToCambioAceite = onNavigateToCambioAceite,
                onNavigateToCombustible = onNavigateToCombustible,
                showOilChange = !isOperario
            )

            PendingOilChangeVehiculoCard(
                pendingOilChanges = uiState.pendingOilChanges,
                isSyncing = uiState.isSyncingOilChanges,
                onSyncClicked = { viewModel.onSyncOilChangesClicked() }
            )

            PendingVehiculoFormsCard(
                pendingInspections = uiState.pendingInspections,
                isSyncing = uiState.isSyncingInspections,
                onSyncClicked = { viewModel.onSyncInspectionsClicked() }
            )

            VehiculoSyncActionsCard(
                isSyncingCatalog = uiState.isSyncingCatalog,
                onSyncCatalogClicked = { viewModel.onSyncCatalogClicked() },
                isSyncingDocs = uiState.isSyncingDocuments,
                onSyncDocsClicked = { viewModel.onSyncDocumentsClicked() }
            )
        }

        if (uiState.showLogoutDialog) {
            VehiculoLogoutDialog(
                onConfirm = { viewModel.onConfirmLogout() },
                onDismiss = { viewModel.onDismissLogoutDialog() }
            )
        }
    }
}

@Composable
fun VehiculoAvailableFormsCard(
    onNavigateToForm: () -> Unit,
    onNavigateToCambioAceite: () -> Unit,
    onNavigateToCombustible: () -> Unit = {},
    showOilChange: Boolean = true
) {
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
                Icon(Icons.Default.DirectionsCar, null)
                Spacer(Modifier.width(8.dp))
                Text("Inspección Vehicular", fontSize = 18.sp)
            }

            if (showOilChange) {
                OutlinedButton(
                    onClick = onNavigateToCambioAceite,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Build, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cambio aceite", fontSize = 18.sp)
                }
            }

        }
    }
}

@Composable
fun PendingOilChangeVehiculoCard(
    pendingOilChanges: List<VehiculoOilChangeEntity>,
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
                    "Cambio aceite Pendientes",
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
                        "No hay formularios de cambio de aceite pendientes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingOilChanges.forEach { item ->
                        PendingOilChangeVehiculoItem(oilChange = item)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingOilChangeVehiculoItem(oilChange: VehiculoOilChangeEntity) {
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

@Composable
fun PendingVehiculoFormsCard(
    pendingInspections: List<VehiculoInspectionEntity>,
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
                Text("Inspecciones Pendientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onSyncClicked,
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar Todo")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (pendingInspections.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No hay inspecciones pendientes", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingInspections.forEach { inspection ->
                        PendingVehiculoItem(inspection = inspection)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingVehiculoItem(inspection: VehiculoInspectionEntity) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(inspection.timestamp))
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Placa: ${inspection.placaVehiculo}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text("Tipo: ${inspection.tipoVehiculo}", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Estado: ", style = MaterialTheme.typography.bodySmall)
            Text("Pendiente 🔄", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun VehiculoSyncActionsCard(
    isSyncingCatalog: Boolean,
    onSyncCatalogClicked: () -> Unit,
    isSyncingDocs: Boolean,
    onSyncDocsClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sincronización de Datos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Botón 1: Sincronizar Vehículos
                Button(
                    onClick = onSyncCatalogClicked,
                    enabled = !isSyncingCatalog,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSyncingCatalog) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Sincronizando...", fontSize = 16.sp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sincronizar Vehículos", fontSize = 16.sp)
                    }
                }
                
                // Botón 2: Sincronizar Documentos
                Button(
                    onClick = onSyncDocsClicked,
                    enabled = !isSyncingDocs,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSyncingDocs) {
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

@Composable
fun VehiculoLogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar Sesión") },
        text = { Text("¿Desea cerrar la sesión actual en el menú vehicular?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Salir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
