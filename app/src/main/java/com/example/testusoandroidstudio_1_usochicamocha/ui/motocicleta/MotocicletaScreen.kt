package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.Manifest
import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import coil.compose.AsyncImage
import com.example.testusoandroidstudio_1_usochicamocha.ui.form.StatusSelector
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotocicletaScreen(
    viewModel: MotocicletaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current



    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inspección Motocicleta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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
                var showAddPlateDialog by remember { mutableStateOf(false) }
                var newPlateText by remember { mutableStateOf("") }

                if (showAddPlateDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddPlateDialog = false },
                        title = { Text("Registrar Nueva Placa") },
                        text = {
                            OutlinedTextField(
                                value = newPlateText,
                                onValueChange = { newPlateText = it.uppercase() },
                                label = { Text("Placa") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newPlateText.isNotBlank()) {
                                        viewModel.registrarNuevaPlaca(newPlateText)
                                        showAddPlateDialog = false
                                        newPlateText = ""
                                    }
                                }
                            ) { Text("Registrar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddPlateDialog = false }) { Text("Cancelar") }
                        }
                    )
                }

                if (uiState.isLoadingData) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Cargando placas...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Seleccione La PLACA de Su Motocicleta (*)",
                                displayValue = uiState.selectedMoto?.placa ?: "Seleccione una placa",
                                items = uiState.motocicletas,
                                itemLabel = { it.placa },
                                onItemSelected = { viewModel.onMotoSelected(it) }
                            )
                        }
                        
                        IconButton(
                            onClick = { showAddPlateDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.size(48.dp) // Quité el padding(top = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add, 
                                contentDescription = "Añadir Placa"
                            )
                        }
                    }
                }
            }
            item { HorizontalDivider() }

            // 2. UBICACIÓN
            item {
                if (uiState.isLoadingData) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Cargando ubicaciones...", style = MaterialTheme.typography.bodyMedium)
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
                OutlinedTextField(
                    value = uiState.kilometraje,
                    onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.onKilometrajeChange(it) },
                    label = { Text("Escriba el KILOMETRAJE Actual de la Moto (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item { HorizontalDivider() }

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
                        titulo = "SOAT (Seguro Obligatorio)",
                        state = uiState.soat,
                        isLocked = false, // SOAT siempre se puede actualizar si se desea (o sigue su propia lógica de isDateLocked)
                        onVigenciaSelected = { y, m, d -> viewModel.onSoatVigenciaChange(y, m, d) },
                        onImagenSelected = { viewModel.onSoatImagenSelected(it) },
                        onImagenRemoved = { viewModel.onSoatImagenRemoved() },
                        context = context
                    )
                }
            }
            item { HorizontalDivider() }

            // 5. REVISIÓN TÉCNICO-MECÁNICA
            item {
                DocumentoSection(
                    titulo = "Revisión TECNICOMECÁNICA",
                    state = uiState.revisionTecno,
                    isLocked = false,
                    onVigenciaSelected = { y, m, d -> viewModel.onRevisionVigenciaChange(y, m, d) },
                    onImagenSelected = { viewModel.onRevisionImagenSelected(it) },
                    onImagenRemoved = { viewModel.onRevisionImagenRemoved() },
                    context = context
                )
            }
            item { HorizontalDivider() }

            // 6. LICENCIA DE CONDUCIR
            item {
                // Requirement: Licencia se bloquea solo si YA está en el sistema y sigue vigente
                val isLicenciaLocked = uiState.licencia.yaRegistrado && uiState.licencia.estadoDoc == "Vigente"
                DocumentoSection(
                    titulo = "LICENCIA de Conducción",
                    state = uiState.licencia,
                    isLocked = isLicenciaLocked,
                    onVigenciaSelected = { y, m, d -> viewModel.onLicenciaVigenciaChange(y, m, d) },
                    onImagenSelected = { viewModel.onLicenciaImagenSelected(it) },
                    onImagenRemoved = { viewModel.onLicenciaImagenRemoved() },
                    context = context
                )
            }
            item { HorizontalDivider() }

            // 7. ESTADO GENERAL
            item {
                StatusSelector(
                    label = "Estado ACTUAL - GENERAL de La Motocicleta (*)",
                    selectedOption = uiState.estadoGeneral,
                    onOptionSelected = { viewModel.onEstadoGeneralChange(it) }
                )
            }
            item { HorizontalDivider() }

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
                    leadingIcon = { 
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) 
                    }
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

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Alerta de éxito
        if (uiState.saveCompleted) {
            AlertDialog(
                onDismissRequest = { /* No hacer nada para forzar click en Aceptar */ },
                title = { Text("Guardado Exitoso", fontWeight = FontWeight.Bold) },
                text = { Text("La inspección se ha guardado correctamente.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onNavigateBack()
                            viewModel.onNavigationDone()
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

/** Sección de un documento: título + indicador si ya está en BD + selector de fecha + botones de foto */
@Composable
fun DocumentoSection(
    titulo: String,
    state: DocumentoState,
    isLocked: Boolean,
    onVigenciaSelected: (year: Int, month: Int, day: Int) -> Unit,
    onImagenSelected: (Uri) -> Unit,
    onImagenRemoved: () -> Unit,
    context: android.content.Context
) {
    val calendar = Calendar.getInstance()

    var showDateDialog by remember { mutableStateOf(false) }

    // Camera / gallery launchers
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let(onImagenSelected) }
    )
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success) tempUri?.let(onImagenSelected) }
    )
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val file = File(context.cacheDir, "moto_doc_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                tempUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    )

    // ALWAYS use Month/Year precision for all scenarios as requested
    if (showDateDialog) {
        val currentVigencia = state.vigencia
        val parts = currentVigencia.split("-")
        val yr = if (parts.size >= 1) parts[0].toIntOrNull() ?: calendar.get(Calendar.YEAR) else calendar.get(Calendar.YEAR)
        val mt = if (parts.size >= 2) (parts[1].toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1 else calendar.get(Calendar.MONTH)

        MonthYearPickerDialog(
            onDismissRequest = { showDateDialog = false },
            onDateSelected = { y, m ->
                onVigenciaSelected(y, m, -1) // -1 signifies only Year-Month approximation
                showDateDialog = false
            },
            initialYear = yr,
            initialMonth = mt
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Título + badge si ya está registrado
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
            if (state.yaRegistrado) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Ya registrado",
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Ya registrado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Nuevo: Estado Informativo (Automático)
        val badgeColor = when (state.estadoDoc) {
            "Vigente" -> Color(0xFF4CAF50)
            "Próximo a vencer" -> Color(0xFFFFA000)
            "Vencido" -> Color(0xFFD32F2F)
            else -> Color.Gray
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = RoundedCornerShape(8.dp),
            color = badgeColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, badgeColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Estado: ${state.estadoDoc.ifBlank { "Sin calcular" }}",
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    fontSize = 14.sp
                )
                if (isLocked) {
                    Icon(Icons.Default.Lock, contentDescription = "Bloqueado", tint = badgeColor, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Selector de fecha
        val finalLocked = isLocked 
        OutlinedTextField(
            value = state.vigencia,
            onValueChange = {},
            readOnly = true,
            label = { 
                Text(
                    if (finalLocked) "Vigencia (Bloqueado)" 
                    else "Fecha Vencimiento (Mes/Año)"
                ) 
            },
            placeholder = { Text("Toca para seleccionar") },
            trailingIcon = {
                if (!finalLocked) {
                    IconButton(onClick = { showDateDialog = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!finalLocked) Modifier.clickable { showDateDialog = true } else Modifier)
        )

        // Botones de subir imagen
        if (!finalLocked) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) { Text("Subir foto") }

                OutlinedButton(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f)
                ) { Text("Tomar foto") }
            }
        } else if (state.estadoDoc == "Vigente") {
            Text(
                "La información está vigente. Solo se podrá actualizar cuando esté próxima a vencer.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Vista previa solo para IMAGEN NUEVA
        if (state.imagenUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier.size(80.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = state.imagenUri,
                        contentDescription = "Vista previa del documento",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Column {
                    Text("Nueva imagen seleccionada", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    
                    TextButton(
                        onClick = onImagenRemoved,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Eliminar selección", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthYearPickerDialog(
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

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = androidx.compose.ui.Modifier.width(320.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = androidx.compose.ui.Modifier.padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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

                Spacer(androidx.compose.ui.Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(months) { index, month ->
                        val isSelected = (selectedYear == initialYear && index == initialMonth)
                        OutlinedButton(
                            onClick = {
                                onDateSelected(selectedYear, index)
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

/** Dropdown genérico reutilizable */
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
            label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp) }, // Bajé de 17.sp a 15.sp para evitar cortes
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
