package com.example.testusoandroidstudio_1_usochicamocha.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sync
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
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PendingFormStatus
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    networkStatus: Boolean,
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToForm: () -> Unit,
    onNavigateToImprevisto: () -> Unit,
    onNavigateToMantenimiento: (Int?) -> Unit,
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

    LaunchedEffect(uiState.syncMachinesMessage) {
        uiState.syncMachinesMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSyncMachinesMessage()
        }
    }

    LaunchedEffect(uiState.syncOilsMessage) {
        uiState.syncOilsMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSyncOilsMessage()
        }
    }

    LaunchedEffect(uiState.syncFormsMessage) {
        uiState.syncFormsMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSyncFormsMessage()
        }
    }

    LaunchedEffect(uiState.syncMaintenanceMessage) {
        uiState.syncMaintenanceMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSyncMaintenanceMessage()
        }
    }

    // AÑADIDO: LaunchedEffect para el nuevo mensaje de sincronización de imágenes
    LaunchedEffect(uiState.syncImagesMessage) {
        uiState.syncImagesMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSyncImagesMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                ConnectionStatusTopBar(isConnected = networkStatus)
                TopAppBar(
                    title = { Text("Menú Principal") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToLogs) {
                            Icon(Icons.Filled.History, contentDescription = "Ver Logs")
                        }
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
            AvailableFormsCard(
                onNavigateToForm = onNavigateToForm,
                onNavigateToImprevisto = onNavigateToImprevisto,
                onNavigateToMantenimiento = { onNavigateToMantenimiento(null) },
                onNavigateToCombustible = onNavigateToCombustible,
                showOilChange = !isOperario
            )

            PendingMaintenanceCard(
                pendingMaintenance = uiState.pendingMaintenanceForms,
                isSyncing = uiState.isSyncingMaintenance,
                onSyncClicked = { viewModel.onSyncMaintenanceClicked() },
                onEditClicked = { id -> onNavigateToMantenimiento(id) }
            )

            PendingFormsCard(
                pendingForms = uiState.pendingForms,
                isSyncingForms = uiState.isSyncingForms,
                onSyncFormsClicked = { viewModel.onSyncFormsClicked() }
            )

            SyncActionsCard(
                isSyncingMachines = uiState.isSyncingMachines,
                onSyncMachinesClicked = { viewModel.onSyncMachinesClicked() },
                isSyncingOils = uiState.isSyncingOils,
                onSyncOilsClicked = { viewModel.onSyncOilsClicked() },
                // AÑADIDO: Pasamos la nueva función al Composable
                onSyncImagesClicked = { viewModel.onSyncImagesClicked() }
            )
        }
        if (uiState.showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = { viewModel.onConfirmLogout() },
                onDismiss = { viewModel.onDismissLogoutDialog() }
            )
        }
    }
}

@Composable
fun SyncActionsCard(
    isSyncingMachines: Boolean,
    onSyncMachinesClicked: () -> Unit,
    isSyncingOils: Boolean,
    onSyncOilsClicked: () -> Unit,
    onSyncImagesClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sincronización de Datos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onSyncMachinesClicked,
                    enabled = !isSyncingMachines,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSyncingMachines) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Sincronizar Máquinas")
                    }
                }
                Button(
                    onClick = onSyncOilsClicked,
                    enabled = !isSyncingOils,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSyncingOils) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Sincronizar Aceites")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSyncImagesClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Forzar Sincronización de Imágenes")
            }
        }
    }
}

@Composable
fun PendingFormsCard(
    pendingForms: List<PendingFormStatus>, // Modificado para recibir la nueva clase
    isSyncingForms: Boolean,
    onSyncFormsClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Inspecciones Pendientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Button(
                    onClick = onSyncFormsClicked,
                    enabled = !isSyncingForms,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (isSyncingForms) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar Inspecciones")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (pendingForms.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No hay texto de inspecciones pendientes.")
                }
            } else {
                // Usamos un Column normal dentro del scroll principal en lugar de un LazyColumn anidado
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pendingForms.forEach { formStatus ->
                        PendingFormItem(formStatus = formStatus) // Modificado
                        Divider()
                    }
                }
            }
        }
    }
}


@Composable
fun AvailableFormsCard(
    onNavigateToForm: () -> Unit,
    onNavigateToImprevisto: () -> Unit,
    onNavigateToMantenimiento: () -> Unit,
    onNavigateToCombustible: () -> Unit = {},
    showOilChange: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Formularios disponibles", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onNavigateToForm, modifier = Modifier.fillMaxWidth()) {
                Text("Inspección Maquinaria", fontSize = 18.sp)
            }
            OutlinedButton(onClick = onNavigateToImprevisto, modifier = Modifier.fillMaxWidth()) {
                Text("Imprevisto Maquinaria", fontSize = 18.sp)
            }
            if (showOilChange) {
                OutlinedButton(onClick = onNavigateToMantenimiento, modifier = Modifier.fillMaxWidth()) {
                    Text("Cambio aceite", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun PendingMaintenanceCard(
    pendingMaintenance: List<Maintenance>,
    isSyncing: Boolean,
    onSyncClicked: () -> Unit,
    onEditClicked: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Cambio aceite Pendientes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Button(
                    onClick = onSyncClicked,
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar cambios de aceite")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (pendingMaintenance.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No hay fomularios de cambio de aceite pendientes.")
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pendingMaintenance.forEach { maintenance ->
                        PendingMaintenanceItem(
                            maintenance = maintenance,
                            onEditClicked = onEditClicked
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun PendingMaintenanceItem(
    maintenance: Maintenance,
    onEditClicked: (Int) -> Unit
) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(maintenance.dateTime))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cambio de aceite (${maintenance.type})", fontWeight = FontWeight.Bold)
                    Text(formattedDate, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { onEditClicked(maintenance.id) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("EDITAR")
                }
            }

            if (maintenance.syncError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Error: ${maintenance.syncError}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// MODIFICADO: El Composable ahora recibe el objeto de estado completo
@Composable
fun PendingFormItem(formStatus: PendingFormStatus) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(formStatus.form.timestamp))
    val formType = if (formStatus.form.isUnexpected) "Imprevisto" else "Inspección"

    val formSyncStatusText = if (formStatus.form.isSynced) "Sync ✔" else "Sync 🔄"
    val formSyncStatusColor = if (formStatus.form.isSynced) Color(0xFF4CAF50) else Color.Gray

    // Lógica para el estado de las imágenes
    val imagesSyncStatusText = if (formStatus.totalImageCount > 0) {
        if (formStatus.syncedImageCount == formStatus.totalImageCount) "Sync ✔" else "Sync 🔄"
    } else {
        "N/A"
    }
    val imagesSyncStatusColor = if (formStatus.totalImageCount > 0) {
        if (formStatus.syncedImageCount == formStatus.totalImageCount) Color(0xFF4CAF50) else Color(0xFFFFA000)
    } else {
        Color.Gray
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formType, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(formattedDate, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Formulario: ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    formSyncStatusText,
                    color = formSyncStatusColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Imágenes (${formStatus.syncedImageCount}/${formStatus.totalImageCount}): ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    imagesSyncStatusText,
                    color = imagesSyncStatusColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar Sesión") },
        text = { Text("¿Seguro desea salir? Esto cerrará la sesión actual.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Salir")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

