package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.cronograma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.colorDeEstado
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.etiquetaDeEstado
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.nombreMes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.tonoDeEstado
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.AccentListCard
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.EstadoPill
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionType
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.dashedBorder
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CronogramaScreen(
    networkStatus: Boolean,
    viewModel: CronogramaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCaptura: (programacionId: Long, estacionId: Long, actividadId: Long, esInspeccion: Boolean, vencida: Boolean, mes: Int) -> Unit,
    onNavigateToDetalle: (ejecucionId: Long) -> Unit,
    onNavigateToCapturaLibre: () -> Unit = {},
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
                    title = "Cronograma",
                    subtitle = "Citas por estación · busca o cambia de mes",
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
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    placeholder = { Text("Buscar…") },
                    singleLine = true,
                    shape = SubestacionShapes.Input,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SubestacionColors.Purple,
                        unfocusedBorderColor = SubestacionColors.PurpleBorder,
                        focusedContainerColor = SubestacionColors.CardBackground,
                        unfocusedContainerColor = SubestacionColors.CardBackground
                    ),
                    modifier = Modifier.weight(1f)
                )
                MesSelector(
                    anio = uiState.anio,
                    mes = uiState.mes,
                    onSeleccionar = { a, m -> viewModel.irAMes(a, m) }
                )
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SubestacionColors.Purple)
                }
                uiState.error != null -> EstadoVacio(
                    mensaje = uiState.error ?: "No se pudo cargar el cronograma.",
                    onReintentar = { viewModel.cargar() }
                )
                uiState.grupos.isEmpty() -> EstadoVacio(
                    mensaje = if (uiState.query.isBlank())
                        "No hay citas Civil programadas en ${nombreMes(uiState.mes)} ${uiState.anio}."
                    else "Nada coincide con \"${uiState.query}\"."
                )
                else -> LazyColumn(
                    // Recrea el LazyListState al cambiar de mes, para que la lista
                    // vuelva arriba — si no, se queda con el scroll del mes anterior
                    // y el contenido nuevo queda fuera de la vista.
                    state = remember(uiState.mes, uiState.anio) { androidx.compose.foundation.lazy.LazyListState() },
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    uiState.grupos.forEach { grupo ->
                        item(key = "header_${grupo.estacionId}") {
                            Row(
                                Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(8.dp).clip(CircleShape)
                                        .background(
                                            // Mismo orden que estStatus() del mock: rojo si hay
                                            // alguna vencida, si no morado si hay alguna pendiente,
                                            // si no verde cuando todas están ejecutadas.
                                            when {
                                                grupo.citas.any { it.estado == EstadoCita.VENCIDA } -> SubestacionColors.Red
                                                grupo.citas.any { it.estado == EstadoCita.PENDIENTE } -> SubestacionColors.Purple
                                                grupo.citas.all { it.estado == EstadoCita.EJECUTADA } -> SubestacionColors.Green
                                                else -> SubestacionColors.Purple
                                            }
                                        )
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    grupo.estacionNombre,
                                    color = SubestacionColors.TextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    buildString {
                                        append(if (grupo.estacionTipo == "BOMBEO") "Bombeo" else "Complementaria")
                                        val frec = grupo.estacionFrecuencia
                                        if (frec != null) {
                                            append(" · ")
                                            append(if (frec == "TRIMESTRAL") "Trimestral" else "Anual")
                                        }
                                    },
                                    color = SubestacionColors.TextMeta,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        items(grupo.citas, key = { "cita_${it.cita.programacionId}" }) { citaUi ->
                            CitaRow(citaUi) {
                                when (citaUi.estado) {
                                    EstadoCita.PENDIENTE, EstadoCita.VENCIDA -> onNavigateToCaptura(
                                        citaUi.cita.programacionId,
                                        citaUi.cita.estacionId,
                                        citaUi.cita.actividadId,
                                        citaUi.cita.actividadNombre.startsWith("Inspecci", ignoreCase = true),
                                        citaUi.estado == EstadoCita.VENCIDA,
                                        citaUi.cita.mes
                                    )
                                    EstadoCita.EJECUTADA -> viewModel.resolverDetalle(
                                        citaUi.cita.programacionId,
                                        onResuelto = { id -> onNavigateToDetalle(id) },
                                        onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                    )
                                    EstadoCita.PROGRAMADA -> scope.launch {
                                        snackbarHostState.showSnackbar("Todavía no toca — es de un mes futuro")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            "+ Registrar algo que no está en el cronograma",
                            color = SubestacionColors.Purple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                                .clip(SubestacionShapes.CardLarge)
                                .background(SubestacionColors.PurpleTint)
                                .dashedBorder(SubestacionColors.PurpleBorder, cornerRadius = 16.dp)
                                .clickable { onNavigateToCapturaLibre() }
                                .padding(16.dp)
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CitaRow(citaUi: CitaUi, onClick: () -> Unit) {
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
                Spacer(Modifier.width(8.dp))
                SubestacionType.Hint(metaDeCita(citaUi))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                citaUi.cita.actividadNombre,
                color = SubestacionColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubestacionType.CardMeta(if (citaUi.estado == EstadoCita.EJECUTADA) "Registro completo con evidencia" else "Sin registrar")
                val cta = when (citaUi.estado) {
                    EstadoCita.EJECUTADA -> "Ver detalle"
                    EstadoCita.VENCIDA -> "Registrar ya"
                    else -> "Registrar"
                }
                Text("$cta ›", color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp)
            }
        }
    }
}

/** Texto de contexto junto a la píldora de estado — equivalente a `c.meta` del mock
 *  ("Pendiente este mes"/"Ejecutada DD/MM"/"No se realizó en el mes"). El backend no
 *  expone la fecha exacta de ejecución en el cumplimiento (solo `cumple`/`ejecutado`),
 *  así que para EJECUTADA se usa el mes de la cita en vez de una fecha puntual. */
private fun metaDeCita(citaUi: CitaUi): String = when (citaUi.estado) {
    EstadoCita.PENDIENTE -> "Pendiente este mes"
    EstadoCita.VENCIDA -> "No se realizó en el mes"
    EstadoCita.EJECUTADA -> "Ejecutada en ${nombreMes(citaUi.cita.mes)}"
    EstadoCita.PROGRAMADA -> "Aún no toca"
}

/** Selector de mes compacto tipo píldora morada — equivalente al `<select>` del mock. */
@Composable
private fun MesSelector(anio: Int, mes: Int, onSeleccionar: (Int, Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .width(118.dp)
                .clip(SubestacionShapes.Input)
                .background(SubestacionColors.PurpleSurface)
                .border(1.5.dp, SubestacionColors.PurpleBorder, SubestacionShapes.Input)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${nombreMes(mes).take(3)} $anio",
                color = SubestacionColors.TextSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.5.sp
            )
            Icon(
                Icons.Filled.ArrowDropDown, contentDescription = "Elegir mes",
                tint = SubestacionColors.TextSecondary, modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val hoy = remember { YearMonth.now() }
            // 6 meses hacia atrás y 3 hacia adelante — el mock mostraba 4 meses fijos
            // de su propia demo, acá se genera una ventana real alrededor de hoy.
            (-6..3).map { offset -> hoy.plusMonths(offset.toLong()) }.forEach { ym ->
                DropdownMenuItem(
                    text = { Text("${nombreMes(ym.monthValue)} ${ym.year}") },
                    onClick = {
                        onSeleccionar(ym.year, ym.monthValue)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EstadoVacio(mensaje: String, onReintentar: (() -> Unit)? = null) {
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
