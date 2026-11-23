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
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ElementoDeConjunto
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

internal fun sinTildes(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase().trim()

/**
 * Lo marcado, para el resumen del registro: cada conjunto con sus elementos ("Transformador: Fusibles,
 * Estado físico (con óxido)"); "No aplica" si no hay nada.
 */
internal fun resumenIntervenido(s: CapturaUiState): String {
    val marcados = s.elementosAEnviar
    val otros = s.otrosAEnviar.associate { it.conjuntoId to it.texto }
    val partes = s.conjuntosMarcados.map { c ->
        val nombres = c.elementos.mapNotNull { e ->
            marcados.firstOrNull { it.conjuntoId == c.id && it.elementoId == e.id }
                ?.let { m -> if (m.detalle != null) "${e.nombre} (${m.detalle})" else e.nombre }
        } + listOfNotNull(otros[c.id])
        if (nombres.isEmpty()) c.nombre else "${c.nombre}: ${nombres.joinToString(", ")}"
    } + listOfNotNull(s.conjuntoOtro.trim().takeIf { s.conjuntoOtroActivo && it.isNotEmpty() }?.let { "Otro: $it" }) +
        listOfNotNull(s.elementosSinConjunto.takeIf { it.isNotEmpty() }?.let { l -> "Sin conjunto: ${l.joinToString(", ") { it.nombre }}" })
    return if (partes.isEmpty()) "No aplica" else partes.joinToString(" · ")
}

/** Elementos de un conjunto que coinciden con la búsqueda (todos si no se busca nada). */
internal fun elementosVisibles(conjunto: ItemIntervenido, busqueda: String): List<ElementoDeConjunto> {
    val q = sinTildes(busqueda)
    return if (q.isEmpty()) conjunto.elementos else conjunto.elementos.filter { sinTildes(it.nombre).contains(q) }
}

/**
 * Paso «¿Qué se intervino?» (preguntas 9 y 11 del formulario). Primero los conjuntos en mosaicos con
 * ícono; después, los elementos que tiene adentro cada conjunto marcado: con uno solo, su lista; con
 * varios, separados bajo el nombre de cada conjunto, para saber de cuál es cada elemento. El conjunto
 * es obligatorio donde la disciplina lo pide; los elementos, siempre opcionales.
 */
@Composable
internal fun PasoIntervenido(uiState: CapturaUiState, viewModel: CapturaViewModel) {
    val conjuntos = uiState.conjuntosDisciplina
    val marcados = uiState.conjuntosMarcados
    val nConjuntos = uiState.conjuntosSeleccionados.size + if (uiState.conjuntoOtroActivo && uiState.conjuntoOtro.isNotBlank()) 1 else 0
    val nElementos = uiState.elementosAEnviar.size + uiState.otrosAEnviar.size
    val totalElementos = marcados.sumOf { it.elementos.size }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            EncabezadoPregunta(
                1, "¿En qué conjunto trabajaste?",
                if (uiState.conjuntoObligatorio) "Puedes marcar varios · obligatorio" else "Puedes marcar varios · opcional",
                nConjuntos, obligatorio = uiState.conjuntoObligatorio
            )
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
            EncabezadoPregunta(2, "¿Qué elementos revisaste?", "Inspeccionados o intervenidos · opcional", nElementos, obligatorio = false)
        }
        if (marcados.isEmpty()) {
            item { SubestacionType.Hint("Marca primero un conjunto: aquí aparecen los elementos que tiene adentro.") }
        }
        // Con muchos elementos, un buscador que filtra dentro de los conjuntos marcados.
        if (totalElementos > 8) {
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
        }
        marcados.forEach { conjunto ->
            val visibles = elementosVisibles(conjunto, uiState.busquedaElemento)
            // Con varios conjuntos, cada uno con su título: así no se confunden los nombres repetidos.
            if (marcados.size > 1) {
                item(key = "titulo_${conjunto.id}") { TituloConjunto(conjunto) }
            }
            if (conjunto.elementos.isEmpty()) {
                item(key = "vacio_${conjunto.id}") { SubestacionType.Hint("Este conjunto no tiene elementos en la lista. Usa «Otro elemento».") }
            } else if (visibles.isEmpty()) {
                item(key = "nada_${conjunto.id}") { SubestacionType.Hint("Ningún elemento de ${conjunto.nombre} coincide.") }
            }
            visibles.chunked(2).forEachIndexed { i, fila ->
                item(key = "fila_${conjunto.id}_$i") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { e ->
                            val on = MarcaElemento(conjunto.id, e.id) in uiState.elementosMarcados
                            CasillaElemento(e.nombre, on, Modifier.weight(1f)) { viewModel.onElementoToggle(conjunto.id, e.id) }
                        }
                        if (fila.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            // Los marcados que piden texto: cómo se encuentra (opcional).
            conjunto.elementos.filter { it.pideDetalle && MarcaElemento(conjunto.id, it.id) in uiState.elementosMarcados }.forEach { e ->
                item(key = "detalle_${conjunto.id}_${e.id}") {
                    CampoOtro(
                        "${e.nombre} · ¿cómo se encuentra?",
                        uiState.detallesElemento[MarcaElemento(conjunto.id, e.id)] ?: "",
                        "Ej. Con óxido en la tapa",
                        { viewModel.onDetalleElementoChange(conjunto.id, e.id, it) },
                        unaLinea = false
                    )
                }
            }
            item(key = "otro_${conjunto.id}") {
                BotonOtroElemento(
                    if (marcados.size > 1) "Otro elemento de ${conjunto.nombre}" else "Otro elemento que no está en la lista",
                    conjunto.id in uiState.otrosElementosActivos
                ) { viewModel.onElementoOtroToggle(conjunto.id) }
            }
            if (conjunto.id in uiState.otrosElementosActivos) {
                item(key = "otro_campo_${conjunto.id}") {
                    CampoOtro(
                        "¿Qué otro elemento?", uiState.otrosElementos[conjunto.id] ?: "", "Ej. Bornera auxiliar",
                        { viewModel.onElementoOtroChange(conjunto.id, it) }
                    )
                }
            }
        }
        if (uiState.elementosSinConjunto.isNotEmpty()) {
            item {
                SubestacionType.Hint(
                    "Registrado antes sin conjunto: ${uiState.elementosSinConjunto.joinToString(", ") { it.nombre }}. Se conserva."
                )
            }
        }
    }
}

@Composable
private fun TituloConjunto(conjunto: ItemIntervenido) {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(AmbarSuave),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconoConjunto(conjunto), contentDescription = null, tint = SubestacionColors.Amber, modifier = Modifier.size(16.dp))
        }
        Text(
            conjunto.nombre.uppercase(), color = SubestacionColors.TextSecondary, fontWeight = FontWeight.ExtraBold,
            fontSize = 11.5.sp, letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BotonOtroElemento(texto: String, activo: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(SubestacionShapes.Input)
            .background(if (activo) SubestacionColors.PurpleTint else Color.White)
            .border(2.dp, if (activo) SubestacionColors.Purple else SubestacionColors.DashedUploadBorder, SubestacionShapes.Input)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = SubestacionColors.Purple, modifier = Modifier.size(18.dp))
        Text(texto, color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
    }
}

@Composable
private fun EncabezadoPregunta(numero: Int, titulo: String, sub: String, marcados: Int, obligatorio: Boolean) {
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
                    0 -> if (obligatorio) "Obligatorio" else "Opcional"
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
private fun CampoOtro(etiqueta: String, valor: String, ejemplo: String, onChange: (String) -> Unit, unaLinea: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, color = SubestacionColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = { Text(ejemplo) },
            singleLine = unaLinea,
            maxLines = if (unaLinea) 1 else 4,
            shape = SubestacionShapes.Input,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SubestacionColors.Purple,
                unfocusedBorderColor = SubestacionColors.Purple
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
