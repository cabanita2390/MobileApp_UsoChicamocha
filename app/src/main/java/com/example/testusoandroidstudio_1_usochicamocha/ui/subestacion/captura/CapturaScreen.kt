package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.captura

import android.Manifest
import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ActividadCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.L_ACT
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.L_MANT
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.L_MOT
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.L_RES
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.etiquetaDeEstado
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.nombreMes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionSyncIconButton
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionType
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.dashedBorder
import java.io.File
import java.util.Calendar

/** Las 3 disciplinas del selector de Contexto — solo CIVIL está habilitada en el MVP. */
private val DISCIPLINAS = listOf(
    "CIVIL" to "Civil",
    "ELECTRICO" to "Eléctrico",
    "ELECTROMECANICO" to "Electromecánico"
)

private fun nombreDisciplina(valor: String): String = DISCIPLINAS.firstOrNull { it.first == valor }?.second ?: valor

/** Iniciales para el avatar circular del responsable (ej. "Hilson Puerto" -> "HP"),
 * igual que el `respIni` del mockup. */
private fun initialesDe(nombre: String): String =
    nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        .take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturaScreen(
    networkStatus: Boolean,
    programacionId: Long = -1L,
    estacionId: Long = -1L,
    actividadId: Long = -1L,
    esInspeccion: Boolean = false,
    vencida: Boolean = false,
    mes: Int = -1,
    editandoId: Long = -1L,
    motivoEdicionInicial: String = "",
    libre: Boolean = false,
    viewModel: CapturaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCola: () -> Unit = {},
    onNavigateToPendientes: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(programacionId) {
        if (programacionId > 0L) {
            viewModel.cargarDesdeCita(programacionId, estacionId, actividadId, esInspeccion, vencida, mes)
        }
    }
    LaunchedEffect(libre) {
        if (libre) viewModel.iniciarLibre()
    }
    LaunchedEffect(editandoId) {
        if (editandoId > 0L) {
            viewModel.cargarParaEditar(editandoId)
            // El motivo de la edición se pide en la pantalla de Detalle (mock: panel
            // inline con "Abrir formulario"), no dentro de este wizard — llega ya
            // escrito por navegación.
            if (motivoEdicionInicial.isNotBlank()) viewModel.onMotivoEdicionChange(motivoEdicionInicial)
        }
    }
    // Ya no navega solo al guardar — antes esto pasaba en silencio (volvía atrás sin
    // ningún aviso, parecía que no había pasado nada). Ahora, en vez de un efecto que
    // navega, `uiState.saveCompleted` hace que el contenido de más abajo muestre la
    // pantalla de confirmación del mock (check grande + botones), y es el usuario
    // quien decide a dónde ir desde ahí.

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let { viewModel.onFotoSeleccionada(it) } }
    )
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success) tempImageUri?.let { viewModel.onFotoSeleccionada(it) } }
    )
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val file = File(context.cacheDir, "subestacion_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                tempImageUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    )

    Scaffold(
        containerColor = SubestacionColors.ScreenBackground,
        topBar = {
            Column(Modifier.background(SubestacionColors.ScreenBackground).statusBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp, 18.dp, 20.dp, 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SubestacionColors.PurpleSurface)
                            .clickable { if (!viewModel.onAnterior()) onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás",
                            tint = SubestacionColors.TextSecondary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        SubestacionType.ScreenTitle(
                            when {
                                uiState.saveCompleted -> if (uiState.esEdicion) "Cambios guardados" else "Registro guardado"
                                uiState.esEdicion -> "Editar registro"
                                else -> "Inspección y mantenimiento"
                            }
                        )
                    }
                    if (!uiState.saveCompleted && (uiState.esEdicion || uiState.programacionId != null)) {
                        Text(
                            if (uiState.esEdicion) "editando" else "prellenado",
                            color = if (uiState.esEdicion) SubestacionColors.Red else SubestacionColors.Green,
                            fontWeight = FontWeight.Bold, fontSize = 11.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                    SubestacionSyncIconButton(onClick = onNavigateToCola)
                }
                if (!uiState.saveCompleted) {
                    Column(Modifier.fillMaxWidth().padding(20.dp, 4.dp, 20.dp, 12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(uiState.nSteps) { i ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (i < uiState.currentStep) SubestacionColors.Purple else SubestacionColors.PurpleBorderLight)
                                )
                            }
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(
                            "PASO ${uiState.currentStep} DE ${uiState.nSteps} · ${uiState.stepName}",
                            color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp, letterSpacing = 0.4.sp
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (uiState.saveCompleted) return@Scaffold
            Surface(color = SubestacionColors.ScreenBackground, shadowElevation = 8.dp) {
                Column(Modifier.padding(20.dp, 12.dp, 20.dp, 20.dp)) {
                    val puedeAvanzar = viewModel.esPasoValido(uiState.currentStep)
                    val bloqueadoPorRed = uiState.esEdicion && uiState.currentStep == uiState.nSteps && !networkStatus
                    if (bloqueadoPorRed) {
                        SubestacionType.Hint("Necesitas conexión para guardar los cambios.", color = SubestacionColors.Red, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    if (!uiState.errorEdicion.isNullOrBlank()) {
                        SubestacionType.Hint(uiState.errorEdicion!!, color = SubestacionColors.Red, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    val label = when {
                        uiState.currentStep < uiState.nSteps -> "Continuar"
                        !puedeAvanzar && uiState.esEdicion -> "Falta el motivo de la edición"
                        !puedeAvanzar -> "Falta al menos una fotografía"
                        uiState.isSaving -> "Guardando…"
                        uiState.esEdicion -> "Guardar cambios"
                        networkStatus -> "Guardar y enviar"
                        else -> "Guardar en el equipo"
                    }
                    val habilitado = puedeAvanzar && !uiState.isSaving && !bloqueadoPorRed
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier
                                .height(56.dp)
                                .clip(SubestacionShapes.Button)
                                .background(SubestacionColors.PurpleSurface)
                                .clickable { if (!viewModel.onAnterior()) onNavigateBack() }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Atrás", color = SubestacionColors.TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(SubestacionShapes.Button)
                                .background(if (habilitado) SubestacionColors.Purple else SubestacionColors.PurpleBorderLight)
                                .clickable(enabled = habilitado) { viewModel.onSiguiente() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label, color = if (habilitado) Color.White else SubestacionColors.TextQuaternary,
                                fontWeight = FontWeight.ExtraBold, fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(SubestacionColors.ScreenBackground)) {
            if (uiState.saveCompleted) {
                PantallaGuardado(
                    esEdicion = uiState.esEdicion,
                    online = networkStatus,
                    onSiguientePendiente = {
                        viewModel.onConfirmacionMostrada()
                        onNavigateToPendientes()
                    },
                    onVolverInicio = {
                        viewModel.onConfirmacionMostrada()
                        onNavigateToHome()
                    }
                )
            } else if (uiState.cargandoEdicion) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SubestacionColors.Purple)
                }
            } else if (uiState.citaMode) {
                // Modo cita (3 pasos): la estación/actividad/disciplina/tipo de
                // mantenimiento ya vienen fijos de la cita — no hay paso "Actividad"
                // separado, PasoCita reemplaza a Contexto+Actividad en uno solo.
                when (uiState.currentStep) {
                    1 -> PasoCita(uiState, viewModel)
                    2 -> PasoResultado(uiState, viewModel)
                    else -> PasoEvidencia(
                        uiState = uiState,
                        onTakePhoto = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        onPickPhoto = { pickImageLauncher.launch("image/*") },
                        onRemovePhoto = { viewModel.onFotoRemovida(it) }
                    )
                }
            } else when (uiState.currentStep) {
                1 -> PasoContexto(uiState, viewModel)
                2 -> PasoActividad(uiState, viewModel)
                3 -> PasoResultado(uiState, viewModel)
                else -> PasoEvidencia(
                    uiState = uiState,
                    onTakePhoto = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    onPickPhoto = { pickImageLauncher.launch("image/*") },
                    onRemovePhoto = { viewModel.onFotoRemovida(it) }
                )
            }
        }
    }
}

@Composable
private fun SubInput(
    value: String,
    onClick: (() -> Unit)? = null,
    onValueChange: ((String) -> Unit)? = null,
    placeholder: String = "",
    icono: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    if (onClick != null) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(SubestacionShapes.Input)
                .background(Color.White)
                .border(1.5.dp, SubestacionColors.PurpleBorder, SubestacionShapes.Input)
                .clickable(onClick = onClick)
                .padding(16.dp, 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                value.ifBlank { placeholder }, color = SubestacionColors.TextPrimary,
                fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f)
            )
            if (icono != null) {
                Icon(icono, contentDescription = null, tint = SubestacionColors.Purple, modifier = Modifier.size(20.dp))
            }
        }
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange?.invoke(it) },
            placeholder = { Text(placeholder) },
            shape = SubestacionShapes.Input,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SubestacionColors.Purple,
                unfocusedBorderColor = SubestacionColors.PurpleBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Selector tipo píldora para Mes/Semana de ejecución — equivalente al `<select>` del mock. */
@Composable
private fun PeriodoDropdown(
    textoMostrado: String,
    bg: Color,
    bd: Color,
    opciones: List<Pair<Int, String>>,
    onSeleccionar: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(bg)
                .border(1.5.dp, bd, RoundedCornerShape(13.dp))
                .clickable { expanded = true }
                .padding(12.dp, 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(textoMostrado, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Elegir", tint = SubestacionColors.TextSecondary, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opciones.forEach { (valor, texto) ->
                DropdownMenuItem(text = { Text(texto) }, onClick = { onSeleccionar(valor); expanded = false })
            }
        }
    }
}

/** Campo tipo dropdown para Disciplina — mismo estilo (caja blanca, borde morado,
 * flecha a la derecha) que el resto de los campos del paso Contexto (Estación, Fecha),
 * en vez de la fila de chips que no coincidía con el mockup. */
@Composable
private fun DisciplinaField(valorSeleccionado: String, onSeleccionar: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(SubestacionShapes.Input)
                .background(Color.White)
                .border(1.5.dp, SubestacionColors.PurpleBorder, SubestacionShapes.Input)
                .clickable { expanded = true }
                .padding(16.dp, 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                nombreDisciplina(valorSeleccionado), color = SubestacionColors.TextPrimary,
                fontWeight = FontWeight.SemiBold, fontSize = 15.sp
            )
            Icon(
                Icons.Filled.ArrowDropDown, contentDescription = "Elegir disciplina",
                tint = SubestacionColors.TextSecondary, modifier = Modifier.size(20.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DISCIPLINAS.forEach { (valor, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSeleccionar(valor); expanded = false })
            }
        }
    }
}

/** Campo de Fecha de ejecución (date picker) — usado por el paso Contexto en modo
 * libre y por PasoCita en modo cita (misma fecha editable en ambos modos). */
@Composable
private fun FechaEjecucionField(fecha: String, onFechaChange: (String) -> Unit) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        SubestacionType.SectionLabel("Fecha de ejecución")
        SubInput(
            value = fecha,
            icono = Icons.Filled.CalendarMonth,
            onClick = {
                val (y, m, d) = fecha.split("-").map { it.toInt() }
                DatePickerDialog(context, { _, year, month, day ->
                    onFechaChange("%04d-%02d-%02d".format(year, month + 1, day))
                }, y, m - 1, d).apply {
                    datePicker.maxDate = Calendar.getInstance().timeInMillis
                }.show()
            }
        )
    }
}

/** Mes/semana de ejecución (dropdowns) + hint de ajuste manual — mismo bloque
 * editable en modo libre (paso Contexto) y en modo cita (PasoCita). */
@Composable
private fun PeriodoEjecucionSection(uiState: CapturaUiState, viewModel: CapturaViewModel) {
    val ajustado = uiState.periodoAjustadoManualmente
    val bg = if (ajustado) SubestacionColors.AmberBg else SubestacionColors.PurpleSurface
    val bd = if (ajustado) SubestacionColors.AmberBorder else SubestacionColors.PurpleBorder
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("MES DE EJECUCIÓN", color = SubestacionColors.TextTertiary, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, letterSpacing = 0.7.sp)
                PeriodoDropdown(
                    textoMostrado = nombreMes(uiState.mesEjecucion),
                    bg = bg, bd = bd,
                    opciones = (1..12).map { it to nombreMes(it) },
                    onSeleccionar = { viewModel.onMesManualChange(it) }
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("SEMANA DE EJECUCIÓN", color = SubestacionColors.TextTertiary, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, letterSpacing = 0.7.sp)
                PeriodoDropdown(
                    textoMostrado = "Semana ${uiState.semanaEjecucion}",
                    bg = bg, bd = bd,
                    opciones = (1..4).map { it to "Semana $it" },
                    onSeleccionar = { viewModel.onSemanaManualChange(it) }
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            SubestacionType.Hint(
                if (ajustado) "Ajustados a mano — no coinciden con la fecha." else "Se llenan solos con la fecha; cámbialos si el período de reporte es otro.",
                color = if (ajustado) SubestacionColors.Amber else SubestacionColors.TextQuaternary
            )
            if (ajustado) {
                Text(
                    "Volver al cálculo", color = SubestacionColors.Purple, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                    modifier = Modifier.clickable { viewModel.onRestablecerPeriodoAuto() }
                )
            }
        }
    }
}

@Composable
private fun PasoContexto(uiState: CapturaUiState, viewModel: CapturaViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { FechaEjecucionField(uiState.fecha, viewModel::onFechaChange) }
        item { PeriodoEjecucionSection(uiState, viewModel) }
        item {
            SubestacionType.SectionLabel("Estación", modifier = Modifier.padding(bottom = 7.dp))
            // Mismo selector de lista que al crear un registro nuevo — ya llega con la
            // estación correcta preseleccionada (resaltada), no hay que escribir nada.
            EstacionPickerField(
                estaciones = uiState.estaciones,
                estacionSeleccionadaId = uiState.estacionId,
                onEstacionSeleccionada = { viewModel.onEstacionSeleccionada(it) }
            )
        }
        item {
            SubestacionType.SectionLabel("Disciplina", modifier = Modifier.padding(bottom = 7.dp))
            DisciplinaField(
                valorSeleccionado = uiState.disciplinaSeleccionada,
                onSeleccionar = { viewModel.onDisciplinaSeleccionada(it) }
            )
            SubestacionType.Hint(
                "El catálogo de abajo es el de ${nombreDisciplina(uiState.disciplinaSeleccionada)}.",
                modifier = Modifier.padding(top = 7.dp)
            )
            if (uiState.mostrarAlertaDisciplinaEnDesarrollo) {
                AlertDialog(
                    onDismissRequest = { viewModel.onAlertaDisciplinaEnDesarrolloCerrada() },
                    title = { Text("Disciplina en desarrollo") },
                    text = {
                        Text(
                            "Aún estamos desarrollando la captura para " +
                                "${nombreDisciplina(uiState.disciplinaSeleccionadaPendiente ?: "")}. " +
                                "Por ahora solo está disponible Civil."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.onAlertaDisciplinaEnDesarrolloCerrada() }) {
                            Text("Entendido")
                        }
                    }
                )
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().clip(SubestacionShapes.Input).background(SubestacionColors.SectionHeaderBackground)
                    .border(1.5.dp, SubestacionColors.PurpleBorderLight, SubestacionShapes.Input).padding(16.dp, 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(SubestacionColors.Purple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initialesDe(uiState.responsableNombre.ifBlank { "Usuario en sesión" }),
                        color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("RESPONSABLE · USUARIO EN SESIÓN", color = SubestacionColors.TextTertiary, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, letterSpacing = 0.7.sp)
                    Text(
                        uiState.responsableNombre.ifBlank { "Usuario en sesión" },
                        color = SubestacionColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text("🔒", fontSize = 14.sp, color = SubestacionColors.TextQuaternary)
            }
        }
    }
}

@Composable
private fun ChipOption(label: String, sub: String?, seleccionado: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(SubestacionShapes.Chip)
            .background(if (seleccionado) SubestacionColors.Purple else SubestacionColors.ChipBg)
            .border(2.dp, if (seleccionado) SubestacionColors.Purple else SubestacionColors.ChipBorder, SubestacionShapes.Chip)
            .clickable(onClick = onClick)
            .padding(14.dp, 13.dp)
    ) {
        Column {
            Text(label, color = if (seleccionado) Color.White else SubestacionColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
            if (sub != null) {
                Text(
                    sub, color = if (seleccionado) Color.White.copy(alpha = 0.78f) else SubestacionColors.TextTertiary,
                    fontWeight = FontWeight.SemiBold, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/** Chips de tipo de actividad (Inspección/Mantenimiento/No programado) — usado por
 * el paso Actividad en modo libre ("Tipo de actividad") y por PasoCita en modo cita
 * ("Tipo de actividad realizada"), donde es lo único de esta sección que se captura. */
@Composable
private fun TipoActividadSection(uiState: CapturaUiState, viewModel: CapturaViewModel, titulo: String = "Tipo de actividad") {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SubestacionType.SectionLabel(titulo)
        val opciones = listOf(
            Triple("INSPECCION", "Inspección", "Se revisa y se reporta"),
            Triple("MANTENIMIENTO", "Mantenimiento", "Se interviene el activo"),
            Triple("NO_PROGRAMADO", "No programado", "Fuera del cronograma")
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            opciones.take(2).forEach { (valor, label, sub) ->
                ChipOption(label, sub, uiState.tipoActividad == valor, Modifier.weight(1f)) { viewModel.onTipoActividadChange(valor) }
            }
        }
        val (valor, label, sub) = opciones[2]
        ChipOption(label, sub, uiState.tipoActividad == valor, Modifier.fillMaxWidth(0.5f)) { viewModel.onTipoActividadChange(valor) }
    }
}

@Composable
private fun PasoActividad(uiState: CapturaUiState, viewModel: CapturaViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TipoActividadSection(uiState, viewModel) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                SubestacionType.SectionLabel("Tipo de mantenimiento")
                val opcionesMant = listOf("PREVENTIVO" to "Preventivo", "CORRECTIVO" to "Correctivo", "PREDICTIVO" to "Predictivo", "NO_PROGRAMADO" to "No programado")
                opcionesMant.chunked(2).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { (valor, label) ->
                            val on = uiState.tipoMantenimiento == valor
                            val danger = valor == "CORRECTIVO"
                            val bg = if (on) (if (danger) SubestacionColors.Red else SubestacionColors.Purple) else SubestacionColors.ChipBg
                            val fg = if (on) Color.White else SubestacionColors.TextPrimary
                            val bd = if (on) bg else SubestacionColors.ChipBorder
                            Box(
                                Modifier.clip(SubestacionShapes.Chip).background(bg).border(2.dp, bd, SubestacionShapes.Chip)
                                    .clickable { viewModel.onTipoMantenimientoChange(valor) }.padding(17.dp, 14.dp)
                            ) { Text(label, color = fg, fontWeight = FontWeight.Bold, fontSize = 13.5.sp) }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SubestacionType.SectionLabel("Actividad civil")
                Text(
                    if (uiState.modoLibre) "Usar catálogo" else "No está en el catálogo",
                    color = SubestacionColors.Purple, fontWeight = FontWeight.Bold, fontSize = 11.5.sp,
                    modifier = Modifier.clickable { viewModel.onToggleModoLibre() }
                )
            }
        }
        if (uiState.modoLibre) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    SubestacionType.Hint("Se registra como actividad no programada. Describe qué se hizo:")
                    OutlinedTextField(
                        value = uiState.descripcionLibre,
                        onValueChange = { viewModel.onDescripcionLibreChange(it) },
                        placeholder = { Text("Describa la actividad civil no programada…") },
                        shape = SubestacionShapes.Input,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SubestacionColors.Purple, unfocusedBorderColor = SubestacionColors.PurpleBorder),
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            }
        } else {
            items(uiState.actividades) { actividad: ActividadCatalogo ->
                val seleccionada = uiState.actividadId == actividad.id
                Row(
                    Modifier.fillMaxWidth().clip(SubestacionShapes.Chip)
                        .background(if (seleccionada) SubestacionColors.PurpleSurface else Color.White)
                        .border(2.dp, if (seleccionada) SubestacionColors.Purple else SubestacionColors.PurpleBorderLight, SubestacionShapes.Chip)
                        .clickable { viewModel.onActividadSeleccionada(actividad) }.padding(15.dp, 14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        Modifier.size(20.dp).clip(RoundedCornerShape(6.dp))
                            .background(if (seleccionada) SubestacionColors.Purple else Color.Transparent)
                            .border(2.dp, if (seleccionada) SubestacionColors.Purple else SubestacionColors.PurpleBorder, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (seleccionada) Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(11.dp))
                    Text(actividad.nombre, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
                }
            }
            if (uiState.actividades.isEmpty()) {
                item { SubestacionType.Hint("El catálogo de actividades aún no se ha sincronizado.", color = SubestacionColors.Red) }
            }
        }
        // Coincidencia exacta estación+actividad contra el cronograma — solo aparece
        // cuando la actividad puntual que se acaba de elegir del catálogo ya tenía una
        // cita sin resolver en esta estación. No es una lista para adivinar entre las
        // 10-20 actividades de la estación, es un hecho concreto ya verificado.
        if (uiState.citaSugerida != null) {
            item {
                val cita = uiState.citaSugerida!!
                val estadoLabel = uiState.citaSugeridaEstado?.let { etiquetaDeEstado(it).lowercase() } ?: ""
                Column(
                    Modifier.fillMaxWidth().clip(SubestacionShapes.Card).background(SubestacionColors.AmberBg)
                        .border(1.5.dp, SubestacionColors.AmberBorder, SubestacionShapes.Card).padding(14.dp)
                ) {
                    Text(
                        "Esta actividad coincide con una cita $estadoLabel del cronograma (${nombreMes(cita.mes)} ${cita.anio}). ¿Vinculamos este registro a esa cita?",
                        color = SubestacionColors.Amber, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, lineHeight = 17.sp
                    )
                    Row(Modifier.padding(top = 11.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Box(
                            Modifier
                                .height(42.dp)
                                .clip(SubestacionShapes.Button)
                                .background(Color.White)
                                .clickable { viewModel.descartarCitaSugerida() }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No, es aparte", color = SubestacionColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(SubestacionShapes.Button)
                                .background(SubestacionColors.Amber)
                                .clickable { viewModel.vincularCitaSugerida() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Vincular esta cita", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}

/** Un dato fijo de la cita, mostrado como chip de solo lectura dentro de la tarjeta
 * "Del cronograma" de PasoCita — mismo look que ChipOption pero sin `clickable`. */
@Composable
private fun LockedChip(etiqueta: String, valor: String, danger: Boolean = false, modifier: Modifier = Modifier) {
    val bg = if (danger) SubestacionColors.RedBg else SubestacionColors.PurpleSurface
    val bd = if (danger) SubestacionColors.RedBorder else Color.Transparent
    val fg = if (danger) SubestacionColors.Red else SubestacionColors.TextPrimary
    Column(
        modifier
            .clip(SubestacionShapes.Chip)
            .background(bg)
            .border(1.dp, bd, SubestacionShapes.Chip)
            .padding(12.dp, 10.dp)
    ) {
        Text(
            etiqueta, color = if (danger) SubestacionColors.Red else SubestacionColors.TextTertiary,
            fontWeight = FontWeight.SemiBold, fontSize = 9.sp, letterSpacing = 0.5.sp
        )
        Text(valor, color = fg, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

/**
 * Paso 1 en modo cita: reemplaza a Contexto+Actividad cuando el registro viene de una
 * cita real del cronograma (`citaMode`). Estación, actividad, disciplina y tipo de
 * mantenimiento se muestran fijos — no hay selector para ninguno de los dos, porque
 * ya los definió el cronograma; tocarlos aquí fue justo lo que causó el bug de campo
 * que `onActividadSeleccionada`/`onToggleModoLibre` ya corrigen (una cita marcada
 * "cumplida" sin serlo). Lo único capturable en este paso es fecha/mes/semana de
 * ejecución (reutilizando `FechaEjecucionField`/`PeriodoEjecucionSection`, igual que
 * en modo libre) y el tipo de actividad realmente realizada (`TipoActividadSection`,
 * mismos chips que en modo libre).
 *
 * La tarjeta incluye un chip "MES" con el mes que el cronograma programó para esta
 * cita (`mesProgramado`, viajando desde Cronograma/Pendientes por la ruta de
 * navegación) — deliberadamente separado del mes de ejecución editable de abajo,
 * para que se note si el técnico está registrando algo fuera de tiempo.
 */
@Composable
private fun PasoCita(uiState: CapturaUiState, viewModel: CapturaViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column(
                Modifier.fillMaxWidth().clip(SubestacionShapes.CardLarge).background(Color.White)
                    .border(1.5.dp, SubestacionColors.PurpleBorderLight, SubestacionShapes.CardLarge)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            "DEL CRONOGRAMA", color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.5.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            uiState.estacionNombre.ifBlank { "—" },
                            color = SubestacionColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                        )
                        Text(
                            uiState.actividadNombre ?: "—",
                            color = SubestacionColors.TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LockedChip("DISCIPLINA", nombreDisciplina(uiState.disciplinaSeleccionada))
                        LockedChip(
                            "MANTENIM.",
                            L_MANT[uiState.tipoMantenimiento] ?: uiState.tipoMantenimiento,
                            danger = uiState.tipoMantenimiento == "CORRECTIVO"
                        )
                        uiState.mesProgramado?.let { mes ->
                            LockedChip("MES", nombreMes(mes))
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(SubestacionShapes.Input).background(SubestacionColors.SectionHeaderBackground)
                            .padding(12.dp, 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            Modifier.size(30.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                initialesDe(uiState.responsableNombre.ifBlank { "Usuario en sesión" }),
                                color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("RESPONSABLE", color = SubestacionColors.TextTertiary, fontWeight = FontWeight.SemiBold, fontSize = 8.5.sp, letterSpacing = 0.6.sp)
                            Text(
                                uiState.responsableNombre.ifBlank { "Usuario en sesión" },
                                color = SubestacionColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp
                            )
                        }
                        Text("🔒", fontSize = 12.sp, color = SubestacionColors.TextQuaternary)
                    }
                }
            }
        }
        item { FechaEjecucionField(uiState.fecha, viewModel::onFechaChange) }
        item { PeriodoEjecucionSection(uiState, viewModel) }
        item { TipoActividadSection(uiState, viewModel, titulo = "Tipo de actividad realizada") }
    }
}

@Composable
private fun PasoResultado(uiState: CapturaUiState, viewModel: CapturaViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubestacionType.SectionLabel("Resultado")
                val opciones = listOf(
                    Triple("CONFORME", "Conforme", "Sin novedades, queda operativo"),
                    Triple("CON_HALLAZGOS", "Con hallazgos", "Funciona, pero hay algo que reportar"),
                    Triple("REQUIERE_INTERVENCION", "Requiere intervención", "")
                )
                opciones.forEach { (valor, label, hint) ->
                    val on = uiState.resultado == valor
                    val dot = when (valor) {
                        "CONFORME" -> SubestacionColors.Green
                        "CON_HALLAZGOS" -> SubestacionColors.Amber
                        else -> SubestacionColors.Red
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(SubestacionShapes.Chip)
                            .background(if (on) SubestacionColors.PurpleSurface else Color.White)
                            .border(2.dp, if (on) SubestacionColors.Purple else SubestacionColors.PurpleBorderLight, SubestacionShapes.Chip)
                            .clickable { viewModel.onResultadoChange(valor) }.padding(16.dp, 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(dot))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(label, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp)
                            if (hint.isNotEmpty()) Text(hint, color = SubestacionColors.TextTertiary, fontWeight = FontWeight.Medium, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SubestacionType.SectionLabel("Observaciones")
                    Text("${uiState.observaciones.length}/500", color = SubestacionColors.TextQuaternary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
                Text(
                    uiState.observacionesRapidasTitulo,
                    color = SubestacionColors.TextQuaternary, fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp, letterSpacing = 0.5.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    uiState.observacionesRapidas.forEach { frase ->
                        val aplicada = uiState.observaciones.contains(frase)
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                                .background(if (aplicada) SubestacionColors.PurpleSurface else Color.White)
                                .border(1.5.dp, if (aplicada) SubestacionColors.Purple else SubestacionColors.PurpleBorderLight, RoundedCornerShape(13.dp))
                                .clickable { viewModel.onObservacionRapidaClick(frase) }.padding(14.dp, 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (aplicada) "✓" else "+",
                                color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(frase, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                        }
                    }
                }
                OutlinedTextField(
                    value = uiState.observaciones,
                    onValueChange = { viewModel.onObservacionesChange(it) },
                    placeholder = { Text("Agrega detalle o escribe algo distinto…") },
                    shape = SubestacionShapes.Input,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SubestacionColors.Purple, unfocusedBorderColor = SubestacionColors.PurpleBorder),
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        }
    }
}

@Composable
private fun PasoEvidencia(
    uiState: CapturaUiState,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit
) {
    val fotoObligatoria = !uiState.esEdicion && uiState.resultado != "CONFORME" && uiState.fotos.isEmpty()
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SubestacionType.SectionLabel("Fotografías")
                if (!uiState.esEdicion) {
                    SubestacionType.Hint(
                        if (uiState.fotos.isNotEmpty()) "${uiState.fotos.size} en el equipo"
                        else if (fotoObligatoria) "Obligatoria" else "Opcional",
                        color = if (fotoObligatoria) SubestacionColors.Red else SubestacionColors.TextQuaternary
                    )
                }
            }
        }
        // Editar (PUT /ejecuciones/{id}) no acepta fotos — el backend no las incluye en
        // EjecucionEditRequest. Se avisa en vez de mostrar cámara/galería, que aquí no
        // harían nada útil.
        if (uiState.esEdicion) {
            item {
                Box(
                    Modifier.fillMaxWidth().clip(SubestacionShapes.Card).background(SubestacionColors.PurpleSurface)
                        .padding(14.dp)
                ) {
                    Text(
                        "Las fotografías no se modifican al editar — se conservan las del registro original. Puedes verlas en el detalle.",
                        color = SubestacionColors.TextSecondary, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp
                    )
                }
            }
        }
        // Cuadrícula armada a mano (no LazyVerticalGrid) para que crezca con el
        // contenido dentro del LazyColumn de la pantalla — con la grilla perezosa
        // anidada, la altura se calculaba a mano con una fórmula que redondeaba
        // para abajo (con 2, 5, 8… fotos faltaba la última fila, quedaba cortada).
        // Acá cada fila es un `item` normal, así que toda la pantalla se desplaza
        // de forma natural y siempre se ve completa.
        val celdas: List<@Composable () -> Unit> = if (uiState.esEdicion) emptyList() else buildList {
            add {
                Column(
                    Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp))
                        .background(SubestacionColors.DashedUploadBg)
                        .dashedBorder(SubestacionColors.DashedUploadBorder, cornerRadius = 14.dp)
                        .clickable(onClick = onTakePhoto),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = SubestacionColors.Purple)
                    Spacer(Modifier.height(4.dp))
                    Text("Cámara", color = SubestacionColors.Purple, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                }
            }
            add {
                Column(
                    Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(2.dp, SubestacionColors.PurpleBorderLight, RoundedCornerShape(14.dp))
                        .clickable(onClick = onPickPhoto),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = SubestacionColors.Purple)
                    Spacer(Modifier.height(4.dp))
                    Text("Galería", color = SubestacionColors.Purple, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                }
            }
            uiState.fotos.forEach { uri ->
                add {
                    Box(
                        Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(SubestacionColors.PhotoGradientStart, SubestacionColors.PhotoGradientEnd)))
                    ) {
                        AsyncImage(
                            model = uri, contentDescription = null,
                            modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(5.dp).size(22.dp).clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)).clickable { onRemovePhoto(uri) },
                            contentAlignment = Alignment.Center
                        ) { Text("×", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    }
                }
            }
        }
        celdas.chunked(3).forEach { fila ->
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    fila.forEach { celda ->
                        Box(Modifier.weight(1f)) { celda() }
                    }
                    repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        if (!uiState.esEdicion) {
            item { SubestacionType.Hint("Se guardan en el equipo y se suben solas cuando haya señal.") }
        }
        item {
            Column(
                Modifier.fillMaxWidth().clip(SubestacionShapes.CardLarge).background(Color.White)
                    .border(1.5.dp, SubestacionColors.PurpleBorderLight, SubestacionShapes.CardLarge)
            ) {
                Box(Modifier.fillMaxWidth().background(SubestacionColors.SectionHeaderBackground).padding(16.dp, 13.dp)) {
                    SubestacionType.SectionLabel("Resumen del registro")
                }
                // Sin el ID crudo de la cita/registro — no le dice nada al técnico,
                // la estación/actividad/fecha ya se muestran arriba en el resumen.
                val origen = when {
                    uiState.esEdicion -> "Corrección de un registro ya sincronizado"
                    uiState.programacionId != null -> "Prellenado desde el cronograma"
                    else -> "Registro libre, fuera del cronograma"
                }
                val resumen = buildList {
                    add("Fecha" to uiState.fecha)
                    add(
                        "Mes / semana" to "${nombreMes(uiState.mesEjecucion)} · Semana ${uiState.semanaEjecucion}" +
                            if (uiState.periodoAjustadoManualmente) " (ajustado a mano)" else ""
                    )
                    add("Responsable" to uiState.responsableNombre.ifBlank { "—" })
                    add("Estación" to uiState.estacionNombre)
                    add("Disciplina" to "Civil")
                    add("Tipo actividad" to (L_ACT[uiState.tipoActividad] ?: uiState.tipoActividad))
                    add("Mantenimiento" to (L_MANT[uiState.tipoMantenimiento] ?: uiState.tipoMantenimiento))
                    add("Actividad" to (uiState.actividadNombre ?: uiState.descripcionLibre.ifBlank { "—" }))
                    if (uiState.modoLibre) add("Motivo" to (L_MOT["NO_PROGRAMADO"] ?: "No programado"))
                    add("Resultado" to (L_RES[uiState.resultado] ?: uiState.resultado))
                    add("Observaciones" to uiState.observaciones.ifBlank { "Sin observaciones" })
                    if (!uiState.esEdicion) add("Fotografías" to "${uiState.fotos.size} adjunta(s)")
                    if (uiState.esEdicion) add("Motivo edición" to uiState.motivoEdicion)
                    add("Origen" to origen)
                }
                resumen.forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth().padding(16.dp, 11.dp)) {
                        Text(k, color = SubestacionColors.TextMeta, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, modifier = Modifier.width(112.dp))
                        Text(v, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


/**
 * Pantalla de confirmación final del mock (`isDone`): check grande, mensaje según
 * edición/conexión, nota ámbar de idempotencia, y dos botones para decidir a dónde
 * ir — reemplaza el Toast anterior, que no dejaba ninguna huella visual de que el
 * registro se había guardado.
 */
@Composable
private fun PantallaGuardado(
    esEdicion: Boolean,
    online: Boolean,
    onSiguientePendiente: () -> Unit,
    onVolverInicio: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(28.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        Box(
            Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(SubestacionColors.GreenBg),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = SubestacionColors.Green, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            if (esEdicion) "Cambios guardados" else "Registro guardado",
            color = SubestacionColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 23.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            when {
                esEdicion -> "La edición quedó registrada con su motivo en el historial del registro."
                online -> "El registro se envió correctamente y ya quedó sincronizado con el servidor."
                else -> "El registro quedó guardado en el dispositivo. Se sincronizará automáticamente en cuanto haya conexión."
            },
            color = SubestacionColors.TextTertiary,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(SubestacionShapes.Card)
                .background(SubestacionColors.AmberBg)
                .border(1.dp, SubestacionColors.AmberBorder, SubestacionShapes.Card)
                .padding(14.dp)
        ) {
            Text(
                "La sincronización es automática. Cada registro lleva un identificador único, así que reintentar nunca crea duplicados.",
                color = SubestacionColors.Amber,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Spacer(Modifier.height(28.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(SubestacionShapes.Button)
                .background(SubestacionColors.Purple)
                .clickable(onClick = onSiguientePendiente),
            contentAlignment = Alignment.Center
        ) {
            Text("Siguiente pendiente", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(SubestacionShapes.Button)
                .background(SubestacionColors.PurpleSurface)
                .clickable(onClick = onVolverInicio),
            contentAlignment = Alignment.Center
        ) {
            Text("Volver al inicio", color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}
