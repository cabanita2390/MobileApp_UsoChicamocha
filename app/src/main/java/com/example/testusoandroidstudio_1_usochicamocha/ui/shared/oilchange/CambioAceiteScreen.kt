package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil

/** Lo que de verdad difiere entre la pantalla de cambio de aceite de vehículo y la de moto:
 * textos y cómo se muestra cada activo en el selector. Todo lo demás (layout, validación,
 * estados de carga) es idéntico y vive en este composable genérico. */
data class CambioAceiteScreenConfig(
    val subtitle: String,
    val assetLabel: String,
    val assetDialogTitle: String,
    val assetSearchPlaceholder: String,
    val assetEmptyMessage: String,
    val intervalSupportingText: String? = null,
    val assetDisplayText: (AssetOilChangeItem) -> String,
    val assetListItemText: (AssetOilChangeItem) -> String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambioAceiteScreen(
    uiState: CambioAceiteUiState,
    config: CambioAceiteScreenConfig,
    onAssetSelected: (AssetOilChangeItem) -> Unit,
    onOilSelected: (Oil) -> Unit,
    onKmAtChangeChange: (String) -> Unit,
    onIntervalKmChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onAirFilterChanged: (Boolean) -> Unit,
    onSyncOils: () -> Unit,
    onSubmit: () -> Unit,
    onSubmissionSuccessHandled: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            Toast.makeText(context, "Cambio de aceite guardado.", Toast.LENGTH_SHORT).show()
            onSubmissionSuccessHandled()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Registrar cambio de aceite", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(config.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.isRoleAllowed) {
                Button(
                    onClick = onSubmit,
                    enabled = !uiState.isLoading && uiState.isRoleAllowed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardando...", fontSize = 14.sp)
                    } else {
                        Text("✓ Guardar cambio", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .padding(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (!uiState.isRoleAllowed) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "No tiene permisos para registrar cambios de aceite",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Selector de activo ─────────────────────────────────────────────
            item {
                AssetDropdownField(
                    assets = uiState.assets,
                    selectedAsset = uiState.selectedAsset,
                    enabled = uiState.isRoleAllowed,
                    config = config,
                    onAssetSelected = onAssetSelected
                )
            }

            // ── Marca de aceite ───────────────────────────────────────────────
            item {
                OilBrandDropdown(
                    oils = uiState.oilBrands,
                    selectedOil = uiState.selectedOil,
                    enabled = uiState.oilBrands.isNotEmpty() && uiState.isRoleAllowed,
                    onOilSelected = onOilSelected
                )
                if (uiState.oilBrands.isEmpty()) {
                    if (uiState.isSyncingOils) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Sincronizando marcas de aceite...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                "No hay marcas de aceite sincronizadas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onSyncOils) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }

            // ── Kilometraje actual ────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.kmAtChange,
                    onValueChange = onKmAtChangeChange,
                    label = { Text("Kilometraje actual (*)", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // ── Intervalo próximo cambio ──────────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.intervalKm,
                    onValueChange = onIntervalKmChange,
                    label = { Text("Intervalo próximo cambio (km) (*)", fontWeight = FontWeight.Bold) },
                    supportingText = config.intervalSupportingText?.let { text ->
                        { Text(text, style = MaterialTheme.typography.bodySmall) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // ── Cantidad de aceite (opcional) ─────────────────────────────────
            item {
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Cantidad (L) — opcional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = uiState.isRoleAllowed,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // ── Filtro de aire ────────────────────────────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = uiState.airFilterChanged,
                        enabled = uiState.isRoleAllowed,
                        onCheckedChange = onAirFilterChanged
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("¿Se cambió el filtro de aire?", style = MaterialTheme.typography.bodyMedium)
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun AssetDropdownField(
    assets: List<AssetOilChangeItem>,
    selectedAsset: AssetOilChangeItem?,
    enabled: Boolean,
    config: CambioAceiteScreenConfig,
    onAssetSelected: (AssetOilChangeItem) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayText = selectedAsset?.let { config.assetDisplayText(it) } ?: ""
    val filteredAssets = remember(assets, searchQuery) {
        if (searchQuery.isBlank()) assets
        else assets.filter {
            it.placa.contains(searchQuery, ignoreCase = true) ||
                (it.marca?.contains(searchQuery, ignoreCase = true) ?: false)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(config.assetLabel, fontWeight = FontWeight.Bold) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (enabled) {
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
                    Text(config.assetDialogTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(config.assetSearchPlaceholder) },
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
                        if (assets.isEmpty()) {
                            item { Text(config.assetEmptyMessage, color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else if (filteredAssets.isEmpty()) {
                            item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else {
                            items(filteredAssets) { asset ->
                                Column {
                                    Text(
                                        text = config.assetListItemText(asset),
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onAssetSelected(asset); showDialog = false; searchQuery = "" }
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
private fun OilBrandDropdown(
    oils: List<Oil>,
    selectedOil: Oil?,
    enabled: Boolean,
    onOilSelected: (Oil) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayText = selectedOil?.name ?: ""
    val filteredOils = remember(oils, searchQuery) {
        if (searchQuery.isBlank()) oils
        else oils.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Marca de aceite (*)", fontWeight = FontWeight.Bold) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (enabled) {
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
                    Text("Seleccionar marca de aceite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar marca...") },
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
                        if (filteredOils.isEmpty()) {
                            item { Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) }
                        } else {
                            items(filteredOils) { oil ->
                                Column {
                                    Text(
                                        text = oil.name,
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onOilSelected(oil); showDialog = false; searchQuery = "" }
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
