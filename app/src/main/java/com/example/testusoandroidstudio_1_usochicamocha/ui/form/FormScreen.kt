package com.example.testusoandroidstudio_1_usochicamocha.ui.form

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: FormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- LÓGICA PARA SELECCIONAR IMÁGENES ---
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.onImageSelected(it) }
        }
    )

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // Usamos tempImageUri aquí, que fue asignado antes de lanzar la cámara
                tempImageUri?.let { viewModel.onImageSelected(it) }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                val newUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                // CORRECCIÓN: Guardamos la URI en la variable de estado
                tempImageUri = newUri
                // Y lanzamos el launcher con la variable local newUri, que no es nula
                takePictureLauncher.launch(newUri)
            }
        }
    )

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            onNavigateBack()
            viewModel.onNavigationDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inspección Maquinaria") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
            item {
MachineSelector(
                    machines = uiState.machines,
                    selectedMachine = uiState.selectedMachine,
                    onMachineSelected = { viewModel.onMachineSelected(it) }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                OutlinedTextField(
                    value = uiState.horometro,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            viewModel.onHorometroChange(newValue)
                        }
                    },
                    label = { Text("Escriba el HOROMETRO Actual (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { StatusSelector("Fugas en el Sistema (*)", uiState.estadoFugas) { viewModel.onEstadoChange("Fugas", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Sistema de Frenos (*)", uiState.estadoFrenos) { viewModel.onEstadoChange("Frenos", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Estado de Correas y Poleas (*)", uiState.estadoCorreasPoleas) { viewModel.onEstadoChange("CorreasPoleas", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Estado de Llantas y/o Carriles (*)", uiState.estadoLlantasCarriles) { viewModel.onEstadoChange("LlantasCarriles", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Sistema de Encendido (*)", uiState.estadoEncendido) { viewModel.onEstadoChange("Encendido", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Sistema Eléctrico en General (*)", uiState.estadoElectrico) { viewModel.onEstadoChange("Electrico", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Sistema Mecánico en General (*)", uiState.estadoMecanico) { viewModel.onEstadoChange("Mecanico", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Nivel de Temperatura (*)", uiState.estadoTemperatura) { viewModel.onEstadoChange("Temperatura", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Nivel de Aceite (*)", uiState.estadoAceite) { viewModel.onEstadoChange("Aceite", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Nivel de Hidraulico (*)", uiState.estadoHidraulico) { viewModel.onEstadoChange("Hidraulico", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Nivel de Refrigerante (*)", uiState.estadoRefrigerante) { viewModel.onEstadoChange("Refrigerante", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
            item { StatusSelector("Estado Estructural en General (*)", uiState.estadoEstructural) { viewModel.onEstadoChange("Estructural", it) } }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                ExtinguisherDatePicker(
                    selectedDate = uiState.vigenciaExtintor,
                    onDateSelected = { year, month ->
                        viewModel.onExtinguisherDateChange(year, month)
                    }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                OutlinedTextField(
                    value = uiState.observaciones,
                    onValueChange = { viewModel.onObservacionesChange(it) },
                    label = { Text("Observaciones y/o Aspectos a Revisar (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                GreasingSection(
                    greasedStatus = uiState.greasingStatus,
                    onGreasedStatusChange = { viewModel.onGreasedStatusChange(it) },
                    greasingAction = uiState.greasingAction,
                    onGreasingActionChange = { viewModel.onGreasingActionChange(it) },
                    greasingObservations = uiState.greasingObservations,
                    onGreasingObservationsChange = { viewModel.onGreasingObservationsChange(it) }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                ImageUploadSection(
                    selectedImageUris = uiState.selectedImageUris,
                    onPickImageClick = { pickImageLauncher.launch("image/*") },
                    onTakePhotoClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onRemoveImageClick = { viewModel.onImageRemoved(it) },
                    onViewImageClick = { viewModel.onPreviewImageClicked(it) }
                )
            }

            item {
                Button(
                    onClick = { viewModel.onSaveClick() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isSaveButtonEnabled
                ) {
                    Text("Guardar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (uiState.previewingImageUri != null) {
        ImagePreviewDialog(
            uri = uiState.previewingImageUri!!,
            onDismiss = { viewModel.onDismissPreview() },
            onDelete = { viewModel.onImageRemoved(uiState.previewingImageUri!!) }
        )
    }
}

@Composable
fun GreasingSection(
    greasedStatus: String,
    onGreasedStatusChange: (String) -> Unit,
    greasingAction: String,
    onGreasingActionChange: (String) -> Unit,
    greasingObservations: String,
    onGreasingObservationsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Engrasado (*)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
        Text("¿Se engrasó la máquina?", fontSize = 16.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Sí", "No").forEach { option ->
                val isSelected = option == greasedStatus
                OutlinedButton(
                    onClick = { onGreasedStatusChange(option) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                ) {
                    Text(option)
                }
            }
        }

        if (greasedStatus == "Sí") {
            Spacer(Modifier.height(8.dp))
            Text("Tipo de engrasado:", fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Total", "Parcial").forEach { option ->
                    val isSelected = option == greasingAction
                    val backgroundColor = when (option) {
                        "Total" -> Color(0xFF4CAF50)
                        "Parcial" -> Color(0xFFFFA000)
                        else -> Color.Gray
                    }
                    val textColor = if (isSelected) Color.White else Color.Black

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) backgroundColor else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onGreasingActionChange(option) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.2f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.background(if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = option,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = greasingObservations,
                onValueChange = onGreasingObservationsChange,
                label = { Text("Observaciones del Engrasado", fontWeight = FontWeight.Normal, fontSize = 16.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Composable
fun MachineSelector(
    machines: List<Machine>,
    selectedMachine: Machine?,
    onMachineSelected: (Machine) -> Unit,
    isEnabled: Boolean = true
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
            label = { Text("Máquina (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
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
                            item { Text("No hay máquinas disponibles. Verifica la sincronización.", color = MaterialTheme.colorScheme.error,
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
fun StatusSelector(
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
                    "Malo" -> Color(0xFFD32F2F)
                    else -> Color.Gray
                }
                val textColor = if (isSelected) Color.White else Color.Black

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) backgroundColor else Color.LightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onOptionSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExtinguisherDatePicker(
    selectedDate: String,
    onDateSelected: (year: Int, month: Int) -> Unit
) {
    val calendar = Calendar.getInstance()

    val (initialYear, initialMonth) = remember(selectedDate) {
        if (selectedDate.contains("-")) {
            val parts = selectedDate.split("-")
            Pair(
                parts[0].toIntOrNull() ?: calendar.get(Calendar.YEAR),
                (parts[1].toIntOrNull()?.minus(1)) ?: calendar.get(Calendar.MONTH)
            )
        } else {
            Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        MonthYearPickerDialog(
            onDismissRequest = { showDialog = false },
            onDateSelected = { year, month ->
                onDateSelected(year, month)
                showDialog = false
            },
            initialYear = initialYear,
            initialMonth = initialMonth
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Vigencia EXTINTOR", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            readOnly = true,
            label = { Text("Fecha de Vencimiento (YYYY-MM)", fontWeight = FontWeight.Normal, fontSize = 16.sp) },
            trailingIcon = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Seleccionar Fecha")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
        )
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

@Composable
fun ImageUploadSection(
    selectedImageUris: List<Uri>,
    onPickImageClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onRemoveImageClick: (Uri) -> Unit,
    onViewImageClick: (Uri) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Subir imágenes", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

            if (selectedImageUris.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImageUris) { uri ->
                        SelectedImageItem(
                            uri = uri,
                            onRemoveClick = { onRemoveImageClick(uri) },
                            onViewClick = { onViewImageClick(uri) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onPickImageClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Subir foto")
                }
                OutlinedButton(
                    onClick = onTakePhotoClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Tomar foto")
                }
            }
        }
    }
}

@Composable
fun SelectedImageItem(
    uri: Uri,
    onRemoveClick: () -> Unit,
    onViewClick: () -> Unit
) {
    val fileName = uri.lastPathSegment ?: "imagen.jpg"

    ListItem(
        headlineContent = { Text(fileName, maxLines = 1) },
        leadingContent = {
            IconButton(onClick = onViewClick) {
                Icon(Icons.Default.Visibility, contentDescription = "Ver imagen")
            }
        },
        trailingContent = {
            TextButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun ImagePreviewDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Previsualización",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(8.dp))

                AsyncImage(
                    model = uri,
                    contentDescription = "Imagen previsualizada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Volver")
                    }
                    Button(
                        onClick = {
                            onDelete()
                            onDismiss() // Cierra el diálogo después de eliminar
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
