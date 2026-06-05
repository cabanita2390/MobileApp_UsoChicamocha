package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotoCambioAceiteScreen(
    viewModel: MotoCambioAceiteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                title = { Text("Cambio de aceite — Motocicleta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            item {
                MotoDropdownField(
                    motos = uiState.motos,
                    selectedMoto = uiState.selectedMoto,
                    enabled = uiState.isRoleAllowed,
                    onMotoSelected = { viewModel.onMotoSelected(it) }
                )
            }

            item {
                MotoOilBrandDropdown(
                    oils = uiState.motoOilBrands,
                    selectedOil = uiState.selectedOil,
                    enabled = uiState.motoOilBrands.isNotEmpty() && uiState.isRoleAllowed,
                    onOilSelected = { viewModel.onOilSelected(it) }
                )
                if (uiState.motoOilBrands.isEmpty()) {
                    Text(
                        "Sincronice los aceites desde el menú principal para elegir marca.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.kmAtChange,
                    onValueChange = { viewModel.onKmAtChangeChange(it) },
                    label = { Text("Kilometraje actual (*)", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.intervalKm,
                    onValueChange = { viewModel.onIntervalKmChange(it) },
                    label = { Text("Intervalo próximo cambio (km) (*)", fontWeight = FontWeight.Bold) },
                    supportingText = { Text("Típico: 2000-3000 km para motos", style = MaterialTheme.typography.bodySmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.onQuantityChange(it) },
                    label = { Text("Cantidad (L) — opcional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = uiState.airFilterChanged,
                        enabled = uiState.isRoleAllowed,
                        onCheckedChange = { viewModel.onAirFilterChanged(it) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("¿Se cambió el filtro de aire?", style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Button(
                    onClick = { viewModel.submit() },
                    enabled = !uiState.isLoading && uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar cambio de aceite", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MotoDropdownField(
    motos: List<MotoItem>,
    selectedMoto: MotoItem?,
    enabled: Boolean,
    onMotoSelected: (MotoItem) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayText = if (selectedMoto != null) {
        if (!selectedMoto.marca.isNullOrBlank()) "${selectedMoto.placa} — ${selectedMoto.marca}"
        else selectedMoto.placa
    } else ""
    val filteredMotos = remember(motos, searchQuery) {
        if (searchQuery.isBlank()) motos
        else motos.filter {
            it.placa.contains(searchQuery, ignoreCase = true) ||
            (it.marca?.contains(searchQuery, ignoreCase = true) ?: false)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Motocicleta (*)", fontWeight = FontWeight.Bold) },
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
                    Text("Seleccionar motocicleta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar placa...") },
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
                        if (motos.isEmpty()) {
                            item { Text("Sin motocicletas. Sincronice el catálogo.", color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else if (filteredMotos.isEmpty()) {
                            item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else {
                            items(filteredMotos) { moto ->
                                Column {
                                    Text(
                                        text = if (!moto.marca.isNullOrBlank()) "${moto.placa} — ${moto.marca}" else moto.placa,
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onMotoSelected(moto); showDialog = false; searchQuery = "" }
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
private fun MotoOilBrandDropdown(
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
