package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotocicletaScreen(
    networkStatus: Boolean,
    viewModel: MotocicletaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current




    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            Column {
                com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar(isConnected = networkStatus)
                TopAppBar(
                    title = { Text("Inspección Motocicleta") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. PLACA
            item {
                if (uiState.isLoadingData) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Cargando placas...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    DropdownField(
                        label = "Seleccione La PLACA de Su Motocicleta (*)",
                        displayValue = uiState.selectedMoto?.placa ?: "Seleccione una placa",
                        items = uiState.motocicletas,
                        itemLabel = { it.placa },
                        onItemSelected = { viewModel.onMotoSelected(it) }
                    )
                }
            }
            item { HorizontalDivider() }

            // 2. UBICACIÓN
            item {
                if (uiState.isLoadingData) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Cargando unidades...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    DropdownField(
                        label = "Seleccione la UNIDAD a la que Pertenece (*)",
                        displayValue = uiState.selectedUbicacion?.nombreUbicacion ?: "Seleccione la UNIDAD",
                        items = uiState.ubicaciones,
                        itemLabel = { it.nombreUbicacion },
                        onItemSelected = { viewModel.onUbicacionSelected(it) }
                    )
                }
            }
            item { HorizontalDivider() }

            // 3. KILOMETRAJE
            item {
                Column {
                    OutlinedTextField(
                        value = uiState.kilometraje,
                        onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.onKilometrajeChange(it) },
                        label = { Text("Escriba el KILOMETRAJE Actual de la Moto (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.kilometrajeError != null,
                        supportingText = {
                            if (uiState.kilometrajeError != null) {
                                Text(uiState.kilometrajeError ?: "", color = MaterialTheme.colorScheme.error)
                            } else if (uiState.kilometrajeMinimo > 0) {
                                Text(
                                    "Último kilometraje registrado: ${uiState.kilometrajeMinimo} km",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
            item { HorizontalDivider() }

            // Título sección documentos
            item {
                Text(
                    text = "Vigencia DOCUMENTACION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // 4. SOAT
            item {
                if (uiState.isLoadingDocumentos) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cargando documentos...")
                    }
                } else {
                    DocumentoSection(
                        titulo = "SOAT (Seguro Obligatorio) (*)",
                        state = uiState.soat,
                        isLocked = false,
                        onVigenciaSelected = { y, m, d -> viewModel.onSoatVigenciaChange(y, m, d) }
                    )
                }
            }
            item { HorizontalDivider() }

            // 5. REVISIÓN TÉCNICO-MECÁNICA
            item {
                DocumentoSection(
                    titulo = "Revisión TECNICOMECÁNICA (*)",
                    state = uiState.revisionTecno,
                    isLocked = false,
                    onVigenciaSelected = { y, m, d -> viewModel.onRevisionVigenciaChange(y, m, d) }
                )
            }
            item { HorizontalDivider() }

            // 6. LICENCIA DE CONDUCIR
            item {
                val isLicenciaLocked = uiState.licencia.yaRegistrado && uiState.licencia.estadoDoc == "Vigente"
                DocumentoSection(
                    titulo = "LICENCIA de Conducción (*)",
                    state = uiState.licencia,
                    isLocked = isLicenciaLocked,
                    onVigenciaSelected = { y, m, d -> viewModel.onLicenciaVigenciaChange(y, m, d) }
                )
            }
            item { HorizontalDivider() }

            // 7. ESTADO GENERAL
            item {
                MotoStatusSelector(
                    label = "Estado ACTUAL - GENERAL de La Motocicleta (*)",
                    selectedOption = uiState.estadoVehiculo,
                    onOptionSelected = { viewModel.onEstadoVehiculoChange(it) }
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 8. OBSERVACIONES
            item {
                OutlinedTextField(
                    value = uiState.observaciones,
                    onValueChange = { viewModel.onObservacionesChange(it) },
                    label = { Text("Observaciones y/o Aspectos a Revisar (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
            item { HorizontalDivider() }

            // 9. RESPONSABLE DE LA INSPECCIÓN
            item {
                OutlinedTextField(
                    value = uiState.responsable,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Responsable de la Inspección (Sesión Activa)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                )
            }

            // BOTÓN GUARDAR
            item {
                Button(
                    onClick = { viewModel.onSaveClick() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    enabled = uiState.isSaveButtonEnabled && !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }

        if (uiState.saveCompleted) {
            AlertDialog(
                onDismissRequest = { /* No hacer nada para forzar click en Aceptar */ },
                title = { Text("Guardado Exitoso", fontWeight = FontWeight.Bold) },
                text = { Text("La inspección se ha guardado correctamente.") },
                confirmButton = {
                    Button(onClick = { onNavigateBack(); viewModel.onNavigationDone() }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}
@Composable
fun CheckDropdown(
    label: String,
    value: String,
    onSelect: (String) -> Unit,
    items: List<String> = listOf("Bueno", "Malo", "No Aplica")
) {
    DropdownField(
        label = label,
        displayValue = value.ifBlank { "Seleccione Una Opción" },
        items = items,
        itemLabel = { it },
        onItemSelected = onSelect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownField(
    label: String,
    displayValue: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (items.isEmpty()) {
                DropdownMenuItem(text = { Text("No hay opciones disponibles") }, onClick = { expanded = false })
            }
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = { onItemSelected(item); expanded = false }
                )
            }
        }
    }
}

@Composable
fun MotoStatusSelector(
    label: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val options = listOf("Óptimo", "Regular", "Malo")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                val backgroundColor = when (option) {
                    "Óptimo" -> Color(0xFF4CAF50)
                    "Regular" -> Color(0xFFFFA000)
                    else -> Color(0xFFD32F2F)
                }
                val textColor = if (isSelected) Color.White
                    else Color.Black

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(2.dp, if (isSelected) backgroundColor else Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onOptionSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DocumentoSection(
    titulo: String,
    state: DocumentoState,
    isLocked: Boolean,
    onVigenciaSelected: (year: Int, month: Int, day: Int) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val (initialYear, initialMonth) = remember(state.vigencia) {
        if (state.vigencia.contains("-")) {
            val parts = state.vigencia.split("-")
            Pair(
                parts[0].toIntOrNull() ?: calendar.get(Calendar.YEAR),
                (parts[1].toIntOrNull()?.minus(1)) ?: calendar.get(Calendar.MONTH)
            )
        } else {
            Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
        }
    }

    if (showPicker) {
        DocumentoMonthYearPickerDialog(
            onDismissRequest = { showPicker = false },
            onDateSelected = { y, m -> onVigenciaSelected(y, m, 0); showPicker = false },
            initialYear = initialYear,
            initialMonth = initialMonth
        )
    }

    if (showFullImage && state.imagenUrl != null) {
        ImageDialog(
            imageUrl = state.imagenUrl,
            onDismissRequest = { showFullImage = false }
        )
    }

    val statusClean = state.estadoDoc.trim().lowercase()
    val statusColor = when {
        statusClean == "vigente"           -> Color(0xFF43A047) // Verde
        statusClean.contains("próximo")    -> Color(0xFFFFB300) // Amarillo/Ámbar vibrante
        statusClean == "vencido"           -> Color(0xFFE53935) // Rojo vibrante
        else                               -> Color(0xFF757575) // Gris
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // Título
            Text(titulo, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            // Badge de estado
            val isCalculado = state.estadoDoc.isNotBlank()
            val badgeText = if (isCalculado) "Estado: ${state.estadoDoc}" else "Estado: Sin registro"

            Surface(
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, statusColor),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = badgeText.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            // Campo de fecha
            OutlinedTextField(
                value = state.vigencia,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        text = if (isLocked) "Vigencia (Bloqueado)" else "Fecha de Vencimiento (Mes/Año)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                placeholder = { Text("Toca para seleccionar") },
                trailingIcon = {
                    if (!isLocked) {
                        IconButton(onClick = { showPicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!isLocked) Modifier.clickable { showPicker = true } else Modifier)
            )

            if (isLocked) {
                Text(
                    text = "La información está vigente. Solo se podrá actualizar cuando esté próxima a vencer.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Imagen del documento (Solo lectura del servidor)
            if (state.imagenUrl != null && state.imagenUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { showFullImage = true }
                ) {
                    AsyncImage(
                        model = state.imagenUrl,
                        contentDescription = "Foto $titulo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Indicador de que es clicable
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            "Ver en grande",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                // Placeholder o mensaje si no hay imagen
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("No hay imagen para este documento", 
                             style = MaterialTheme.typography.bodySmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ImageDialog(imageUrl: String, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Vista ampliada",
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun DocumentoMonthYearPickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (year: Int, month: Int) -> Unit,
    initialYear: Int,
    initialMonth: Int
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    val months = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.width(400.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Año anterior")
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Año siguiente")
                    }
                }

                Spacer(Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(months) { index, month ->
                        val isSelected = (selectedYear == initialYear && index == initialMonth)
                        OutlinedButton(
                            onClick = {
                                onDateSelected(selectedYear, index + 1)
                                onDismissRequest()
                            },
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (isSelected) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(text = month, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}


