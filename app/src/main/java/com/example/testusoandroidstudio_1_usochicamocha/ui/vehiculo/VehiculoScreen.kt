package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.DocLabelRow
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.DocumentImage
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.EstadoDocumentoChip
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.SectionCard
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.InspectionSavedDialog
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.KmBlockingAlertDialog
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.KmWarningAlertDialog
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.DocumentoVencidoBlockingDialog
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.DocumentoPorVencerWarningDialog
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.SearchableSelectorField
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection.SegmentedOptionSelector

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
        InspectionSavedDialog(
            assetLabel = "vehículo",
            onDismissRequest = { viewModel.onSuccessDialogDismiss() },
            onConfirm = { viewModel.onSuccessDialogDismiss() }
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
        KmBlockingAlertDialog(
            message = uiState.kmAlertMessage,
            onDismiss = { viewModel.onCancelRedKmHighlight() }
        )
    }

    // ─ Alerta: Kilometraje con incremento alto o igual (No bloqueante) ────
    if (uiState.showKmYellowAlert) {
        KmWarningAlertDialog(
            message = uiState.kmAlertMessage,
            onDismissRequest = { viewModel.onKmYellowAlertDismiss() },
            onConfirmException = { viewModel.onConfirmKmException() },
            onCorrect = { viewModel.onCancelKmHighlight() }
        )
    }

    // ─ Alerta: Documento(s) vencido(s) (BLOQUEANTE) ─
    if (uiState.showDocumentoVencidoDialog) {
        DocumentoVencidoBlockingDialog(
            documentos = uiState.documentosVencidos,
            onDismiss = { viewModel.onDocumentoVencidoDialogDismiss() }
        )
    }

    // ─ Alerta: Documento(s) próximo(s) a vencer (No bloqueante) ─
    if (uiState.showDocumentoPorVencerDialog) {
        DocumentoPorVencerWarningDialog(
            documentos = uiState.documentosPorVencer,
            onDismiss = { viewModel.onDocumentoPorVencerDialogDismiss() }
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

@Composable
fun VehicleSelector(
    vehicles: List<VehiculoItem>,
    selectedVehicle: VehiculoItem?,
    onVehicleSelected: (VehiculoItem) -> Unit,
    syncError: String? = null
) {
    SearchableSelectorField(
        label = "Vehículo (*)",
        displayValue = selectedVehicle?.let { "${it.placa}  •  ${it.marca}" } ?: "Seleccione un vehículo (*)",
        items = vehicles,
        itemLabel = { "${it.placa}  •  ${it.marca}  •  ${it.tipoVehiculo}" },
        onItemSelected = onVehicleSelected,
        dialogTitle = "Seleccionar vehículo",
        emptyMessage = syncError ?: "No hay vehículos. Verifica la sincronización.",
        searchPlaceholder = "Buscar placa, marca..."
    )
}

@Composable
fun VehicleStatusSelector(label: String, selectedOption: String, onOptionSelected: (String) -> Unit) {
    SegmentedOptionSelector(
        label = label,
        selectedOption = selectedOption,
        onOptionSelected = onOptionSelected,
        options = listOf("Bueno" to ColorBueno, "Regular" to ColorRegular, "Malo" to ColorMalo)
    )
}

@Composable
fun YesNoSelector(label: String, selectedOption: String, onOptionSelected: (String) -> Unit) {
    SegmentedOptionSelector(
        label = label,
        selectedOption = selectedOption,
        onOptionSelected = onOptionSelected,
        options = listOf("Si" to ColorBueno, "No" to ColorMalo),
        optionFontSize = 16.sp
    )
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

