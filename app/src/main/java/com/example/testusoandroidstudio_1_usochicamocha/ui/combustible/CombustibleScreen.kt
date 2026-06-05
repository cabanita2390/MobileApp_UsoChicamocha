package com.example.testusoandroidstudio_1_usochicamocha.ui.combustible

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoItem
import com.example.testusoandroidstudio_1_usochicamocha.util.ImageUtils
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val fuelOptions = listOf(
    "DIESEL" to "Diesel",
    "ACPM" to "ACPM",
    "GASOLINA_CORRIENTE" to "Gasolina Corriente",
    "GASOLINA_EXTRA" to "Gasolina Extra",
    "GAS_NATURAL" to "Gas Natural"
)


/** Solo dígitos y un punto decimal. Evita "12.3.4" → "12.34". */
private fun String.asDecimal(): String {
    val clean = filter { it.isDigit() || it == '.' }
    val dot = clean.indexOf('.')
    return if (dot == -1) clean
    else clean.take(dot + 1) + clean.drop(dot + 1).filter { it.isDigit() }
}

/** Mayúsculas y solo alfanuméricos + guion (para códigos de factura). */
private fun String.asVoucherCode(): String =
    uppercase().filter { it.isLetterOrDigit() || it == '-' || it == '/' }

/** Primera letra de cada palabra en mayúscula. */
private fun String.asTitleCase(): String =
    split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercaseChar() }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombustibleScreen(
    onNavigateBack: () -> Unit,
    viewModel: CombustibleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // ── Foto de factura ───────────────────────────────────────────────────────
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val compressed = ImageUtils.compressAndSaveImage(context, it)
            viewModel.setInvoicePhotoPath(compressed?.path ?: it.path)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempCameraUri?.let { viewModel.setInvoicePhotoPath(it.path) }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "invoice_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            Toast.makeText(context, "Carga guardada. Se sincronizará cuando haya conexión.", Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }

    val isMachine = uiState.assetType == "MACHINE"
    val isGas = uiState.fuelType == "GAS_NATURAL"
    val unitLabel = when {
        isGas -> "m³"
        uiState.quantityUnit == "LITERS" -> "L"
        else -> "Gal"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Registrar Combustible", fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    Icon(
                        Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp).size(26.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── 1. Tipo de activo ─────────────────────────────────────────────
            AssetTypeRow(selected = uiState.assetType, onSelect = viewModel::setAssetType)

            // ── 2. Activo específico ──────────────────────────────────────────
            SectionCard(title = "Activo", icon = Icons.Default.Inventory2) {
                when (uiState.assetType) {
                    "MACHINE" -> MachineAssetSelector(uiState.machines, uiState.selectedMachine, viewModel::onMachineSelected)
                    "VEHICLE" -> VehiculoAssetSelector(uiState.vehiculos, uiState.selectedVehiculo, viewModel::onVehiculoSelected)
                    else      -> MotoAssetSelector(uiState.motos, uiState.selectedMoto, viewModel::onMotoSelected)
                }
            }

            // ── 3. Fecha y medidor ────────────────────────────────────────────
            SectionCard(title = "Fecha y medidor", icon = Icons.Default.AccessTime) {
                val focusMeter = LocalFocusManager.current
                DateTimePickerField(
                    value = uiState.fuelDateTime,
                    onValueChange = viewModel::setFuelDateTime
                )
                if (isMachine) {
                    OutlinedTextField(
                        value = uiState.hourMeter,
                        onValueChange = { viewModel.setHourMeter(it.asDecimal()) },
                        label = { Text("Lectura del horómetro *") },
                        supportingText = { Text("Contador de horas de operación de la máquina") },
                        leadingIcon = { Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp)) },
                        suffix = { Text("h") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusMeter.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.odometerKm,
                        onValueChange = { viewModel.setOdometerKm(it.asDecimal()) },
                        label = { Text("Lectura del odómetro *") },
                        supportingText = { Text("Kilometraje actual marcado en el tablero") },
                        leadingIcon = { Icon(Icons.Default.Speed, null, modifier = Modifier.size(18.dp)) },
                        suffix = { Text("km") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusMeter.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── 4. Combustible ────────────────────────────────────────────────
            SectionCard(title = "Combustible", icon = Icons.Default.LocalGasStation) {
                // Tipo + Unidad en la misma fila
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    FuelTypeDropdown(
                        selected = uiState.fuelType,
                        onSelect = viewModel::setFuelType,
                        modifier = Modifier.weight(1.6f)
                    )
                    UnitDropdown(
                        selected = uiState.quantityUnit,
                        onSelect = viewModel::setQuantityUnit,
                        enabled = !isGas,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Cantidad + Precio en la misma fila
                val focusFuel = LocalFocusManager.current
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.quantity,
                        onValueChange = { viewModel.setQuantity(it.asDecimal()) },
                        label = { Text("Cantidad *") },
                        suffix = { Text(unitLabel) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusFuel.moveFocus(FocusDirection.Right) }),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.pricePerUnit,
                        onValueChange = { viewModel.setPricePerUnit(it.asDecimal()) },
                        label = { Text("Precio / $unitLabel *") },
                        prefix = { Text("$") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusFuel.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── 5. Costos ─────────────────────────────────────────────────────
            CostCard(
                totalActual = uiState.totalCostActual,
                hasMismatch = uiState.totalCostMismatch,
                discountAmount = uiState.discountAmount,
                onTotalActualChange = viewModel::setTotalCostActual,
                onDiscountChange = viewModel::setDiscountAmount
            )

            // ── 6. Datos adicionales ──────────────────────────────────────────
            SectionCard(title = "Datos adicionales", icon = Icons.Default.MoreHoriz) {
                // Selector de bomba / estación (catálogo sincronizado desde el servidor)
                StationDropdown(
                    selected = uiState.serviceStation,
                    stations = uiState.stations.map { it.name },
                    onSelect = viewModel::setServiceStation
                )
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::setNotes,
                    label = { Text("Notas (opcional)") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    ),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── 7. Foto de factura ────────────────────────────────────────────
            InvoicePhotoCard(
                photoPath = uiState.invoicePhotoPath,
                onPickGallery = { galleryLauncher.launch("image/*") },
                onTakePhoto = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                onRemove = { viewModel.setInvoicePhotoPath(null) }
            )

            // ── Error ─────────────────────────────────────────────────────────
            AnimatedVisibility(visible = uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Text(uiState.error ?: "", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ── Guardar ───────────────────────────────────────────────────────
            Button(
                onClick = viewModel::submitFuelLog,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(10.dp))
                }
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar carga de combustible", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Selector de tipo de activo ────────────────────────────────────────────────

@Composable
private fun AssetTypeRow(selected: String, onSelect: (String) -> Unit) {
    val types = listOf(
        Triple("MACHINE", "Maquinaria", Icons.Default.Construction),
        Triple("VEHICLE", "Vehículo", Icons.Default.DirectionsCar),
        Triple("MOTO", "Moto", Icons.Default.TwoWheeler)
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        types.forEach { (value, label, icon) ->
            val isSelected = selected == value
            ElevatedCard(
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = if (isSelected) 4.dp else 1.dp
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        icon, null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Dropdowns ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuelTypeDropdown(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val label = fuelOptions.firstOrNull { it.first == selected }?.second ?: selected

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            fuelOptions.forEach { (value, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(value); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    trailingIcon = if (value == selected) {{ Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }} else null
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val options = listOf("LITERS" to "Litros", "GALLONS" to "Galones", "CUBIC_METERS" to "m³")
    var expanded by remember { mutableStateOf(false) }
    val label = options.firstOrNull { it.first == selected }?.second ?: selected

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Unidad") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            options.filter { it.first != "CUBIC_METERS" }.forEach { (value, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(value); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    trailingIcon = if (value == selected) {{ Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }} else null
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDropdown(
    selected: String,
    stations: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val allOptions = stations + "Otra"
    var showCustomInput by remember { mutableStateOf(selected.isNotBlank() && selected !in stations) }
    var customValue by remember { mutableStateOf(if (selected !in stations) selected else "") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected.ifBlank { "Seleccionar bomba" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Bomba / Estación") },
                leadingIcon = { Icon(Icons.Default.LocalGasStation, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (allOptions.size == 1) {
                    // Solo "Otra" — no hay estaciones sincronizadas aún
                    DropdownMenuItem(
                        text = { Text("Sin estaciones — sincroniza los datos primero", style = MaterialTheme.typography.bodySmall) },
                        onClick = {},
                        enabled = false,
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
                allOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            expanded = false
                            if (option == "Otra") {
                                showCustomInput = true
                                onSelect(customValue)
                            } else {
                                showCustomInput = false
                                onSelect(option)
                            }
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        trailingIcon = if (option == selected) {{ Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }} else null
                    )
                }
            }
        }
        if (showCustomInput) {
            OutlinedTextField(
                value = customValue,
                onValueChange = { customValue = it; onSelect(it) },
                label = { Text("Nombre de la estación") },
                placeholder = { Text("Escribe el nombre de la bomba") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Tarjeta de costos ─────────────────────────────────────────────────────────

@Composable
private fun CostCard(
    totalActual: String,
    hasMismatch: Boolean,
    discountAmount: String,
    onTotalActualChange: (String) -> Unit,
    onDiscountChange: (String) -> Unit
) {
    var showDiscount by remember { mutableStateOf(discountAmount.isNotBlank()) }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AttachMoney, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Costos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }

            // Total del tiquete
            val focusCost = LocalFocusManager.current
            OutlinedTextField(
                value = totalActual,
                onValueChange = { onTotalActualChange(it.asDecimal()) },
                label = { Text("Total del tiquete físico") },
                placeholder = { Text("0.00") },
                prefix = { Text("$") },
                supportingText = { Text("Valor impreso en el tiquete para verificar") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusCost.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Alerta discrepancia
            AnimatedVisibility(visible = hasMismatch, enter = expandVertically(), exit = shrinkVertically()) {
                // Discrepancia detectada pero no mostrada al usuario (se marca en backend)
                // Esto se revisa en el dashboard del admin
            }

            // Descuento (opcional, colapsable)
            AnimatedVisibility(visible = showDiscount, enter = expandVertically(), exit = shrinkVertically()) {
                OutlinedTextField(
                    value = discountAmount,
                    onValueChange = { onDiscountChange(it.asDecimal()) },
                    label = { Text("Descuento aplicado") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusCost.clearFocus() }),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDiscount = false; onDiscountChange("") }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!showDiscount) {
                TextButton(
                    onClick = { showDiscount = true },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Agregar descuento", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ── SectionCard ───────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

// ── Selector de fecha y hora ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerField(value: String, onValueChange: (String) -> Unit) {
    val isoFmt = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }
    val displayFmt = remember { SimpleDateFormat("dd/MM/yyyy  —  HH:mm", Locale.getDefault()) }

    val parsed = remember(value) { runCatching { isoFmt.parse(value)!! }.getOrElse { Date() } }
    val parsedCal = remember(parsed) { Calendar.getInstance().apply { time = parsed } }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableStateOf<Long?>(null) }

    // DatePicker necesita UTC-midnight para mostrar la fecha correcta
    val utcOffset = TimeZone.getDefault().getOffset(parsed.time)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parsed.time - utcOffset
    )
    val timePickerState = rememberTimePickerState(
        initialHour = parsedCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = parsedCal.get(Calendar.MINUTE),
        is24Hour = true
    )

    val displayText = remember(value) {
        runCatching { displayFmt.format(isoFmt.parse(value)!!) }.getOrElse { value }
    }

    // Campo de solo lectura que abre el picker al tocar
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha y hora *") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)) },
            trailingIcon = { Icon(Icons.Default.EditCalendar, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    showDatePicker = true
                }
        )
    }

    // 1) Selector de fecha
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Siguiente →") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 2) Selector de hora (se abre tras confirmar la fecha)
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Seleccionar hora") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Combinar fecha UTC del DatePicker con hora local del TimePicker
                    val millis = pendingDateMillis ?: (parsed.time - utcOffset)
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
                    val finalCal = Calendar.getInstance().apply {
                        set(
                            utcCal.get(Calendar.YEAR),
                            utcCal.get(Calendar.MONTH),
                            utcCal.get(Calendar.DAY_OF_MONTH),
                            timePickerState.hour,
                            timePickerState.minute,
                            0
                        )
                        set(Calendar.MILLISECOND, 0)
                    }
                    onValueChange(isoFmt.format(finalCal.time))
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── Asset Selectors ───────────────────────────────────────────────────────────

@Composable
private fun MachineAssetSelector(machines: List<Machine>, selected: Machine?, onSelected: (Machine) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val display = selected?.let { "${it.internalIdentificationNumber}  ·  ${it.name}" } ?: "Seleccionar maquinaria *"
    val filtered = remember(machines, query) {
        if (query.isBlank()) machines
        else machines.filter {
            it.name.contains(query, true) || it.brand.contains(query, true) || it.internalIdentificationNumber.contains(query, true)
        }
    }
    AssetPickerField(text = display, icon = Icons.Default.Construction, onClick = { showDialog = true })
    if (showDialog) {
        AssetDialog("Seleccionar maquinaria", "Nombre, marca o identificador…", query, { query = it }, { showDialog = false; query = "" },
            machines.isEmpty(), "Sin maquinaria. Verifica la sincronización.", filtered.isEmpty()
        ) {
            items(filtered) { m ->
                AssetDialogRow("${m.internalIdentificationNumber}  —  ${m.name}", "${m.brand}  ·  ${m.model}") {
                    onSelected(m); showDialog = false; query = ""
                }
            }
        }
    }
}

@Composable
private fun VehiculoAssetSelector(vehiculos: List<VehiculoItem>, selected: VehiculoItem?, onSelected: (VehiculoItem) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val display = selected?.let { "${it.placa}  ·  ${it.marca}" } ?: "Seleccionar vehículo *"
    val filtered = remember(vehiculos, query) {
        if (query.isBlank()) vehiculos
        else vehiculos.filter { it.placa.contains(query, true) || it.marca.contains(query, true) }
    }
    AssetPickerField(text = display, icon = Icons.Default.DirectionsCar, onClick = { showDialog = true })
    if (showDialog) {
        AssetDialog("Seleccionar vehículo", "Placa o marca…", query, { query = it }, { showDialog = false; query = "" },
            vehiculos.isEmpty(), "Sin vehículos. Verifica la sincronización.", filtered.isEmpty()
        ) {
            items(filtered) { v ->
                AssetDialogRow("${v.placa}  —  ${v.marca}", v.tipoVehiculo) {
                    onSelected(v); showDialog = false; query = ""
                }
            }
        }
    }
}

@Composable
private fun MotoAssetSelector(motos: List<Moto>, selected: Moto?, onSelected: (Moto) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val display = selected?.placa ?: "Seleccionar motocicleta *"
    val filtered = remember(motos, query) {
        if (query.isBlank()) motos else motos.filter { it.placa.contains(query, true) }
    }
    AssetPickerField(text = display, icon = Icons.Default.TwoWheeler, onClick = { showDialog = true })
    if (showDialog) {
        AssetDialog("Seleccionar motocicleta", "Placa…", query, { query = it }, { showDialog = false; query = "" },
            motos.isEmpty(), "Sin motos. Verifica la sincronización.", filtered.isEmpty()
        ) {
            items(filtered) { m ->
                AssetDialogRow(m.placa, m.ubicacionBase.ifBlank { "Sin base asignada" }) {
                    onSelected(m); showDialog = false; query = ""
                }
            }
        }
    }
}

// ── Primitivas de selección ───────────────────────────────────────────────────

@Composable
private fun AssetPickerField(text: String, icon: ImageVector, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = {},
            readOnly = true,
            leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
        )
    }
}

@Composable
private fun AssetDialog(
    title: String,
    placeholder: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    isEmpty: Boolean,
    emptyMsg: String,
    noResults: Boolean,
    content: LazyListScope.() -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        delay(60)
        try { focusRequester.requestFocus() } catch (_: Exception) { }
        keyboard?.show()
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(0.93f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(placeholder) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = if (query.isNotEmpty()) {{
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                        }
                    }} else null,
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 340.dp)) {
                    when {
                        isEmpty   -> item { Text(emptyMsg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp, 14.dp)) }
                        noResults -> item { Text("Sin resultados para \"$query\"", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp, 14.dp)) }
                        else      -> content()
                    }
                }
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancelar") }
            }
        }
    }
}

@Composable
private fun AssetDialogRow(line1: String, line2: String, onClick: () -> Unit) {
    Column {
        Column(
            modifier = Modifier.fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Text(line1, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(line2, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ── Foto de factura ───────────────────────────────────────────────────────────

@Composable
private fun InvoicePhotoCard(
    photoPath: String?,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemove: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Foto de la factura", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("(requerida)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (photoPath != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = File(photoPath),
                        contentDescription = "Factura de combustible",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                    )
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Cancel, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text(
                    "Toca × para cambiar la imagen",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Adjunta la foto del tiquete/factura para soportar el registro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onTakePhoto,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cámara")
                    }
                    OutlinedButton(
                        onClick = onPickGallery,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Galería")
                    }
                }
            }
        }
    }
}
