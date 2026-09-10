package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.testusoandroidstudio_1_usochicamocha.BuildConfig
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EdicionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EvidenciaDetalle
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.L_ACT
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.L_MANT
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.L_MOT
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.L_RES
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.nombreMes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionSyncIconButton
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionType

/** URL completa de una evidencia: el backend sirve las rutas de /uploads fuera de /api (ver WebConfig del backend). */
private fun urlEvidencia(rutaArchivo: String): String {
    val raiz = BuildConfig.BASE_URL.substringBefore("/api")
    return "$raiz/uploads/$rutaArchivo"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    ejecucionId: Long,
    networkStatus: Boolean,
    viewModel: DetalleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditar: (Long, String) -> Unit,
    onNavigateToCola: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(ejecucionId) { viewModel.cargar(ejecucionId) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.recargarSiCorresponde()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = SubestacionColors.ScreenBackground,
        topBar = {
            Column(Modifier.background(SubestacionColors.ScreenBackground).statusBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp, 18.dp, 20.dp, 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(SubestacionColors.PurpleSurface)
                            .clickable(onClick = onNavigateBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = SubestacionColors.TextSecondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        SubestacionType.ScreenTitle("Detalle del registro")
                        SubestacionType.ScreenSubtitle("Solo lectura")
                    }
                    ConnectionStatusTopBar(isConnected = networkStatus)
                    SubestacionSyncIconButton(onClick = onNavigateToCola)
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(SubestacionColors.ScreenBackground)) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SubestacionColors.Purple)
                }
                !networkStatus && uiState.detalle == null -> EstadoVacioDetalle(
                    "Necesitas conexión para ver el detalle de este registro."
                )
                uiState.error != null -> EstadoVacioDetalle(
                    uiState.error ?: "No se pudo cargar el detalle.",
                    onReintentar = { viewModel.cargar(ejecucionId) }
                )
                uiState.detalle != null -> DetalleContenido(uiState.detalle!!, networkStatus, onNavigateToEditar)
            }
        }
    }
}

@Composable
private fun EstadoVacioDetalle(mensaje: String, onReintentar: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(mensaje, textAlign = TextAlign.Center, color = SubestacionColors.TextTertiary)
        if (onReintentar != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Reintentar", color = SubestacionColors.Purple, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onReintentar)
            )
        }
    }
}

@Composable
private fun DetalleContenido(d: EjecucionDetalle, networkStatus: Boolean, onNavigateToEditar: (Long, String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            EstadoBanner(
                texto = if (d.evidenciaPendiente) "EJECUTADA · FALTA EVIDENCIA" else "EJECUTADA Y SINCRONIZADA",
                colores = if (d.evidenciaPendiente)
                    Triple(SubestacionColors.Amber, SubestacionColors.AmberBg, SubestacionColors.AmberBorder)
                else
                    Triple(SubestacionColors.Green, SubestacionColors.GreenBg, SubestacionColors.GreenBorder)
            )
        }
        if (!networkStatus) {
            item { AvisoCard("Sin señal — este detalle es el último que se pudo cargar.", SubestacionColors.Amber, SubestacionColors.AmberBg, SubestacionColors.AmberBorder) }
        }

        item {
            Column(
                Modifier.fillMaxWidth().clip(SubestacionShapes.CardLarge).background(Color.White)
                    .border(1.5.dp, SubestacionColors.PurpleBorderLight, SubestacionShapes.CardLarge)
            ) {
                SeccionEncabezado("CONTEXTO")
                FilaDetalle("Fecha", d.fecha)
                FilaDetalle("Mes / semana", "${nombreMes(d.mesEjecucion)} · Semana ${d.semanaEjecucion}")
                FilaDetalle("Responsable", d.responsable)
                FilaDetalle("Estación", d.estacionNombre)
                FilaDetalle("Disciplina", if (d.disciplina == "CIVIL") "Civil" else d.disciplina)

                SeccionEncabezado("ACTIVIDAD")
                FilaDetalle("Tipo de actividad", L_ACT[d.tipoActividad] ?: d.tipoActividad)
                FilaDetalle("Tipo de mantenimiento", L_MANT[d.tipoMantenimiento] ?: d.tipoMantenimiento)
                FilaDetalle(
                    if (d.actividadId != null) "Actividad civil" else "Descripción (no catalogada)",
                    d.actividadNombre ?: d.descripcionLibre ?: "—"
                )
                FilaDetalle("Programada", if (d.esProgramada) "Sí, venía de una cita del cronograma" else "No, registro libre")
                if (d.actividadId == null && !d.motivoNoCatalogado.isNullOrBlank()) {
                    FilaDetalle("Motivo", L_MOT[d.motivoNoCatalogado] ?: d.motivoNoCatalogado)
                }

                SeccionEncabezado("RESULTADO")
                FilaDetalle("Resultado", L_RES[d.resultado] ?: d.resultado)
                FilaDetalle("Observaciones", d.observaciones)

                // El backend no expone un timestamp de servidor separado de `fecha` (no
                // hay createdAt/syncedAt) — el mock tampoco lo tiene, usa x.fecha para
                // las dos filas "Registrado"/"Sincronizado".
                SeccionEncabezado("TRAZABILIDAD")
                FilaDetalle("Registrado", "${d.fecha} · desde la app móvil")
                FilaDetalle("Sincronizado", "${d.fecha} · confirmado por el servidor")
                FilaDetalle("uuidCliente", d.uuidCliente)
                FilaDetalle("ID ejecución", "EJ-${d.id}", ultima = true)
            }
        }

        if (d.evidencias.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    SubestacionType.SectionLabel("Fotografías (${d.evidencias.size})")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(d.evidencias, key = { it.id }) { evidencia: EvidenciaDetalle ->
                            Box(
                                Modifier.size(104.dp).clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(SubestacionColors.PhotoGradientStart, SubestacionColors.PhotoGradientEnd)))
                            ) {
                                AsyncImage(
                                    model = urlEvidencia(evidencia.rutaArchivo),
                                    contentDescription = evidencia.nombreOriginal,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        if (d.ediciones.isNotEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().clip(SubestacionShapes.CardLarge).background(SubestacionColors.AmberBg)
                        .border(1.5.dp, SubestacionColors.AmberBorder, SubestacionShapes.CardLarge).padding(15.dp)
                ) {
                    Text("HISTORIAL DE EDICIONES", color = SubestacionColors.Amber, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 0.7.sp)
                    d.ediciones.forEach { edicion: EdicionDetalle ->
                        Text(
                            "${edicion.usuario}${edicion.editadoEn?.let { " · $it" } ?: ""} — ${edicion.motivo}",
                            color = Color(0xFF5C4410), fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 7.dp)
                        )
                    }
                }
            }
        }

        item { PanelEditarRegistro(ejecucionId = d.id, networkStatus = networkStatus, onNavigateToEditar = onNavigateToEditar) }

        item { Spacer(Modifier.height(4.dp)) }
    }
}

/**
 * Tarjeta "Editar este registro" (mock líneas ~211-228): un botón que abre un panel
 * inline con el motivo de la edición — el motivo se captura ACÁ, antes de entrar al
 * wizard, no dentro de él (así el wizard de edición queda visualmente idéntico al de
 * captura normal: mismos 4 pasos, Contexto/Actividad/Resultado/Evidencia).
 */
@Composable
private fun PanelEditarRegistro(ejecucionId: Long, networkStatus: Boolean, onNavigateToEditar: (Long, String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("") }
    val longitud = motivo.trim().length
    val motivoOk = longitud >= 15

    Column(
        Modifier.fillMaxWidth().clip(SubestacionShapes.CardLarge).background(Color.White)
            .border(1.5.dp, SubestacionColors.PurpleBorderLight, SubestacionShapes.CardLarge).padding(16.dp)
    ) {
        SubestacionType.CardTitle("Editar este registro")
        Text(
            "Queda auditado: se guarda quién editó, cuándo y por qué. El original no se borra.",
            color = SubestacionColors.TextTertiary, fontWeight = FontWeight.Medium, fontSize = 11.5.sp,
            modifier = Modifier.padding(top = 5.dp)
        )
        if (!abierto) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(48.dp)
                    .clip(SubestacionShapes.Button)
                    .background(if (networkStatus) SubestacionColors.PurpleSurface else SubestacionColors.PurpleBorderLight)
                    .clickable(enabled = networkStatus) { abierto = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Edit, contentDescription = null,
                    tint = if (networkStatus) SubestacionColors.TextSecondary else SubestacionColors.TextQuaternary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Editar registro",
                    color = if (networkStatus) SubestacionColors.TextSecondary else SubestacionColors.TextQuaternary,
                    fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp
                )
            }
            if (!networkStatus) {
                SubestacionType.Hint("Necesitas conexión para editar.", color = SubestacionColors.Red, modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    "MOTIVO DE LA EDICIÓN · OBLIGATORIO",
                    color = SubestacionColors.TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.4.sp
                )
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    placeholder = { Text("Ej. se corrigió el tipo de mantenimiento: la compuerta falló, fue correctivo y no preventivo.") },
                    shape = SubestacionShapes.Input,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SubestacionColors.Purple, unfocusedBorderColor = SubestacionColors.Purple),
                    modifier = Modifier.fillMaxWidth().height(92.dp)
                )
                SubestacionType.Hint(
                    if (motivoOk) "Listo. Queda en el historial del registro." else "Escribe al menos 15 caracteres ($longitud/15).",
                    color = if (motivoOk) SubestacionColors.Green else SubestacionColors.TextQuaternary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(
                        Modifier
                            .height(48.dp)
                            .clip(SubestacionShapes.Button)
                            .background(SubestacionColors.PurpleSurface)
                            .clickable { abierto = false; motivo = "" }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancelar", color = SubestacionColors.TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(SubestacionShapes.Button)
                            .background(if (motivoOk) SubestacionColors.Purple else SubestacionColors.PurpleBorderLight)
                            .clickable(enabled = motivoOk) { onNavigateToEditar(ejecucionId, motivo.trim()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Abrir formulario",
                            color = if (motivoOk) Color.White else SubestacionColors.TextQuaternary,
                            fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoBanner(texto: String, colores: Triple<Color, Color, Color>) {
    val (fg, bg, bd) = colores
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(bg)
            .border(1.5.dp, bd, RoundedCornerShape(13.dp)).padding(14.dp, 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(fg))
        Spacer(Modifier.width(9.dp))
        Text(texto, color = fg, fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun AvisoCard(texto: String, fg: Color, bg: Color, bd: Color) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg)
            .border(1.dp, bd, RoundedCornerShape(10.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SeccionEncabezado(texto: String) {
    Box(Modifier.fillMaxWidth().background(SubestacionColors.SectionHeaderBackground).padding(15.dp, 10.dp)) {
        Text(texto, color = SubestacionColors.TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp, letterSpacing = 0.7.sp)
    }
}

@Composable
private fun FilaDetalle(etiqueta: String, valor: String, ultima: Boolean = false) {
    Row(
        Modifier.fillMaxWidth()
            .then(if (!ultima) Modifier.border(0.dp, Color.Transparent) else Modifier)
            .padding(15.dp, 10.dp)
    ) {
        Text(etiqueta, color = SubestacionColors.TextMeta, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.width(104.dp))
        Text(valor, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}
