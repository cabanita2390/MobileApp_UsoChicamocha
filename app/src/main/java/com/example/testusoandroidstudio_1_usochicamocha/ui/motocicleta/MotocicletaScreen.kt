package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    var focusedImage by remember { mutableStateOf<String?>(null) }

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
                .padding(16.dp)
                .testTag("moto_form_scroll"),
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
                        onItemSelected = { viewModel.onMotoSelected(it) },
                        dropdownTag = "plate_option"
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
                        onItemSelected = { viewModel.onUbicacionSelected(it) },
                        dropdownTag = "unit_option"
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
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

            // ─── 4. DOCUMENTACIÓN ───────────────────────────────────────────
            item {
                SectionCard("Vigencia DOCUMENTACION", modifier = Modifier.testTag("section_vigencia")) {
                    if (uiState.isLoadingDocumentos) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Consultando documentos...", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (uiState.selectedMoto == null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Seleccione una motocicleta para cargar los documentos registrados.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Sin documento registrado
                    }
                    Spacer(Modifier.height(12.dp))

                    // SOAT
                    DocLabelRow(label = "SOAT (Seguro Obligatorio)", icon = Icons.Default.Shield)
                    if (uiState.soat.vigenciaMaster.isNotBlank()) {
                        Text("📅 Registrado en sistema: ${uiState.soat.vigenciaMaster}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    DocDatePickerField(
                        selectedDate = uiState.soat.vigencia,
                        onDateSelected = { y, m -> viewModel.onSoatVigenciaChange(y, m, 0) },
                        enabled = uiState.selectedMoto != null
                    )
                    if (uiState.soat.estadoDoc.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        EstadoDocumentoChip(uiState.soat.estadoDoc)
                    }
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(
                        url = uiState.soat.imagenUrl,
                        label = "Imagen SOAT",
                        onClick = { focusedImage = uiState.soat.imagenUrl }
                    )

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // TECNO
                    DocLabelRow(label = "Revisión TECNICOMECÁNICA", icon = Icons.Default.Build)
                    if (uiState.revisionTecno.vigenciaMaster.isNotBlank()) {
                        Text("📅 Registrado en sistema: ${uiState.revisionTecno.vigenciaMaster}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    DocDatePickerField(
                        selectedDate = uiState.revisionTecno.vigencia,
                        onDateSelected = { y, m -> viewModel.onRevisionVigenciaChange(y, m, 0) },
                        enabled = uiState.selectedMoto != null
                    )
                    if (uiState.revisionTecno.estadoDoc.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        EstadoDocumentoChip(uiState.revisionTecno.estadoDoc)
                    }
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(
                        url = uiState.revisionTecno.imagenUrl,
                        label = "Imagen Tecnomecánica",
                        onClick = { focusedImage = uiState.revisionTecno.imagenUrl }
                    )

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // LICENCIA
                    DocLabelRow(label = "Licencia de Conducción", icon = Icons.Default.AccountBox)
                    if (uiState.licencia.vigenciaMaster.isNotBlank()) {
                        Text("📅 Registrado en sistema: ${uiState.licencia.vigenciaMaster}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    DocDatePickerField(
                        selectedDate = uiState.licencia.vigencia,
                        onDateSelected = { y, m -> viewModel.onLicenciaVigenciaChange(y, m, 0) },
                        enabled = uiState.selectedMoto != null
                    )
                    if (uiState.licencia.estadoDoc.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        EstadoDocumentoChip(uiState.licencia.estadoDoc)
                    }
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(
                        url = uiState.licencia.imagenUrl,
                        label = "Imagen Licencia",
                        onClick = { focusedImage = uiState.licencia.imagenUrl }
                    )
                }
            }

            // 7. ESTADO GENERAL
            item {
                SectionCard("Estado de la Motocicleta", modifier = Modifier.testTag("section_estado")) {
                    MotoStatusSelector(
                        label = "Estado ACTUAL - GENERAL de La Motocicleta (*)",
                        selectedOption = uiState.estadoVehiculo,
                        onOptionSelected = { viewModel.onEstadoVehiculoChange(it) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 8. OBSERVACIONES
            item {
                SectionCard("Observaciones", modifier = Modifier.testTag("section_observaciones")) {
                    OutlinedTextField(
                        value = uiState.observaciones,
                        onValueChange = { viewModel.onObservacionesChange(it) },
                        label = { Text("Observaciones y/o Aspectos a Revisar (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
            }

            // 9. RESPONSABLE DE LA INSPECCIÓN
            item {
                SectionCard("Responsable", modifier = Modifier.testTag("section_responsable")) {
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
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                }
            }

            // BOTÓN GUARDAR
            item {
                Button(
                    onClick = { viewModel.onSaveClick() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("btn_guardar"),
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
                    Button(
                        onClick = { onNavigateBack(); viewModel.onNavigationDone() },
                        modifier = Modifier.testTag("btn_done_audit")
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }

        if (focusedImage != null) {
            ImageDialog(
                imageUrl = focusedImage!!,
                onDismissRequest = { focusedImage = null }
            )
        }
    }
}

// ─── COMPOSABLES REUTILIZABLES (ALINEADOS CON VEHICULOS) ─────────────────────

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DocLabelRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DocDatePickerField(
    selectedDate: String,
    onDateSelected: (Int, Int) -> Unit,
    enabled: Boolean = true
) {
    val calendar = Calendar.getInstance()
    val (initialYear, initialMonth) = remember(selectedDate) {
        if (selectedDate.contains("-")) {
            val p = selectedDate.split("-")
            Pair(
                p[0].toIntOrNull() ?: calendar.get(Calendar.YEAR),
                (p[1].toIntOrNull()?.minus(1)) ?: calendar.get(Calendar.MONTH)
            )
        } else Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog && enabled) {
        DocumentoMonthYearPickerDialog(
            onDismissRequest = { showDialog = false },
            onDateSelected = { y, m ->
                onDateSelected(y, m)
                showDialog = false
            },
            initialYear = initialYear,
            initialMonth = initialMonth
        )
    }
    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = { Text("Fecha Vencimiento", fontSize = 13.sp) },
        placeholder = { Text("AAAA-MM", fontSize = 13.sp) },
        trailingIcon = {
            IconButton(onClick = { if (enabled) showDialog = true }, enabled = enabled) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Seleccionar fecha", modifier = Modifier.size(18.dp))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { showDialog = true } else Modifier),
        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
    )
}

@Composable
fun EstadoDocumentoChip(estado: String) {
    if (estado.isBlank()) return
    val colorBueno   = Color(0xFF4CAF50)
    val colorRegular = Color(0xFFFFA000)
    val colorMalo    = Color(0xFFD32F2F)

    val (bgColor, textColor, icon) = when (estado) {
        "Vigente"          -> Triple(colorBueno.copy(alpha = 0.12f),   colorBueno,   Icons.Default.CheckCircle)
        "Próximo a Vencer" -> Triple(colorRegular.copy(alpha = 0.12f), colorRegular, Icons.Default.Warning)
        "Vencido"          -> Triple(colorMalo.copy(alpha = 0.12f),    colorMalo,    Icons.Default.Warning)
        else               -> Triple(Color.Gray.copy(alpha = 0.12f),   Color.Gray,   Icons.Default.Info)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = textColor, shape = RoundedCornerShape(8.dp))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = textColor
            )
            Spacer(Modifier.width(8.dp))
            Text(
                estado,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun DocumentImage(
    url: String?,
    label: String,
    onClick: () -> Unit = {}
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .then(if (!url.isNullOrBlank()) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (url.isNullOrBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Imagen no disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Box(Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = url,
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Indicador de que es clicable (Badge "Ver en grande")
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
            }
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
    onItemSelected: (T) -> Unit,
    dropdownTag: String = "dropdown_option"
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
                    onClick = { onItemSelected(item); expanded = false },
                    modifier = Modifier.testTag(dropdownTag)
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
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Cerrar", tint = Color.White)
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
                        Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Año anterior")
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Año siguiente")
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


