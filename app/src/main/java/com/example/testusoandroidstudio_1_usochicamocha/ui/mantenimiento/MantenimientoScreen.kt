package com.example.testusoandroidstudio_1_usochicamocha.ui.mantenimiento

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Maintenance
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.ui.theme.AppUsoChicamochaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoScreen(
    viewModel: MantenimientoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Efecto para navegar hacia atrás cuando el formulario se guarda con éxito
    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            Toast.makeText(context, "Cambio aceite guardado localmente.", Toast.LENGTH_SHORT).show()
            viewModel.onSubmissionSuccessHandled() // Resetea el flag
            onNavigateBack()
        }
    }

    // Efecto para mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError() // Limpia el error después de mostrarlo
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de cambio aceite") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Usamos LazyColumn para un mejor rendimiento y para evitar overflows de pantalla
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MachineSelectorMantenimiento(
                    machines = uiState.machines,
                    selectedMachine = uiState.selectedMachine,
                    onMachineSelected = { viewModel.onFormEvent(MantenimientoFormEvent.MachineSelected(it)) },
                    isEnabled = !uiState.isLoading
                )
            }

            item {
                MaintenanceTypeSelector(
                    selectedType = uiState.maintenanceType,
                    onTypeSelected = { viewModel.onFormEvent(MantenimientoFormEvent.MaintenanceTypeChanged(it)) },
                    isEnabled = !uiState.isLoading
                )
            }

            item {
                MaintenanceDetailsCard(
                    uiState = uiState,
                    onFormEvent = viewModel::onFormEvent, // Pasamos la referencia a la función
                    onRetrySyncOils = { viewModel.syncOils() },
                    isEnabled = !uiState.isLoading
                )
            }

            item {
                Button(
                    onClick = { viewModel.onFormEvent(MantenimientoFormEvent.Submit) },
                    enabled = uiState.selectedMachine != null &&
                            uiState.maintenanceType != null &&
                            uiState.selectedOil != null &&
                            uiState.quantity.toDoubleOrNull() != null && // Validar que sea un número
                            !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (uiState.editingFormId != null) "ACTUALIZAR" else "GUARDAR", fontSize = 18.sp)
                    }
                }
            }

            item {
                PendingMaintenanceList(forms = uiState.pendingForms)
            }
        }
    }
}

@Composable
fun MachineSelectorMantenimiento(
    machines: List<Machine>,
    selectedMachine: Machine?,
    onMachineSelected: (Machine) -> Unit,
    isEnabled: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayText = selectedMachine?.let { "${it.name} - ${it.model} - ${it.internalIdentificationNumber}" } ?: "Seleccione una máquina"
    val filteredMachines = remember(machines, searchQuery) {
        if (searchQuery.isBlank()) machines
        else machines.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.model.contains(searchQuery, ignoreCase = true) ||
            it.internalIdentificationNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Máquina") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth()
        )
        if (isEnabled) {
            Spacer(modifier = Modifier.matchParentSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showDialog = true })
        }
    }

    if (showDialog) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(Unit) {
            delay(50)
            try { focusRequester.requestFocus() } catch (e: Exception) { }
            keyboardController?.show()
        }
        Dialog(
            onDismissRequest = { showDialog = false; searchQuery = "" },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seleccionar máquina", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar nombre, modelo, código...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {{
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                            }
                        }} else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        if (machines.isEmpty()) {
                            item { Text("Sin máquinas disponibles. Sincronice.", color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else if (filteredMachines.isEmpty()) {
                            item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else {
                            items(filteredMachines) { machine ->
                                Column {
                                    Text(
                                        text = "${machine.name} - ${machine.model} - ${machine.internalIdentificationNumber}",
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onMachineSelected(machine); showDialog = false; searchQuery = "" }
                                            .padding(horizontal = 8.dp, vertical = 10.dp)
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showDialog = false; searchQuery = "" },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Cancelar") }
                }
            }
        }
    }
}

@Composable
fun MaintenanceTypeSelector(
    selectedType: String?,
    onTypeSelected: (String) -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("TIPO DE CAMBIO:", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val motorButtonColors = if (selectedType == "motor") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                OutlinedButton(
                    onClick = { onTypeSelected("motor") },
                    modifier = Modifier.weight(1f),
                    colors = motorButtonColors,
                    enabled = isEnabled
                ) {
                    Text("ACEITE MOTOR", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))

                val hydraulicButtonColors = if (selectedType == "hydraulic") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                OutlinedButton(
                    onClick = { onTypeSelected("hydraulic") },
                    modifier = Modifier.weight(1f),
                    colors = hydraulicButtonColors,
                    enabled = isEnabled
                ) {
                    Text("HIDRÁULICO", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun MaintenanceDetailsCard(
    uiState: MantenimientoUiState,
    onFormEvent: (MantenimientoFormEvent) -> Unit,
    onRetrySyncOils: () -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Este campo NO cambia
            OutlinedTextField(
                value = uiState.currentHourMeter,
                onValueChange = { onFormEvent(MantenimientoFormEvent.CurrentHourMeterChanged(it)) },
                label = { Text("Horómetro al momento del cambio") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = isEnabled
            )

            // --- INICIO DE LA MODIFICACIÓN ---

            // 1. Obtenemos la lista de aceites como antes.
            val availableOils = when (uiState.maintenanceType) {
                "motor" -> uiState.motorOils
                "hydraulic" -> uiState.hydraulicOils
                else -> emptyList()
            }



                OilSelector(
                    oils = availableOils,
                    selectedOil = uiState.selectedOil, // <-- PASA EL OBJETO "selectedOil"
                    onOilSelected = { oil -> // <-- RECIBE EL OBJETO "oil"
                        onFormEvent(MantenimientoFormEvent.OilSelected(oil)) // <-- ENVÍA EL NUEVO EVENTO
                    },
                    isEnabled = isEnabled && uiState.maintenanceType != null,
                    allOilsEmpty = uiState.allOils.isEmpty(),
                    isSyncingOils = uiState.isSyncingOils,
                    onRetrySyncOils = onRetrySyncOils
                )


            OutlinedTextField(
                value = uiState.quantity,
                onValueChange = { onFormEvent(MantenimientoFormEvent.QuantityChanged(it)) },
                label = { Text("Cantidad (Gl)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = isEnabled
            )

            // Este campo NO cambia
            OutlinedTextField(
                value = uiState.averageHoursChange,
                onValueChange = { onFormEvent(MantenimientoFormEvent.AverageHoursChangeChanged(it)) },
                label = { Text("Horas para siguiente cambio") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = isEnabled
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMantenimientoScreen() {
    AppUsoChicamochaTheme {
        MantenimientoScreen(onNavigateBack = {})
    }
}

@Composable
fun OilSelector(
    oils: List<Oil>,
    selectedOil: Oil?,
    onOilSelected: (Oil) -> Unit,
    isEnabled: Boolean,
    allOilsEmpty: Boolean = false,
    isSyncingOils: Boolean = false,
    onRetrySyncOils: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayText = selectedOil?.name ?: "Seleccione un aceite"
    val filteredOils = remember(oils, searchQuery) {
        if (searchQuery.isBlank()) oils
        else oils.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Marca del Aceite") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth()
        )
        if (isEnabled) {
            Spacer(modifier = Modifier.matchParentSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showDialog = true })
        }
    }

    if (allOilsEmpty) {
        if (isSyncingOils) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Sincronizando marcas de aceite...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    "No hay marcas de aceite sincronizadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRetrySyncOils) {
                    Text("Reintentar")
                }
            }
        }
    }

    if (showDialog) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(Unit) {
            delay(50)
            try { focusRequester.requestFocus() } catch (e: Exception) { }
            keyboardController?.show()
        }
        Dialog(
            onDismissRequest = { showDialog = false; searchQuery = "" },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seleccionar aceite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar marca...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {{
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                            }
                        }} else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        if (filteredOils.isEmpty()) {
                            item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else {
                            items(filteredOils) { oil ->
                                Column {
                                    Text(
                                        text = oil.name,
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onOilSelected(oil); showDialog = false; searchQuery = "" }
                                            .padding(horizontal = 8.dp, vertical = 10.dp)
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showDialog = false; searchQuery = "" },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Cancelar") }
                }
            }
        }
    }
}

@Composable
fun PendingMaintenanceList(forms: List<Maintenance>) {
    if (forms.isNotEmpty()) {
        Text(
            "Pendientes de Sincronización / Errores",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        forms.forEach { form ->
            PendingMaintenanceItem(form)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PendingMaintenanceItem(form: Maintenance) {
    val cardColor = if (form.syncError != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (form.syncError != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Máquina ID: ${form.machineId} - ${form.type.uppercase()}",
                style = MaterialTheme.typography.titleSmall,
                color = contentColor
            )
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(
                "Fecha: ${dateFormat.format(Date(form.dateTime))}",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (form.isSyncing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = contentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sincronizando...", style = MaterialTheme.typography.bodySmall, color = contentColor)
                }
            } else {
                Text("Pendiente de envío", style = MaterialTheme.typography.bodySmall, color = contentColor)
            }
        }
    }
}
