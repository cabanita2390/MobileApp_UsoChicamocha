package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/** Campo de selección con diálogo de búsqueda — antes vivía duplicado como
 * `VehicleSelector` (VehiculoScreen.kt, fijo a VehiculoItem) y `DropdownField<T>`
 * (MotocicletaScreen.kt, ya genérico). `itemLabel` se usa tanto para el texto de
 * cada fila como para filtrar la búsqueda, así que el filtro cubre implícitamente
 * cualquier campo que el llamador incluya en esa etiqueta. */
@Composable
fun <T> SearchableSelectorField(
    label: String,
    displayValue: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    dialogTitle: String = label,
    emptyMessage: String = "No hay opciones disponibles",
    searchPlaceholder: String = "Buscar...",
    dropdownTag: String = "dropdown_option"
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
                    Text(dialogTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(searchPlaceholder) },
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
                            item { Text(
                                emptyMessage,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                            ) }
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
