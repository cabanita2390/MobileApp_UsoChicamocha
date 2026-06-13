package com.example.testusoandroidstudio_1_usochicamocha.ui.maquinaria

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.OilTypeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovedMachineOilChangeScreen(
    viewModel: ImprovedMachineOilChangeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedOilType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            Toast.makeText(context, "Cambio de aceite guardado.", Toast.LENGTH_SHORT).show()
            viewModel.onSubmissionSuccessHandled()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Registrar cambio de aceite", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Maquinaria", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.isRoleAllowed) {
                Button(
                    onClick = { viewModel.submitMachineOilChange() },
                    enabled = !uiState.isLoading && uiState.isRoleAllowed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardando...", fontSize = 14.sp)
                    } else {
                        Text("✓ Guardar cambio", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .padding(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (!uiState.isRoleAllowed) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "No tiene permisos para registrar cambios de aceite",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Selector de máquina ───────────────────────────────────────────
            item {
                MachineDropdownField(
                    machines = uiState.machines,
                    selectedMachine = uiState.selectedMachine,
                    enabled = uiState.isRoleAllowed,
                    onMachineSelected = { viewModel.onMachineSelected(it) }
                )
            }

            // 🆕 SELECTOR TIPO DE ACEITE (NUEVO)
            item {
                OilTypeSelector(
                    selectedType = selectedOilType,
                    assetType = "MACHINERY",
                    onTypeSelected = { oilType ->
                        selectedOilType = oilType.code
                    }
                )
            }

            // ── Marca de aceite ───────────────────────────────────────────────
            item {
                MachineOilBrandDropdown(
                    oils = uiState.oils,
                    selectedOil = uiState.selectedOil,
                    enabled = uiState.oils.isNotEmpty() && uiState.isRoleAllowed,
                    onOilSelected = { viewModel.onOilSelected(it) }
                )
                if (uiState.oils.isEmpty()) {
                    Text(
                        "Sincronice los aceites desde el menú principal para elegir marca.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // ── Horometro actual ──────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.hourStamp,
                    onValueChange = { viewModel.onHourStampChanged(it) },
                    label = { Text("Horometro actual (hrs) (*)", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // ── Cantidad de aceite ────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.onQuantityChanged(it) },
                    label = { Text("Cantidad (L) — opcional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // ── Aceite motor ──────────────────────────────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = uiState.motorOil,
                        enabled = uiState.isRoleAllowed,
                        onCheckedChange = { viewModel.onMotorOilChanged(it) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("¿Cambio de aceite motor?", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // ── Aceite hidráulico ─────────────────────────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = uiState.hydraulicOil,
                        enabled = uiState.isRoleAllowed,
                        onCheckedChange = { viewModel.onHydraulicOilChanged(it) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("¿Cambio de aceite hidráulico?", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // ── Botón guardar ─────────────────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun MachineDropdownField(
    machines: List<MachineItem>,
    selectedMachine: MachineItem?,
    enabled: Boolean,
    onMachineSelected: (MachineItem) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayText = selectedMachine?.let { "${it.codigo} — ${it.nombre}" } ?: ""
    val filteredMachines = remember(machines, searchQuery) {
        if (searchQuery.isBlank()) machines
        else machines.filter {
            it.codigo.contains(searchQuery, ignoreCase = true) ||
            it.nombre.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Máquina (*)", fontWeight = FontWeight.Bold) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (enabled) {
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
                        placeholder = { Text("Buscar código, nombre...") },
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
                            item { Text("Sin máquinas. Sincronice el catálogo.", color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else if (filteredMachines.isEmpty()) {
                            item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else {
                            items(filteredMachines) { machine ->
                                Column {
                                    Text(
                                        text = "${machine.codigo} — ${machine.nombre} (${machine.horasOperacion} hrs)",
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
private fun MachineOilBrandDropdown(
    oils: List<Oil>,
    selectedOil: Oil?,
    enabled: Boolean,
    onOilSelected: (Oil) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayText = selectedOil?.name ?: ""
    val filteredOils = remember(oils, searchQuery) {
        if (searchQuery.isBlank()) oils
        else oils.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Marca de aceite (*)", fontWeight = FontWeight.Bold) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (enabled) {
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
                    Text("Seleccionar marca de aceite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
