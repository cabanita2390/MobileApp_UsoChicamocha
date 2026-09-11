package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.pendientes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.CitaUi
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.EstadoCita
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.etiquetaDeEstado
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.tonoDeEstado
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.AccentListCard
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.EstadoPill
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionType
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendientesScreen(
    networkStatus: Boolean,
    viewModel: PendientesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCaptura: (programacionId: Long, estacionId: Long, actividadId: Long, esInspeccion: Boolean, vencida: Boolean, mes: Int) -> Unit,
    onNavigateToDetalle: (ejecucionId: Long) -> Unit,
    onNavigateToCola: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Recarga al volver a esta pantalla (ej. tras registrar una visita desde acá) —
    // si no, se queda con los datos de antes y parece que el registro no se guardó.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.cargar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            Column {
                SubestacionTopBar(
                    title = "Pendientes",
                    subtitle = "Vencidas y realizadas, agrupado por mes",
                    onBack = onNavigateBack,
                    onSyncClick = onNavigateToCola,
                    networkStatus = networkStatus
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SubestacionColors.ScreenBackground
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(SubestacionColors.ScreenBackground)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    FiltroPendientes.POR_HACER to Triple("Por hacer", SubestacionColors.Purple, SubestacionColors.PurpleSurface),
                    FiltroPendientes.VENCIDAS to Triple("Vencidas", SubestacionColors.Red, SubestacionColors.RedBg),
                    FiltroPendientes.REALIZADAS to Triple("Realizadas", SubestacionColors.Green, SubestacionColors.GreenBg)
                ).forEach { (filtro, spec) ->
                    val (label, fg, bg) = spec
                    val selected = uiState.filtro == filtro
                    Box(
                        Modifier
                            .clip(SubestacionShapes.PillRound)
                            .background(if (selected) bg else SubestacionColors.ChipBg)
                            .border(
                                1.5.dp,
                                if (selected) fg else SubestacionColors.ChipBorder,
                                SubestacionShapes.PillRound
                            )
                            .clickable { viewModel.onFiltroChange(filtro) }
                            .padding(horizontal = 15.dp, vertical = 9.dp)
                    ) {
                        Text(
                            label,
                            color = if (selected) fg else SubestacionColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SubestacionColors.Purple)
                }
                uiState.error != null -> EstadoVacioPendientes(
                    mensaje = uiState.error ?: "No se pudieron cargar los pendientes.",
                    onReintentar = { viewModel.cargar() }
                )
                uiState.grupos.isEmpty() -> EstadoVacioPendientes(
                    mensaje = when (uiState.filtro) {
                        FiltroPendientes.REALIZADAS -> "Aún no hay actividades ejecutadas."
                        FiltroPendientes.VENCIDAS -> "Ningún mes cerrado quedó con actividades sin realizar."
                        FiltroPendientes.POR_HACER -> "Nada pendiente en este filtro. Todo al día."
                    }
                )
                else -> LazyColumn(
                    // key(uiState.filtro) recrea el LazyListState al cambiar de
                    // filtro, para que la lista vuelva arriba — si no, se quedaba
                    // con el scroll de la pestaña anterior y el contenido nuevo
                    // quedaba fuera de la vista, pareciendo que no había pasado nada.
                    state = remember(uiState.filtro) { androidx.compose.foundation.lazy.LazyListState() },
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    uiState.grupos.forEach { grupo ->
                        item(key = "mes_${grupo.anio}_${grupo.mes}") {
                            Row(
                                Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Text(
                                    grupo.label.uppercase(),
                                    color = SubestacionColors.TextMeta,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 1.2.sp
                                )
                                Box(Modifier.weight(1f).height(1.dp).background(SubestacionColors.PurpleBorderLight))
                                Text(
                                    "${grupo.citas.size} actividad${if (grupo.citas.size == 1) "" else "es"}",
                                    color = SubestacionColors.TextQuaternary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        items(grupo.citas, key = { "cita_${it.cita.programacionId}" }) { citaUi ->
                            PendienteRow(citaUi) {
                                when (citaUi.estado) {
                                    EstadoCita.PENDIENTE, EstadoCita.VENCIDA -> onNavigateToCaptura(
                                        citaUi.cita.programacionId,
                                        citaUi.cita.estacionId,
                                        citaUi.cita.actividadId,
                                        citaUi.cita.actividadNombre.startsWith("Inspecci", ignoreCase = true),
                                        citaUi.estado == EstadoCita.VENCIDA,
                                        citaUi.cita.mes
                                    )
                                    // Una ejecución no programada ya trae su propio ejecucionId (no
                                    // tiene programacionId real: ver CitaProgramada/EjecucionNoProgramadaCacheEntity),
                                    // así que puede navegar directo a Detalle sin el paso de
                                    // "resolver por programación" que sí necesitan las citas del cronograma.
                                    EstadoCita.EJECUTADA -> {
                                        val ejecucionId = citaUi.cita.ejecucionId
                                        if (ejecucionId != null) {
                                            onNavigateToDetalle(ejecucionId)
                                        } else {
                                            viewModel.resolverDetalle(
                                                citaUi.cita.programacionId,
                                                onResuelto = { id -> onNavigateToDetalle(id) },
                                                onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                            )
                                        }
                                    }
                                    EstadoCita.PROGRAMADA -> Unit
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PendienteRow(citaUi: CitaUi, onClick: () -> Unit) {
    val tono = tonoDeEstado(citaUi.estado)
    AccentListCard(
        accentColor = tono.accent,
        borderColor = tono.border,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EstadoPill(etiquetaDeEstado(citaUi.estado), tono.pillBg, tono.pillFg)
                // Actividad civil hecha fuera de cronograma (sin cita asociada) — se
                // distingue de una cita programada cumplida, que es el caso normal.
                if (!citaUi.cita.esProgramada) {
                    Spacer(Modifier.width(6.dp))
                    EstadoPill("No programada", SubestacionColors.ChipBg, SubestacionColors.TextQuaternary)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    citaUi.cita.estacionNombre,
                    color = SubestacionColors.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                citaUi.cita.actividadNombre,
                color = SubestacionColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val meta = when (citaUi.estado) {
                    EstadoCita.EJECUTADA -> "Registro completo"
                    else -> "${citaUi.cita.mes}/${citaUi.cita.anio}"
                }
                SubestacionType.CardMeta(meta)
                val cta = when (citaUi.estado) {
                    EstadoCita.EJECUTADA -> "Ver detalle"
                    else -> "Registrar"
                }
                Text("$cta ›", color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EstadoVacioPendientes(mensaje: String, onReintentar: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(mensaje, textAlign = TextAlign.Center, color = SubestacionColors.TextQuaternary)
        if (onReintentar != null) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onReintentar) { Text("Reintentar", color = SubestacionColors.Purple) }
        }
    }
}
