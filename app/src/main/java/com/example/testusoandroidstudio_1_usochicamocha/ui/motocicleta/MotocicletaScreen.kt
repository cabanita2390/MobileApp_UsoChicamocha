package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import androidx.compose.foundation.background
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
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
                TopAppBar(
                    title = { Text("Inspección Motocicleta") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar(isConnected = networkStatus)
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
                        SearchableSelectorField(
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
                        SearchableSelectorField(
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

            // 4. INSPECCIÓN MECÁNICA
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

            // ─── 10. DOCUMENTACIÓN (AL FINAL) ───────────────────────────────────────────
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
            InspectionSavedDialog(
                assetLabel = "motocicleta",
                onDismissRequest = { /* No hacer nada para forzar click en Aceptar */ },
                onConfirm = { onNavigateBack(); viewModel.onNavigationDone() },
                confirmButtonModifier = Modifier.testTag("btn_done_audit")
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
                onDismissRequest = { viewModel.onCancelKmHighlight() },
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
    val coloredOptions = options.map { option ->
        option to when (option) {
            "Óptimo", "Bueno" -> Color(0xFF4CAF50)
            "Regular" -> Color(0xFFFFA000)
            else -> Color(0xFFD32F2F)
        }
    }
    SegmentedOptionSelector(
        label = label,
        selectedOption = selectedOption,
        onOptionSelected = onOptionSelected,
        options = coloredOptions,
        tagPrefix = tagPrefix
    )
}





