package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion

// ─── COLORES COMPARTIDOS ─────────────────────────────────────────────────────
private val ColorBueno   = Color(0xFF4CAF50)
private val ColorRegular = Color(0xFFFFA000)
private val ColorMalo    = Color(0xFFD32F2F)

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
                SectionCard("PLACA", modifier = Modifier.testTag("section_placa")) {
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
                            dropdownTag = "plate_option",
                            modifier = Modifier.testTag("plate_option_dropdown")
                        )
                    }
                }
            }

            // 2. UBICACIÓN
            item {
                SectionCard("Ubicación", modifier = Modifier.testTag("section_ubicacion")) {
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
            }

            // 3. KILOMETRAJE
            item {
                SectionCard("Kilometraje", modifier = Modifier.testTag("section_km")) {
                    OutlinedTextField(
                        value = uiState.kilometraje,
                        onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.onKilometrajeChange(it) },
                        label = { Text("Escriba el KILOMETRAJE Actual de la Moto (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    viewModel.onKilometrajeBlur()
                                }
                            }
                            .testTag("moto_km_field"),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        isError = uiState.kilometrajeError != null,
                        supportingText = {
                            if (uiState.kilometrajeError != null) {
                                Text(uiState.kilometrajeError ?: "", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }

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
                    Spacer(Modifier.height(4.dp))
                    if (uiState.soat.estadoDoc.isNotBlank()) {
                        EstadoDocumentoChip(uiState.soat.estadoDoc, uiState.soat.diasRestantes)
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
                    Spacer(Modifier.height(4.dp))
                    if (uiState.revisionTecno.estadoDoc.isNotBlank()) {
                        EstadoDocumentoChip(uiState.revisionTecno.estadoDoc, uiState.revisionTecno.diasRestantes)
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
                    Spacer(Modifier.height(4.dp))
                    if (uiState.licencia.estadoDoc.isNotBlank()) {
                        EstadoDocumentoChip(uiState.licencia.estadoDoc, uiState.licencia.diasRestantes)
                    }
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(
                        url = uiState.licencia.imagenUrl,
                        label = "Imagen Licencia",
                        onClick = { focusedImage = uiState.licencia.imagenUrl }
                    )
                }
            }

            // 6. INSPECCIÓN MECÁNICA (NUEVO)
            item {
                SectionCard("Inspección Mecánica", modifier = Modifier.testTag("section_mecanica")) {
                    MotoStatusSelector(
                        label = "Nivel de Aceite (*)",
                        selectedOption = uiState.checkNivelAceite,
                        onOptionSelected = { viewModel.onCheckNivelAceiteChange(it) },
                        tagPrefix = "status_aceite",
                        options = listOf("Bueno", "Regular", "Malo")
                    )
                    Spacer(Modifier.height(12.dp))
                    MotoStatusSelector(
                        label = "Estado de Llantas (*)",
                        selectedOption = uiState.checkEstadoLlantas,
                        onOptionSelected = { viewModel.onCheckEstadoLlantasChange(it) },
                        tagPrefix = "status_llantas",
                        options = listOf("Bueno", "Regular", "Malo")
                    )
                    Spacer(Modifier.height(12.dp))
                    MotoStatusSelector(
                        label = "Estado de Luces (*)",
                        selectedOption = uiState.checkEstadoLuces,
                        onOptionSelected = { viewModel.onCheckEstadoLucesChange(it) },
                        tagPrefix = "status_luces",
                        options = listOf("Bueno", "Regular", "Malo")
                    )
                }
            }

            // 7. ESTADO GENERAL
            item {
                SectionCard("Estado de la Motocicleta", modifier = Modifier.testTag("section_estado")) {
                    MotoStatusSelector(
                        label = "Estado ACTUAL - GENERAL de La Motocicleta (*)",
                        selectedOption = uiState.estadoVehiculo,
                        onOptionSelected = { viewModel.onEstadoVehiculoChange(it) },
                        tagPrefix = "status_general"
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
                title = { Text("¡Inspección Guardada!", fontWeight = FontWeight.Bold) },
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

        // ─ Alerta: Kilometraje menor al registrado (No bloqueante tras confirmar) ─
        if (uiState.showKmAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.onCancelRedKmHighlight() },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = ColorMalo,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        "Kilometraje Incorrecto",
                        fontWeight = FontWeight.Bold,
                        color = ColorMalo
                    )
                },
                text = {
                    Text(
                        uiState.kmAlertMessage +
                                "\n\nPor favor, verifica si el número es correcto o corrígelo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onConfirmRedKmException() },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorMalo)
                    ) {
                        Text("Confirmar Excepción")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.onCancelRedKmHighlight() }) {
                        Text("Corregir")
                    }
                }
            )
        }

        // ─ Alerta: Kilometraje con incremento alto o igual (No bloqueante) ────
        if (uiState.showKmYellowAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.onCancelKmHighlight() },
                icon = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = ColorRegular,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        "Verificación de Kilometraje",
                        fontWeight = FontWeight.Bold,
                        color = ColorRegular
                    )
                },
                text = {
                    Text(
                        uiState.kmAlertMessage +
                                "\n\nPor favor, verifica si el número es correcto o corrígelo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onConfirmKmException() },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorRegular)
                    ) {
                        Text("Confirmar Excepción")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.onCancelKmHighlight() }) {
                        Text("Corregir")
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
fun EstadoDocumentoChip(estado: String, diasRestantes: Long = 0L) {
    if (estado.isBlank()) return
    val (bgColor, textColor, icon) = when (estado) {
        "Vigente"          -> Triple(ColorBueno.copy(alpha = 0.12f),   ColorBueno,   Icons.Default.CheckCircle)
        "Próximo a Vencer" -> Triple(ColorRegular.copy(alpha = 0.12f), ColorRegular, Icons.Default.Warning)
        "Vencido"          -> Triple(ColorMalo.copy(alpha = 0.12f),    ColorMalo,    Icons.Default.Cancel)
        else               -> Triple(Color.Gray.copy(alpha = 0.12f),   Color.Gray,   Icons.Default.Info)
    }
    // Leyenda de días según el estado
    val leyenda = when (estado) {
        "Vigente"          -> "— quedan $diasRestantes días"
        "Próximo a Vencer" -> "— quedan $diasRestantes días"
        "Vencido"          -> "— venció hace ${-diasRestantes} días"
        else               -> ""
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
                "$estado $leyenda",
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
    items: List<String> = listOf("Bueno", "Regular", "Malo"),
    dropdownTag: String = "dropdown_option"
) {
    DropdownField(
        label = label,
        displayValue = value.ifBlank { "Seleccione Una Opción" },
        items = items,
        itemLabel = { it },
        onItemSelected = onSelect,
        dropdownTag = dropdownTag
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
    dropdownTag: String = "dropdown_option",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = modifier.menuAnchor().fillMaxWidth()
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
    onOptionSelected: (String) -> Unit,
    tagPrefix: String = "status",
    options: List<String> = listOf("Óptimo", "Regular", "Malo")
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                val backgroundColor = when (option) {
                    "Óptimo", "Bueno" -> Color(0xFF4CAF50)
                    "Regular" -> Color(0xFFFFA000)
                    else -> Color(0xFFD32F2F)
                }
                val textColor = if (isSelected) Color.White
                    else Color.Black

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("${tagPrefix}_$option")
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




