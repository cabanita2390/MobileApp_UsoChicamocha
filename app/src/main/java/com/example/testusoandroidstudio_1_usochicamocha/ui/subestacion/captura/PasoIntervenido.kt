package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.captura

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ItemIntervenido
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionType
import java.text.Normalizer

/** Fondo ámbar del ícono de cada conjunto (identidad de Eléctrico). */
private val AmbarSuave = Color(0xFFFDF3D8)

/**
 * Ícono de un conjunto: el código que se eligió en Configuración (web), con los mismos dibujos de
 * Material que muestra el selector. Si todavía no llegó del servidor (caché anterior), se deduce por
 * el nombre; si no, el genérico.
 */
private fun iconoConjunto(conjunto: ItemIntervenido): ImageVector = when (conjunto.icono) {
    "SUBESTACION" -> Icons.Filled.ElectricalServices
    "GABINETE" -> Icons.Filled.Dns
    "RED_MT" -> Icons.Filled.Cable
    "GENERADOR" -> Icons.Filled.Bolt
    "ILUMINACION" -> Icons.Filled.Lightbulb
    "MOTOR" -> Icons.Filled.Settings
    "TRANSFORMADOR" -> Icons.Filled.ElectricBolt
    "POSTE" -> Icons.Filled.CellTower
    "SENSOR" -> Icons.Filled.Sensors
    "CONTROL" -> Icons.Filled.Tune
    "ENERGIA" -> Icons.Filled.Power
    else -> iconoPorNombre(conjunto.nombre)
}

private fun iconoPorNombre(nombre: String): ImageVector = when (sinTildes(nombre)) {
    "subestacion" -> Icons.Filled.ElectricalServices
    "gabinetes de control" -> Icons.Filled.Dns
    "red de media tension" -> Icons.Filled.Cable
    "generador var" -> Icons.Filled.Bolt
    "iluminacion" -> Icons.Filled.Lightbulb
    "motores" -> Icons.Filled.Settings
    "transformador" -> Icons.Filled.ElectricBolt
    "postes" -> Icons.Filled.CellTower
    else -> Icons.Filled.Power
}

/**
 * Elementos que suelen ir con cada conjunto. Solo ordena la lista (sugeridos primero): cualquier
 * elemento se puede marcar con cualquier conjunto. Por nombre, sin tildes ni mayúsculas.
 */
private val ELEMENTOS_DE_CONJUNTO: Map<String, List<String>> = mapOf(
    "subestacion" to listOf("subestacion", "transformador", "sistema de puesta a tierra", "breaker", "reconectador"),
    "gabinetes de control" to listOf(
        "gabinete electrico", "plc / sistema de control", "variador de velocidad", "arrancador suave",
        "contactores", "reles de proteccion", "breaker", "cableado y conexiones", "sensores / instrumentacion"
    ),
    "red de media tension" to listOf("red media tension", "reconectador", "postes", "cableado y conexiones"),
    "generador var" to listOf("generador var", "arranque"),
    "iluminacion" to listOf("iluminacion", "cableado y conexiones"),
    "motores" to listOf("motor electrico", "variador de velocidad", "arrancador suave", "arranque", "sensores / instrumentacion"),
    "transformador" to listOf("transformador", "sistema de puesta a tierra"),
    "postes" to listOf("postes", "red media tension")
)

internal fun sinTildes(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase().trim()

/** Lo marcado, en una línea, para el resumen del registro ("No aplica" si no hay nada). */
internal fun resumenIntervenido(s: CapturaUiState): String {
    val nombres = s.conjuntosDisciplina.filter { it.id in s.conjuntosSeleccionados }.map { it.nombre } +
        s.elementosDisciplina.filter { it.id in s.elementosSeleccionados }.map { it.nombre } +
        listOfNotNull(
            s.conjuntoOtro.trim().takeIf { s.conjuntoOtroActivo && it.isNotEmpty() },
            s.elementoOtro.trim().takeIf { s.elementoOtroActivo && it.isNotEmpty() }
        )
    return if (nombres.isEmpty()) "No aplica" else nombres.joinToString(", ")
}

/** Grupos de elementos a mostrar: los que coinciden con la búsqueda, o los sugeridos primero. */
internal data class GrupoElementos(val titulo: String, val items: List<ItemIntervenido>)

internal fun gruposDeElementos(s: CapturaUiState): List<GrupoElementos> {
    val todos = s.elementosDisciplina
    val busqueda = sinTildes(s.busquedaElemento)
    if (busqueda.isNotEmpty()) {
        val hallados = todos.filter { sinTildes(it.nombre).contains(busqueda) }
        return if (hallados.isEmpty()) emptyList() else listOf(GrupoElementos("${hallados.size} COINCIDEN", hallados))
    }
    val sugeridosNombres = s.conjuntosDisciplina
        .filter { it.id in s.conjuntosSeleccionados }
        .flatMap { ELEMENTOS_DE_CONJUNTO[sinTildes(it.nombre)].orEmpty() }
        .distinct()
    val sugeridos = sugeridosNombres.mapNotNull { n -> todos.find { sinTildes(it.nombre) == n } }
    if (sugeridos.isEmpty()) return listOf(GrupoElementos("TODOS LOS ELEMENTOS", todos))
    val demas = todos.filter { it !in sugeridos }
    return if (s.verTodosElementos) {
        listOf(GrupoElementos("SUGERIDOS POR LO QUE MARCASTE ARRIBA", sugeridos), GrupoElementos("LOS DEMÁS ELEMENTOS", demas))
    } else {
        // Lo ya marcado fuera de los sugeridos sigue a la vista aunque el resto esté plegado.
        listOf(GrupoElementos("SUGERIDOS POR LO QUE MARCASTE ARRIBA", sugeridos + demas.filter { it.id in s.elementosSeleccionados }))
    }
}

/**
 * Paso «¿Qué se intervino?» (solo Eléctrico; preguntas 9 y 11 del formulario). Conjuntos en mosaicos
 * con ícono y elementos en casillas, primero los sugeridos según los conjuntos marcados. Todo es
 * opcional: no marcar nada equivale a "No aplica".
 */
@Composable
internal fun PasoIntervenido(uiState: CapturaUiState, viewModel: CapturaViewModel) {
    val conjuntos = uiState.conjuntosDisciplina
    val grupos = gruposDeElementos(uiState)
    val haySugeridos = uiState.busquedaElemento.isBlank() && uiState.conjuntosSeleccionados.isNotEmpty() &&
        grupos.firstOrNull()?.titulo?.startsWith("SUGERIDOS") == true
    val nConjuntos = uiState.conjuntosSeleccionados.size + if (uiState.conjuntoOtroActivo && uiState.conjuntoOtro.isNotBlank()) 1 else 0
    val nElementos = uiState.elementosSeleccionados.size + if (uiState.elementoOtroActivo && uiState.elementoOtro.isNotBlank()) 1 else 0

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            EncabezadoPregunta(1, "¿En qué conjunto trabajaste?", "Puedes marcar varios · opcional", nConjuntos)
        }
        if (conjuntos.isEmpty()) {
            item { SubestacionType.Hint("Los conjuntos aún no se han sincronizado. Puedes escribirlo en «Otro».", color = SubestacionColors.Red) }
        }
        // Mosaicos de 3 columnas: los conjuntos y "Otro" al final.
        val celdas = conjuntos.map { c -> Triple(c.nombre, iconoConjunto(c), c.id) } + Triple("Otro", Icons.Filled.Add, -1L)
        celdas.chunked(3).forEach { fila ->
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fila.forEach { (nombre, icono, id) ->
                        val esOtro = id == -1L
                        val on = if (esOtro) uiState.conjuntoOtroActivo else id in uiState.conjuntosSeleccionados
                        MosaicoConjunto(nombre, icono, on, esOtro, Modifier.weight(1f)) {
                            if (esOtro) viewModel.onConjuntoOtroToggle() else viewModel.onConjuntoToggle(id)
                        }
                    }
                    repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        if (uiState.conjuntoOtroActivo) {
            item {
                CampoOtro("¿Qué otro conjunto?", uiState.conjuntoOtro, "Ej. Tablero de bombas auxiliares", viewModel::onConjuntoOtroChange)
            }
        }

        item { Box(Modifier.fillMaxWidth().height(1.dp).background(SubestacionColors.PurpleBorderLight)) }

        item {
            EncabezadoPregunta(2, "¿Qué elementos revisaste?", "Inspeccionados o intervenidos · opcional", nElementos)
        }
        item {
            OutlinedTextField(
                value = uiState.busquedaElemento,
                onValueChange = viewModel::onBusquedaElementoChange,
                placeholder = { Text("Buscar elemento…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SubestacionColors.TextTertiary) },
                singleLine = true,
                shape = SubestacionShapes.Input,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SubestacionColors.Purple,
                    unfocusedBorderColor = SubestacionColors.PurpleBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        grupos.forEach { grupo ->
            item {
                Text(
                    grupo.titulo, color = SubestacionColors.TextTertiary, fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.5.sp, letterSpacing = 0.6.sp
                )
            }
            grupo.items.chunked(2).forEach { fila ->
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { e ->
                            CasillaElemento(e.nombre, e.id in uiState.elementosSeleccionados, Modifier.weight(1f)) {
                                viewModel.onElementoToggle(e.id)
                            }
                        }
                        if (fila.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        if (uiState.busquedaElemento.isNotBlank() && grupos.isEmpty()) {
            item { SubestacionType.Hint("Ningún elemento coincide. Usa «Otro elemento».") }
        }
        if (haySugeridos) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clip(SubestacionShapes.Input)
                        .background(SubestacionColors.PurpleSurface)
                        .clickable { viewModel.onVerTodosElementosToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (uiState.verTodosElementos) "Ver solo los sugeridos" else "Ver los ${uiState.elementosDisciplina.size} elementos",
                        color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp
                    )
                }
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(SubestacionShapes.Input)
                    .background(if (uiState.elementoOtroActivo) SubestacionColors.PurpleTint else Color.White)
                    .border(
                        2.dp,
                        if (uiState.elementoOtroActivo) SubestacionColors.Purple else SubestacionColors.DashedUploadBorder,
                        SubestacionShapes.Input
                    )
                    .clickable { viewModel.onElementoOtroToggle() }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = SubestacionColors.Purple, modifier = Modifier.size(18.dp))
                Text("Otro elemento que no está en la lista", color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
        if (uiState.elementoOtroActivo) {
            item {
                CampoOtro("¿Qué otro elemento?", uiState.elementoOtro, "Ej. Celda de medida", viewModel::onElementoOtroChange)
            }
        }
    }
}

@Composable
private fun EncabezadoPregunta(numero: Int, titulo: String, sub: String, marcados: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (marcados > 0) SubestacionColors.Purple else SubestacionColors.PurpleBorderLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$numero", color = if (marcados > 0) Color.White else SubestacionColors.TextSecondary,
                fontWeight = FontWeight.ExtraBold, fontSize = 12.sp
            )
        }
        Column(Modifier.weight(1f)) {
            Text(titulo, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text(sub, color = SubestacionColors.TextTertiary, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (marcados > 0) SubestacionColors.Purple else SubestacionColors.ChipBg)
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Text(
                when (marcados) {
                    0 -> "Opcional"
                    1 -> "1 marcado"
                    else -> "$marcados marcados"
                },
                color = if (marcados > 0) Color.White else SubestacionColors.TextSecondary,
                fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp
            )
        }
    }
}

@Composable
private fun MosaicoConjunto(
    nombre: String,
    icono: ImageVector,
    seleccionado: Boolean,
    esOtro: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Box(
        modifier
            .heightIn(min = 96.dp)
            .clip(forma)
            .background(if (seleccionado) SubestacionColors.Purple else Color.White)
            .border(
                2.dp,
                when {
                    seleccionado -> SubestacionColors.Purple
                    esOtro -> SubestacionColors.DashedUploadBorder
                    else -> SubestacionColors.PurpleBorderLight
                },
                forma
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        when {
                            seleccionado -> Color.White.copy(alpha = 0.16f)
                            esOtro -> SubestacionColors.PurpleSurface
                            else -> AmbarSuave
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icono, contentDescription = null, modifier = Modifier.size(22.dp),
                    tint = when {
                        seleccionado -> Color.White
                        esOtro -> SubestacionColors.Purple
                        else -> SubestacionColors.Amber
                    }
                )
            }
            Text(
                nombre, color = if (seleccionado) Color.White else SubestacionColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, lineHeight = 14.sp, textAlign = TextAlign.Center
            )
        }
        if (seleccionado) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = SubestacionColors.Purple, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun CasillaElemento(nombre: String, seleccionado: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (seleccionado) SubestacionColors.PurpleTint else Color.White)
            .border(2.dp, if (seleccionado) SubestacionColors.Purple else SubestacionColors.PurpleBorderLight, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (seleccionado) SubestacionColors.Purple else Color.White)
                .border(2.dp, if (seleccionado) SubestacionColors.Purple else SubestacionColors.PurpleBorder, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (seleccionado) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(9.dp))
        Text(nombre, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun CampoOtro(etiqueta: String, valor: String, ejemplo: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, color = SubestacionColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = { Text(ejemplo) },
            singleLine = true,
            shape = SubestacionShapes.Input,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SubestacionColors.Purple,
                unfocusedBorderColor = SubestacionColors.Purple
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
