package com.example.testusoandroidstudio_1_usochicamocha.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar

private const val SHOW_FUEL_FEATURE = true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    networkStatus: Boolean,
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToMaquinaria: () -> Unit,
    onNavigateToVehicular: () -> Unit,
    onNavigateToMotos: () -> Unit,
    onNavigateToCombustible: () -> Unit = {},
    onNavigateToCambioAceiteMaquinaria: () -> Unit = {},
    onNavigateToCambioAceiteVehicular: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    // SUPERVISOR_OPERATIVO (nuevo) + ACEITE (legacy en BD aún no migrada) = mismo acceso
    val isSupervisorOperativo = uiState.userRole == "SUPERVISOR_OPERATIVO" || uiState.userRole == "ACEITE" || uiState.userRole == "MECANIC"
    val isAdmin = uiState.userRole == "ADMIN"
    @Suppress("UNUSED_VARIABLE") val isAceite = isSupervisorOperativo  // alias para compatibilidad

    LaunchedEffect(uiState.logoutCompleted) {
        if (uiState.logoutCompleted) {
            onLogout()
            viewModel.onLogoutCompleted()
        }
    }

    Scaffold(
        topBar = {
            Column {
                ConnectionStatusTopBar(isConnected = networkStatus)
                TopAppBar(
                    title = { Text("Menú Principal") },
                    actions = {
                        IconButton(onClick = { viewModel.onLogoutClick() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar Sesión"
                            )
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
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Qué deseas hacer?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            when {
                isSupervisorOperativo -> {
                    // SUPERVISOR_OPERATIVO: acceso directo a inspecciones y combustible
                    // Cambio de aceite es accesible desde dentro de cada módulo
                    InspectionCategoryCard(
                        title = "Inspección Maquinaria",
                        subtitle = "Registro de inspección\nde maquinaria pesada",
                        icon = Icons.Filled.Settings,
                        onClick = onNavigateToMaquinaria
                    )
                    InspectionCategoryCard(
                        title = "Inspección Vehicular",
                        subtitle = "Registro de inspección\nde vehículos",
                        icon = Icons.Filled.DirectionsCar,
                        onClick = onNavigateToVehicular
                    )
                    InspectionCategoryCard(
                        title = "Inspección de Motos",
                        subtitle = "Registro de inspección\nde motocicletas",
                        icon = Icons.Filled.DirectionsBike,
                        onClick = onNavigateToMotos
                    )
                    if (SHOW_FUEL_FEATURE) FuelCategoryCard(onClick = onNavigateToCombustible)
                }
                isAdmin -> {
                    // ADMIN: acceso completo
                    InspectionCategoryCard(
                        title = "Inspección Maquinaria",
                        subtitle = "Registro de inspección\nde maquinaria pesada",
                        icon = Icons.Filled.Settings,
                        onClick = onNavigateToMaquinaria
                    )
                    InspectionCategoryCard(
                        title = "Inspección Vehicular",
                        subtitle = "Registro de inspección\nde vehículos",
                        icon = Icons.Filled.DirectionsCar,
                        onClick = onNavigateToVehicular
                    )
                    InspectionCategoryCard(
                        title = "Inspección de Motos",
                        subtitle = "Registro de inspección\nde motocicletas",
                        icon = Icons.Filled.DirectionsBike,
                        onClick = onNavigateToMotos
                    )
                    if (SHOW_FUEL_FEATURE) FuelCategoryCard(onClick = onNavigateToCombustible)
                }
                else -> {
                    // OPERARIO: solo inspecciones pre-operativas + combustible (sin aceite)
                    InspectionCategoryCard(
                        title = "Inspección Maquinaria",
                        subtitle = "Registro de inspección\nde maquinaria pesada",
                        icon = Icons.Filled.Settings,
                        onClick = onNavigateToMaquinaria
                    )
                    InspectionCategoryCard(
                        title = "Inspección Vehicular",
                        subtitle = "Registro de inspección\nde vehículos",
                        icon = Icons.Filled.DirectionsCar,
                        onClick = onNavigateToVehicular
                    )
                    InspectionCategoryCard(
                        title = "Inspección de Motos",
                        subtitle = "Registro de inspección\nde motocicletas",
                        icon = Icons.Filled.DirectionsBike,
                        onClick = onNavigateToMotos
                    )
                    if (SHOW_FUEL_FEATURE) FuelCategoryCard(onClick = onNavigateToCombustible)
                }
            }
        }

        if (uiState.showLogoutDialog) {
            HomeLogoutDialog(
                onConfirm = { viewModel.onConfirmLogout() },
                onDismiss = { viewModel.onDismissLogoutDialog() }
            )
        }
    }
}

@Composable
fun InspectionCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(100.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FuelCategoryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(100.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalGasStation,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Registrar Combustible",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Maquinaria · Vehículos · Motos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HomeLogoutDialog(
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
