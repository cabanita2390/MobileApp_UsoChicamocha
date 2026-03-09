package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Calendar
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector

// ─── COLORES COMPARTIDOS ─────────────────────────────────────────────────────
private val ColorBueno   = Color(0xFF4CAF50)
private val ColorRegular = Color(0xFFFFA000)
private val ColorMalo    = Color(0xFFD32F2F)

// ─── SCREEN ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculoScreen(
    networkStatus: Boolean,
    viewModel: VehiculoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) { onNavigateBack(); viewModel.onNavigationDone() }
    }

    // 2do "No" en cierre → salida automática
    LaunchedEffect(uiState.shouldExitForm) {
        if (uiState.shouldExitForm) { viewModel.onExitFormHandled(); onNavigateBack() }
    }

    // Snackbar para errores del backend
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            viewModel.onErrorDismissed()
        }
    }

    // ─ Diálogo de éxito  ──────────────────────────────────────
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onSuccessDialogDismiss() },
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
                    "La inspección del vehículo fue registrada correctamente en el sistema.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.onSuccessDialogDismiss() }) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Aceptar")
                }
            }
        )
    }

    // ─ Alerta: Consciente de Responsabilidad ──────────────────────
    if (uiState.showConscienteAlert) {
        val isSecondTime = uiState.conscienteNoCount >= 2
        AlertDialog(
            onDismissRequest = { if (!isSecondTime) viewModel.onConscienteAlertDismiss() },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    if (isSecondTime) "¡Última en advertencia!" else "¡Atención!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Text(
                    if (isSecondTime)
                        "Ha confirmado nuevamente que NO es consciente de su responsabilidad.\n\nEstá siendo redirigido al menú principal."
                    else
                        "Ha indicado que NO es consciente de su responsabilidad al operar este vehículo.\n\nSi vuelve a seleccionar NO saldrá automáticamente al menú principal.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConscienteAlertDismiss(); onNavigateBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.ExitToApp, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Volver al menú")
                }
            },
            dismissButton = if (!isSecondTime) {{
                OutlinedButton(onClick = { viewModel.onConscienteAlertDismiss() }) {
                    Text("Cancelar")
                }
            }} else null
        )
    }

    // ─ Alerta: Vehículo Aprobado para Ruta ───────────────────────
    if (uiState.showAprobadoAlert) {
        val isSecondTime = uiState.aprobadoNoCount >= 2
        AlertDialog(
            onDismissRequest = { if (!isSecondTime) viewModel.onAprobadoAlertDismiss() },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    if (isSecondTime) "¡Vehículo Rechazado!" else "¡Vehículo No Aprobado!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Text(
                    if (isSecondTime)
                        "Ha confirmado nuevamente que el vehículo NO está aprobado para ruta.\n\nEstá siendo redirigido al menú principal."
                    else
                        "Ha indicado que el vehículo NO está aprobado para salir a ruta.\n\nSi vuelve a seleccionar NO saldrá automáticamente al menú principal.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onAprobadoAlertDismiss(); onNavigateBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.ExitToApp, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Volver al menú")
                }
            },
            dismissButton = if (!isSecondTime) {{
                OutlinedButton(onClick = { viewModel.onAprobadoAlertDismiss() }) {
                    Text("Cancelar")
                }
            }} else null
        )
    }

    // ─ Alerta: Kilometraje menor al registrado (bloqueante) ───────────────
    if (uiState.showKmAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.onKmAlertDismiss() },
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
                        "\n\nCorrige el kilometraje para poder guardar la inspección.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onKmAlertDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorMalo)
                ) {
                    Text("Corregir")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                ConnectionStatusTopBar(isConnected = networkStatus)
                TopAppBar(
                    title = { Text("Inspección del Vehículo") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onSyncClicked() }) {
                            Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {


            // ── 1. DATOS GENERALES ────────────────────────────────────────────
            item {
                SectionCard("Datos Generales") {
                    // Selector de vehículo desde el catálogo
                    VehicleSelector(
                        vehicles = uiState.vehicles,
                        selectedVehicle = uiState.selectedVehicle,
                        onVehicleSelected = { viewModel.onVehicleSelected(it) }
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = uiState.kilometraje,
                        onValueChange = { if (it.all(Char::isDigit)) viewModel.onKilometrajeChange(it) },
                        label = { Text("Kilometraje Actual (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // ── 2. INSPECCIÓN MECÁNICA ────────────────────────────────────────
            item {
                SectionCard("Inspección del Vehículo") {
                    Text(
                        "BUENO: No requiere acción.  REGULAR: Requiere solución a corto plazo.  MALO: Requiere reparación inmediata.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    val mecItems = listOf(
                        "Aceite"       to "Nivel de ACEITE (*)",
                        "Refrigerante" to "Nivel de REFRIGERANTE (*)",
                        "Frenos"       to "Nivel de LÍQUIDO DE FRENOS (*)",
                        "Llantas"      to "Estado de LLANTAS (General y Aire) (*)",
                        "Luces"        to "Funcionamiento General de LUCES (*)",
                        "Visual"       to "VISUAL (Pintura, Golpes, Rayones) (*)",
                        "Limpieza"     to "Limpieza General (*)"
                    )
                    val mecValues = mapOf(
                        "Aceite"       to uiState.nivelAceite,
                        "Refrigerante" to uiState.nivelRefrigerante,
                        "Frenos"       to uiState.nivelFrenos,
                        "Llantas"      to uiState.estadoLlantas,
                        "Luces"        to uiState.lucesGeneral,
                        "Visual"       to uiState.estadoVisual,
                        "Limpieza"     to uiState.limpiezaGeneral
                    )
                    mecItems.forEachIndexed { i, (key, label) ->
                        VehicleStatusSelector(label, mecValues[key] ?: "") { viewModel.onMecanicoChange(key, it) }
                        if (i < mecItems.lastIndex) { Divider(Modifier.padding(vertical = 10.dp)) }
                    }
                }
            }

            // ── 3. DOCUMENTACIÓN ─────────────────────────────────────────────
            item {
                SectionCard("Vigencia Documentación y Elementos") {
                    val hayVehiculo = uiState.selectedVehicle != null

                    if (uiState.isLoadingDocs) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Consultando documentos...", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (!hayVehiculo) {
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
                                Icon(Icons.Default.Info, null, Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "Seleccione un vehículo para cargar los documentos registrados.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    } else {
                        Text(
                            "Ingrese la fecha de vencimiento que aparece en el documento físico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // SOAT
                    DocLabelRow(
                        label = "SOAT (Seguro Obligatorio)",
                        icon = Icons.Default.Shield
                    )
                    if (uiState.fechaVencSoatDB.isNotBlank()) {
                        Text("📅 Registrado en sistema: ${uiState.fechaVencSoatDB}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    DocDatePickerField(
                        doc = "SOAT",
                        selectedDate = uiState.fechaVencSoat,
                        onDateSelected = { viewModel.onDocFechaVencChange("SOAT", it) },
                        enabled = hayVehiculo
                    )
                    if (uiState.estadoSoat.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        EstadoDocumentoChip(uiState.estadoSoat)
                    }
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(url = uiState.urlImagenSoat, label = "Imagen SOAT")

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // TECNO
                    DocLabelRow(
                        label = "Revisión TECNICOMECÁNICA",
                        icon = Icons.Default.Build
                    )
                    if (uiState.fechaVencTecnoDB.isNotBlank()) {
                        Text("📅 Registrado en sistema: ${uiState.fechaVencTecnoDB}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    DocDatePickerField(
                        doc = "Tecno",
                        selectedDate = uiState.fechaVencTecno,
                        onDateSelected = { viewModel.onDocFechaVencChange("Tecno", it) },
                        enabled = hayVehiculo
                    )
                    if (uiState.estadoTecno.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        EstadoDocumentoChip(uiState.estadoTecno)
                    }
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(url = uiState.urlImagenTecno, label = "Imagen Tecnomecánica")

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // LICENCIA
                    DocLabelRow(
                        label = "Licencia de Conducción",
                        icon = Icons.Default.AccountBox
                    )
                    if (uiState.fechaVencLicencioDB.isNotBlank()) {
                        Text("📅 Registrado en sistema: ${uiState.fechaVencLicencioDB}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    DocDatePickerField(
                        doc = "Licencia",
                        selectedDate = uiState.fechaVencLicencia,
                        onDateSelected = { viewModel.onDocFechaVencChange("Licencia", it) },
                        enabled = hayVehiculo
                    )
                    if (uiState.estadoLicencia.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        EstadoDocumentoChip(uiState.estadoLicencia)
                    }
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(url = uiState.urlImagenLicencia, label = "Imagen Licencia")

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // EXTINTOR
                    DocLabelRow(
                        label = "Extintor",
                        icon = Icons.Default.LocalFireDepartment
                    )
                    if (uiState.vigenciaExtintorDB.isNotBlank()) {
                        Text("📅 Registrado en sistema: ${uiState.vigenciaExtintorDB}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    ExtintorDatePickerField(
                        selectedDate = uiState.vigenciaExtintor,
                        onDateSelected = { y, m -> viewModel.onExtintorDateChange(y, m) },
                        enabled = hayVehiculo
                    )
                    if (uiState.estadoExtintor.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        EstadoDocumentoChip(uiState.estadoExtintor)
                    }
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(url = uiState.urlImagenExtintor, label = "Imagen Extintor")
                }
            }

            // ── 4. ELEMENTOS ─────────────────────────────────────────────────
            item {
                SectionCard("Existencia de Elementos") {
                    Text(
                        "Indique si el vehículo cuenta con los siguientes elementos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    val elemItems = listOf(
                        "Botiquin"         to ("Botiquín de Primeros Auxilios (*)" to uiState.tieneBotiquin),
                        "Señalizacion"     to ("Señalización (CONOS) (*)"          to uiState.tieneSeñalizacion),
                        "LineasEmergencia" to ("Líneas de Emergencia (*)"           to uiState.tieneLineasEmergencia),
                        "LlantaRepuesto"   to ("Llanta de REPUESTO (*)"            to uiState.tieneLlantaRepuesto),
                        "GatoHidraulico"   to ("Gato Hidráulico / Cruceta (*)"     to uiState.tieneGatoHidraulico)
                    )
                    elemItems.forEachIndexed { i, (key, pair) ->
                        YesNoSelector(pair.first, pair.second) { viewModel.onElementoChange(key, it) }
                        if (i < elemItems.lastIndex) Divider(Modifier.padding(vertical = 10.dp))
                    }
                }
            }

            // ── 5. SALUD CONDUCTOR ────────────────────────────────────────────
            item {
                SectionCard("Condiciones de Salud del Conductor") {
                    Text(
                        "Cumplimiento de condiciones de salud para conducción:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    val saludItems = listOf(
                        "SaludFisica"           to ("Salud FÍSICA (Sin Enfermedad o Fatiga) (*)"                                      to uiState.saludFisica),
                        "SaludMental"           to ("Salud MENTAL (Sin Preocupaciones o Altercados) (*)"                               to uiState.saludMental),
                        "Sobrio"                to ("SOBRIO (Sin Efectos de Alcohol o Enguayabado) (*)"                                to uiState.sobrio),
                        "Medicamentos"          to ("MEDICAMENTOS (¿Consume algún tipo que afecte los sentidos?) (*)"                  to uiState.medicamentos),
                        "CondicionParaConducir" to ("¿Se siente en CONDICIONES para Conducir? (*)"                                    to uiState.condicionParaConducir)
                    )
                    saludItems.forEachIndexed { i, (key, pair) ->
                        YesNoSelector(pair.first, pair.second) { viewModel.onSaludChange(key, it) }
                        if (i < saludItems.lastIndex) Divider(Modifier.padding(vertical = 10.dp))
                    }
                }
            }

            // ── 6. CIERRE ────────────────────────────────────────────────────
            item {
                SectionCard("Cierre de Inspección") {
                    YesNoSelector(
                        label = "¿Estoy CONSCIENTE de la RESPONSABILIDAD de operar este vehículo sin poner en riesgo mi integridad y la de los demás? (*)",
                        selectedOption = uiState.conscienteResponsabilidad,
                        onOptionSelected = { viewModel.onConscienteChange(it) }
                    )
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    YesNoSelector(
                        label = "¿Considera usted que el vehículo es APROBADO para salir a ruta? (*)",
                        selectedOption = uiState.aprobadoRuta,
                        onOptionSelected = { viewModel.onAprobadoRutaChange(it) }
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.observaciones,
                        onValueChange = { viewModel.onObservacionesChange(it) },
                        label = { Text("Observaciones y/o Aspectos a Revisar", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        placeholder = { Text("Describa brevemente la novedad encontrada...") },
                        modifier = Modifier.fillMaxWidth(), minLines = 4
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.responsableInspeccion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("RESPONSABLE de la Inspección", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        placeholder = { Text("Cargando usuario...") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Campo de solo lectura",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            }

            // ── BOTÓN GUARDAR ─────────────────────────────────────────────────
            item {
                Button(
                    onClick = { viewModel.onSaveClick() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isSaveButtonEnabled && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Enviando...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Guardar Inspección", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}


// ─── COMPOSABLES REUTILIZABLES ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelector(
    vehicles: List<VehiculoItem>,
    selectedVehicle: VehiculoItem?,
    onVehicleSelected: (VehiculoItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedVehicle?.let { "${it.placa}  ${it.marca}" } ?: "Seleccione un vehículo (*)"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Vehículo (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (vehicles.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay vehículos. Verifica la sincronización.", color = MaterialTheme.colorScheme.error) },
                    onClick = { expanded = false }
                )
            } else {
                vehicles.forEach { v ->
                    DropdownMenuItem(
                        text = { Text("${v.placa}  •  ${v.marca}  •  ${v.tipoVehiculo}") },
                        onClick = { onVehicleSelected(v); expanded = false }
                    )
                }
            }
        }
    }
}

/**
 * Muestra el nombre del documento con un ícono representativo.
 * No permite subir fotos — solo identifica visualmente el tipo de documento.
 */
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

    doc: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
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
        VehiculoMonthYearDialog(
            onDismiss = { showDialog = false },
            onDateSelected = { y, m ->
                onDateSelected("$y-${String.format("%02d", m + 1)}")
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
                Icon(Icons.Default.DateRange, "Seleccionar fecha", modifier = Modifier.size(18.dp))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { showDialog = true } else Modifier),
        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
    )
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun VehicleStatusSelector(label: String, selectedOption: String, onOptionSelected: (String) -> Unit) {
    val options = listOf("Bueno" to ColorBueno, "Regular" to ColorRegular, "Malo" to ColorMalo)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (option, bgColor) ->
                val isSelected = option == selectedOption
                Surface(
                    modifier = Modifier.weight(1f).height(48.dp)
                        .border(2.dp, if (isSelected) bgColor else Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onOptionSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) bgColor else bgColor.copy(alpha = 0.15f)
                ) {
                    Box(Modifier.background(Color.Transparent), contentAlignment = Alignment.Center) {
                        Text(option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else Color.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun YesNoSelector(label: String, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Si" to ColorBueno, "No" to ColorMalo).forEach { (option, bgColor) ->
                val isSelected = option == selectedOption
                Surface(
                    modifier = Modifier.weight(1f).height(48.dp)
                        .border(2.dp, if (isSelected) bgColor else Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onOptionSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) bgColor else bgColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else Color.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}


/**
 * Muestra la fecha de vencimiento del backend como referencia (solo lectura)
 * y 3 botones para que el inspector confirme el estado del documento físico.
 */
@Composable
fun DocEstadoSelector(
    fechaVenc: String,
    estadoActual: String,
    habilitado: Boolean,
    onEstadoSelected: (String) -> Unit
) {
    val opciones = listOf(
        "Vigente"          to ColorBueno,
        "Próximo a Vencer" to ColorRegular,
        "Vencido"          to ColorMalo
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Fecha de referencia (solo lectura, viene del backend)
        if (fechaVenc.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Vence: $fechaVenc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Botones de estado — el inspector confirma visualmente
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            opciones.forEach { (opcion, bgColor) ->
                val isSelected = opcion == estadoActual
                val displayLabel = if (opcion == "Próximo a Vencer") "Próximo\na Vencer" else opcion
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(2.dp, if (isSelected) bgColor else Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable(enabled = habilitado) { onEstadoSelected(opcion) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) bgColor else if (habilitado) bgColor.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.2f)
                ) {
                    Box(Modifier.padding(4.dp), contentAlignment = Alignment.Center) {
                        Text(
                            displayLabel,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else if (habilitado) Color.Black else Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Muestra la imagen del documento desde una URL.
 * Si no hay URL o falla la carga, muestra "Imagen no disponible".
 */
@Composable
fun DocumentImage(url: String?, label: String) {
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
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (url.isNullOrBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            } else {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
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
            }
        }
    }
}


@Composable
fun EstadoDocumentoChip(estado: String) {
    if (estado.isBlank()) return
    val (bgColor, textColor, icon) = when (estado) {
        "Vigente"          -> Triple(ColorBueno.copy(alpha = 0.12f),   ColorBueno,   Icons.Default.CheckCircle)
        "Próximo a Vencer" -> Triple(ColorRegular.copy(alpha = 0.12f), ColorRegular, Icons.Default.Warning)
        "Vencido"          -> Triple(ColorMalo.copy(alpha = 0.12f),    ColorMalo,    Icons.Default.Cancel)
        else               -> Triple(Color.Gray.copy(alpha = 0.12f),   Color.Gray,   Icons.Default.Info)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, textColor, RoundedCornerShape(8.dp))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = textColor)
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
fun ExtintorDatePickerField(
    selectedDate: String,
    onDateSelected: (Int, Int) -> Unit,
    enabled: Boolean = true
) {
    val calendar = Calendar.getInstance()
    val (initialYear, initialMonth) = remember(selectedDate) {
        if (selectedDate.contains("-")) {
            val p = selectedDate.split("-")
            Pair(p[0].toIntOrNull() ?: calendar.get(Calendar.YEAR), (p[1].toIntOrNull()?.minus(1)) ?: calendar.get(Calendar.MONTH))
        } else Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog && enabled) {
        VehiculoMonthYearDialog(
            onDismiss = { showDialog = false },
            onDateSelected = { y, m -> onDateSelected(y, m); showDialog = false },
            initialYear = initialYear, initialMonth = initialMonth
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Vigencia EXTINTOR (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        OutlinedTextField(
            value = selectedDate, onValueChange = {}, readOnly = true, enabled = enabled,
            label = { Text("Fecha Vencimiento (YYYY-MM)") },
            trailingIcon = {
                IconButton(onClick = { if (enabled) showDialog = true }, enabled = enabled) {
                    Icon(Icons.Default.DateRange, "Fecha")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (enabled) Modifier.clickable { showDialog = true } else Modifier)
        )
    }
}

@Composable
fun VehiculoMonthYearDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Int, Int) -> Unit,
    initialYear: Int,
    initialMonth: Int
) {
    // Usamos estado interno para el año y mes SELECCIONADO en el diálogo
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonthIdx by remember { mutableStateOf(initialMonth) }
    val months = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
    // Filas de meses: 4 filas x 3 columnas
    val monthRows = listOf(listOf(0,1,2), listOf(3,4,5), listOf(6,7,8), listOf(9,10,11))

    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.width(360.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Selector de año
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedYear-- }) { Icon(Icons.Default.KeyboardArrowLeft, "Anterior") }
                    Text(selectedYear.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { selectedYear++ }) { Icon(Icons.Default.KeyboardArrowRight, "Siguiente") }
                }
                Spacer(Modifier.height(16.dp))
                // Grid de meses usando Column+Row (confiable dentro de Dialog)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    monthRows.forEach { row ->
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                            row.forEach { idx ->
                                val isSelected = selectedYear == initialYear && idx == selectedMonthIdx
                                OutlinedButton(
                                    onClick = {
                                        selectedMonthIdx = idx
                                        onDateSelected(selectedYear, idx)
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = if (isSelected)
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    else ButtonDefaults.outlinedButtonColors()
                                ) { Text(months[idx], fontSize = 13.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}
