package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.DocLabelRow
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.EstadoDocumentoChip
import android.content.Intent
import android.net.Uri

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
                            label = "Placa (*)",
                            displayValue = uiState.selectedMoto?.let {
                                if (it.ubicacionBase.isNotBlank()) "${it.placa}  •  ${it.ubicacionBase}" else it.placa
                            } ?: "Seleccione una placa",
                            items = uiState.motocicletas,
                            itemLabel = {
                                if (it.ubicacionBase.isNotBlank()) "${it.placa}  •  ${it.ubicacionBase}" else it.placa
                            },
                            onItemSelected = { viewModel.onMotoSelected(it) },
                            dropdownTag = "plate_option",
                            modifier = Modifier.testTag("plate_option_dropdown")
                        )
                    }
                    if (uiState.selectedMoto != null) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "MOTOCICLETA",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            val origen = uiState.selectedMoto!!.ubicacionBase
                            if (origen.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                        Text(
                                            origen,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
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
                            label = "Unidad (*)",
                            displayValue = uiState.selectedUbicacion?.nombreUbicacion ?: "Seleccione una unidad",
                            items = uiState.ubicaciones,
                            itemLabel = { it.nombreUbicacion },
                            onItemSelected = { viewModel.onUbicacionSelected(it) },
                            dropdownTag = "unit_option",
                            modifier = Modifier.testTag("unit_option_dropdown")
                        )
                    }
                }
            }

            // 3. KILOMETRAJE
            item {
                SectionCard("Kilometraje", modifier = Modifier.testTag("section_km")) {
                    // Solo aplicar color verde al borde cuando está en rango normal (0)
                    val fieldColors = if (uiState.kmColorEstado == 0) {
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = ColorBueno,
                            unfocusedBorderColor = ColorBueno,
                            focusedLabelColor    = ColorBueno,
                            unfocusedLabelColor  = ColorBueno
                        )
                    } else {
                        OutlinedTextFieldDefaults.colors()
                    }

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
                        colors = fieldColors,
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
                    EstadoDocumentoChip(uiState.soat.estadoDoc, uiState.soat.diasRestantes)
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(
                        url = uiState.soat.imagenUrl,
                        label = "Imagen SOAT"
                    )

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // TECNO
                    DocLabelRow(label = "Revisión TECNICOMECÁNICA", icon = Icons.Default.Build)
                    Spacer(Modifier.height(4.dp))
                    EstadoDocumentoChip(uiState.revisionTecno.estadoDoc, uiState.revisionTecno.diasRestantes)
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(
                        url = uiState.revisionTecno.imagenUrl,
                        label = "Imagen Tecnomecánica"
                    )

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // LICENCIA
                    DocLabelRow(label = "Licencia de Conducción", icon = Icons.Default.AccountBox)
                    Spacer(Modifier.height(4.dp))
                    EstadoDocumentoChip(uiState.licencia.estadoDoc, uiState.licencia.diasRestantes)
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(
                        url = uiState.licencia.imagenUrl,
                        label = "Imagen Licencia"
                    )
                }
            }

            // 6. INSPECCIÓN MECÁNICA (NUEVO)
            item {
                SectionCard("Inspección Mecánica", modifier = Modifier.testTag("section_mecanica")) {
                    Text(
                        "BUENO: No requiere acción.  REGULAR: Requiere solución a corto plazo.  MALO: Requiere reparación inmediata.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    MotoStatusSelector(
                        label = "Nivel de Aceite (*)",
                        selectedOption = uiState.checkNivelAceite,
                        onOptionSelected = { viewModel.onCheckNivelAceiteChange(it) },
                        tagPrefix = "status_aceite",
                        options = listOf("Bueno", "Regular", "Malo")
                    )
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    MotoStatusSelector(
                        label = "Estado de Llantas (*)",
                        selectedOption = uiState.checkEstadoLlantas,
                        onOptionSelected = { viewModel.onCheckEstadoLlantasChange(it) },
                        tagPrefix = "status_llantas",
                        options = listOf("Bueno", "Regular", "Malo")
                    )
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
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
                        label = { Text("RESPONSABLE de la Inspección", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Campo de solo lectura",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.5.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Enviando...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Guardar Inspección", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }

        if (uiState.saveCompleted) {
            AlertDialog(
                onDismissRequest = { /* No hacer nada para forzar click en Aceptar */ },
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = { Text("¡Inspección Guardada!", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "La inspección de la motocicleta fue registrada correctamente en el sistema.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { onNavigateBack(); viewModel.onNavigationDone() },
                        modifier = Modifier.testTag("btn_done_audit")
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Aceptar")
                    }
                }
            )
        }

        // ─ Alerta: Kilometraje menor al registrado (BLOQUEANTE) ─
        if (uiState.showKmAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.onCancelRedKmHighlight() },
                icon = {
                    Icon(
                        Icons.Default.Error,
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
                                "\n\nDebe corregir el valor antes de poder guardar la inspección.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onCancelRedKmHighlight() },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorMalo)
                    ) {
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
fun DocumentImage(url: String?, label: String) {
    val context = LocalContext.current
    val isPdf = !url.isNullOrBlank() && url.trimEnd().lowercase().endsWith(".pdf")
    val isImage = !url.isNullOrBlank() && !isPdf
    var showFullscreen by remember { mutableStateOf(false) }

    if (showFullscreen && isImage) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { showFullscreen = false },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                )
                IconButton(
                    onClick = { showFullscreen = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }

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
                .height(if (isPdf) 100.dp else 180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .then(if (isImage) Modifier.clickable { showFullscreen = true } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            when {
                url.isNullOrBlank() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.HideImage,
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
                isPdf -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = ColorMalo, modifier = Modifier.size(36.dp))
                    Text("Documento PDF", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("Abrir PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
                else -> {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        error = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ErrorOutline, null, tint = ColorMalo)
                                Text("Error al cargar imagen", style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        loading = {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 2.dp)
                        }
                    )
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = "Ampliar imagen",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(24.dp)
                    )
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
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemLabel(it).contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.matchParentSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { showDialog = true })
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
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar...") },
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
                        if (items.isEmpty()) {
                            item { Text("No hay opciones disponibles", color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else if (filteredItems.isEmpty()) {
                            item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else {
                            items(filteredItems) { listItem ->
                                Column {
                                    Text(
                                        text = itemLabel(listItem),
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onItemSelected(listItem); showDialog = false; searchQuery = "" }
                                            .padding(horizontal = 8.dp, vertical = 10.dp)
                                            .testTag(dropdownTag)
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
fun MotoStatusSelector(
    label: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    tagPrefix: String = "status",
    options: List<String> = listOf("Óptimo", "Regular", "Malo")
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                val textColor = if (isSelected) Color.White else Color.Black

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("${tagPrefix}_$option")
                        .border(2.dp, if (isSelected) backgroundColor else Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onOptionSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}





