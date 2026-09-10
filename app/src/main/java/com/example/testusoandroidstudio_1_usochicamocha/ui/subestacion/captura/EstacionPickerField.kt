package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.captura

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EstacionCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes

/**
 * Selector de estación con el estilo real del mock (mock líneas ~286-303): buscador
 * + lista de filas con un "radio" circular relleno de color cuando está seleccionada.
 * Deliberadamente NO reusa `SearchableSelectorField` (compartido con Vehículo/Moto)
 * para no cambiarles el estilo a esas pantallas — es un componente propio de
 * Subestaciones.
 */
@Composable
fun EstacionPickerField(
    estaciones: List<EstacionCatalogo>,
    estacionSeleccionadaId: Long?,
    onEstacionSeleccionada: (EstacionCatalogo) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val filtradas = remember(estaciones, query) {
        if (query.isBlank()) estaciones
        else estaciones.filter { it.nombre.contains(query, ignoreCase = true) }
    }

    Column(modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar entre las ${estaciones.size} estaciones…") },
            singleLine = true,
            shape = SubestacionShapes.Input,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SubestacionColors.Purple,
                unfocusedBorderColor = SubestacionColors.PurpleBorder,
                focusedContainerColor = SubestacionColors.CardBackground,
                unfocusedContainerColor = SubestacionColors.CardBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (filtradas.isEmpty()) {
            Text(
                "Ninguna estación coincide",
                color = SubestacionColors.TextQuaternary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            val listState = rememberLazyListState()
            BoxWithConstraints(Modifier.heightIn(max = 280.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    items(filtradas, key = { it.id }) { estacion ->
                        val seleccionada = estacion.id == estacionSeleccionadaId
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(SubestacionShapes.Chip)
                                .background(if (seleccionada) SubestacionColors.PurpleSurface else SubestacionColors.CardBackground)
                                .border(
                                    if (seleccionada) 2.dp else 1.dp,
                                    if (seleccionada) SubestacionColors.Purple else SubestacionColors.PurpleBorder,
                                    SubestacionShapes.Chip
                                )
                                .clickable { onEstacionSeleccionada(estacion) }
                                .padding(horizontal = 15.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioIndicador(seleccionada)
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(
                                    estacion.nombre, color = SubestacionColors.TextPrimary,
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                                Text(
                                    "${if (estacion.tipo == "BOMBEO") "Bombeo" else "Complementaria"} · ${
                                        if (estacion.frecuenciaBase == "TRIMESTRAL") "Trimestral" else "Anual"
                                    }",
                                    color = SubestacionColors.TextTertiary, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
                // Barra de scroll mini — sin esto, el contenedor de altura fija no
                // deja ver si hay más estaciones debajo (el mock cuenta con la
                // barra nativa del navegador, que Compose no dibuja sola).
                val info = listState.layoutInfo
                val total = info.totalItemsCount
                val visibles = info.visibleItemsInfo.size
                if (total > 0 && visibles < total) {
                    val pista = maxHeight
                    val fraccionThumb = (visibles.toFloat() / total.toFloat()).coerceIn(0.12f, 1f)
                    val altoThumb = pista * fraccionThumb
                    val maxOffset = pista - altoThumb
                    val recorrible = (total - visibles).coerceAtLeast(1).toFloat()
                    val fraccionScroll = (listState.firstVisibleItemIndex.toFloat() / recorrible).coerceIn(0f, 1f)
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = maxOffset * fraccionScroll)
                            .width(4.dp)
                            .height(altoThumb)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SubestacionColors.PurpleBorder)
                    )
                }
            }
        }
    }
}

@Composable
private fun RadioIndicador(seleccionada: Boolean) {
    androidx.compose.foundation.layout.Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(2.dp, if (seleccionada) SubestacionColors.Purple else SubestacionColors.PurpleBorder, CircleShape)
            .background(if (seleccionada) SubestacionColors.Purple else SubestacionColors.CardBackground)
    )
}
