package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
    val context = LocalContext.current

    // Toast de feedback del sync
    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onSyncMessageShown()
        }
    }

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
            // Se solicita que los mensajes de feedback duren 30 segundos
            snackbarHostState.showSnackbar(
                message = it, 
                duration = SnackbarDuration.Indefinite, // Usamos Indefinite + delay manual para controlar los 30s
                actionLabel = "Cerrar"
            )
        }
    }
    
    // Timer manual para el snackbar de 30s
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            delay(30000)
            snackbarHostState.currentSnackbarData?.dismiss()
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
            onDismissRequest = { viewModel.onKmYellowAlertDismiss() },
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
                        IconButton(
                            onClick = { viewModel.onSyncClicked() },
                            enabled = !uiState.isSyncing,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = com.example.testusoandroidstudio_1_usochicamocha.ui.theme.Purple40)
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = com.example.testusoandroidstudio_1_usochicamocha.ui.theme.Purple40)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
                            }
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
                        onVehicleSelected = { viewModel.onVehicleSelected(it) },
                        syncError = uiState.vehicleSyncError
                    )
                    Spacer(Modifier.height(10.dp))

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
                        onValueChange = { if (it.all(Char::isDigit)) viewModel.onKilometrajeChange(it) },
                        label = { Text("Kilometraje Actual (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    viewModel.onKilometrajeBlur()
                                }
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        colors = fieldColors
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

            // ── 3. ELEMENTOS ─────────────────────────────────────────────────
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

            // ── 4. SALUD CONDUCTOR ────────────────────────────────────────────
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

            // ── 5. CIERRE ────────────────────────────────────────────────────
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

            // ── 6. DOCUMENTACIÓN (AL FINAL) ─────────────────────────────────────────────────
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
                        // Sin documento registrado
                    }
                    Spacer(Modifier.height(12.dp))

                    // SOAT
                    DocLabelRow(
                        label = "SOAT (Seguro Obligatorio)",
                        icon = Icons.Default.Shield
                    )
                    Spacer(Modifier.height(4.dp))
                    EstadoDocumentoChip(uiState.estadoSoat, uiState.diasRestantesSoat)
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(url = uiState.urlImagenSoat, label = "Imagen SOAT")

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // TECNO
                    DocLabelRow(
                        label = "Revisión TECNICOMECÁNICA",
                        icon = Icons.Default.Build
                    )
                    Spacer(Modifier.height(4.dp))
                    EstadoDocumentoChip(uiState.estadoTecno, uiState.diasRestantesTecno)
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(url = uiState.urlImagenTecno, label = "Imagen Tecnomecánica")

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // LICENCIA
                    DocLabelRow(
                        label = "Licencia de Conducción",
                        icon = Icons.Default.AccountBox
                    )
                    Spacer(Modifier.height(4.dp))
                    EstadoDocumentoChip(uiState.estadoLicencia, uiState.diasRestantesLicencia)
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(url = uiState.urlImagenLicencia, label = "Imagen Licencia")

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    // EXTINTOR
                    DocLabelRow(
                        label = "Extintor",
                        icon = Icons.Default.LocalFireDepartment
                    )
                    Spacer(Modifier.height(4.dp))
                    EstadoDocumentoChip(uiState.estadoExtintor, uiState.diasRestantesExtintor)
                    Spacer(Modifier.height(8.dp))
                    DocumentImage(url = uiState.urlImagenExtintor, label = "Imagen Extintor")
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
    onVehicleSelected: (VehiculoItem) -> Unit,
    syncError: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayText = selectedVehicle?.let { "${it.placa}  •  ${it.marca}" } ?: "Seleccione un vehículo (*)"
    val filteredVehicles = remember(vehicles, searchQuery) {
        if (searchQuery.isBlank()) vehicles
        else vehicles.filter {
            it.placa.contains(searchQuery, ignoreCase = true) ||
            it.marca.contains(searchQuery, ignoreCase = true) ||
            it.tipoVehiculo.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Vehículo (*)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
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
                    Text("Seleccionar vehículo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar placa, marca...") },
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
                        if (vehicles.isEmpty()) {
                            item { Text(
                                syncError ?: "No hay vehículos. Verifica la sincronización.",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                            ) }
                        } else if (filteredVehicles.isEmpty()) {
                            item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else {
                            items(filteredVehicles) { v ->
                                Column {
                                    Text(
                                        text = "${v.placa}  •  ${v.marca}  •  ${v.tipoVehiculo}",
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onVehicleSelected(v); showDialog = false; searchQuery = "" }
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
 * Si el archivo es PDF, muestra un acceso directo para abrirlo.
 * Si es imagen, al hacer tap se abre a pantalla completa para mejor visualización.
 * Si no hay URL o falla la carga, muestra "Imagen no disponible".
 */
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
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
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = ColorMalo,
                        modifier = Modifier.size(36.dp)
                    )
                    Text("Documento PDF", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("Abrir PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
                else -> {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
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
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = "Ampliar imagen",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
            }
        }
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
                "$estado $leyenda",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

