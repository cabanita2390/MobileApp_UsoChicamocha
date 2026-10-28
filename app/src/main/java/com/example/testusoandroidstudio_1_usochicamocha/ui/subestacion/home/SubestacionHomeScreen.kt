package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionType
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionTopBar

/**
 * Hub de "Subestaciones" — análogo a VehiculoMainScreen/MotoHubScreen.
 *
 * Coincide con el mock real ("Formulario Civil en pasos/Subestaciones Civil.dc.html",
 * líneas ~33-93 = `isHome`): tarjeta "hero" con % de cumplimiento del mes, y 4
 * tarjetas de navegación (Registrar visita, Cronograma, Pendientes, Realizadas) con
 * icono cuadrado de color semántico, borde tenue del mismo color, título/subtítulo
 * con la tipografía de `SubestacionType`, flecha morada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubestacionHomeScreen(
    networkStatus: Boolean,
    viewModel: SubestacionHomeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCaptura: () -> Unit,
    onNavigateToCronograma: () -> Unit,
    onNavigateToPendientes: () -> Unit,
    onNavigateToRealizadas: () -> Unit = {},
    onNavigateToCola: () -> Unit,
    onNavigateToOrden: (Long) -> Unit = {},
    onNavigateToCronogramaDisciplina: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recarga el hero (% de cumplimiento, pendientes, vencidas) al volver a esta
    // pantalla — si no, se queda con los datos de antes tras registrar una visita.
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
            androidx.compose.foundation.layout.Column {
                SubestacionTopBar(
                    title = "Estaciones de Bombeo",
                    subtitle = if (uiState.responsableNombre.isNotBlank()) "Todas las disciplinas · ${uiState.responsableNombre}" else "Todas las disciplinas",
                    onBack = onNavigateBack,
                    onSyncClick = onNavigateToCola,
                    networkStatus = networkStatus
                )
            }
        },
        containerColor = SubestacionColors.ScreenBackground
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SubestacionColors.ScreenBackground)
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            SubestacionHeroCard(
                uiState = uiState,
                onVerPendientes = onNavigateToPendientes,
                onVerCronograma = onNavigateToCronograma,
                onVerDisciplina = onNavigateToCronogramaDisciplina
            )
            // Las correcciones asignadas van debajo del avance del mes.
            PrioritariasSection(ordenes = uiState.ordenes, onAbrir = { onNavigateToOrden(it.id) })

            SubestacionNavCard(
                title = "Cronograma del mes",
                subtitle = if (uiState.estacionesTotal > 0) "Las ${uiState.estacionesTotal} estaciones · busca o cambia de mes"
                    else "Busca una estación o cambia de mes",
                icon = Icons.Filled.CalendarMonth,
                iconBg = SubestacionColors.PurpleSurface,
                iconTint = SubestacionColors.Purple,
                borderColor = SubestacionColors.PurpleBorderLight,
                onClick = onNavigateToCronograma
            )
            SubestacionNavCard(
                title = "Pendientes por realizar",
                subtitle = "${uiState.pendientesDelMes} de ${uiState.mesLabel} · ${uiState.vencidas} de meses cerrados",
                icon = Icons.Filled.PendingActions,
                iconBg = SubestacionColors.RedBg,
                iconTint = SubestacionColors.Red,
                borderColor = SubestacionColors.RedBorder,
                onClick = onNavigateToPendientes
            )
            SubestacionNavCard(
                title = "Actividades realizadas",
                subtitle = "${uiState.realizadasCount} registros con evidencia y observaciones",
                icon = Icons.Filled.CheckCircle,
                iconBg = SubestacionColors.GreenBg,
                iconTint = SubestacionColors.Green,
                borderColor = SubestacionColors.GreenBorder,
                onClick = onNavigateToRealizadas
            )
            SubestacionNavCard(
                title = "Actividad no programada",
                subtitle = "Registro libre, fuera del cronograma",
                icon = Icons.Filled.AddCircle,
                iconBg = SubestacionColors.PurpleSurface,
                iconTint = SubestacionColors.Purple,
                borderColor = SubestacionColors.PurpleBorderLight,
                onClick = onNavigateToCaptura
            )
        }
    }
}

/**
 * Tarjeta "hero" del Home pensada para el operario de campo: cuántas citas le faltan este mes y
 * cuántos días quedan, el avance en una barra simple, lo que falta en cada disciplina (abre el
 * Cronograma filtrado) y, si hay, lo que quedó sin hacer de meses cerrados (lo urgente).
 */
@Composable
private fun SubestacionHeroCard(
    uiState: SubestacionHomeUiState,
    onVerPendientes: () -> Unit,
    onVerCronograma: () -> Unit,
    onVerDisciplina: (String) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(SubestacionColors.Purple, SubestacionColors.PurpleDark)))
            .padding(18.dp)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "${uiState.mesLabel.uppercase()} ${uiState.anio}",
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.4.sp
                )
                Row(
                    Modifier.fillMaxWidth().clickable(onClick = onVerCronograma),
                    verticalAlignment = Alignment.Bottom
                ) {
                    SubestacionType.HeroNumber("${uiState.pendientesDelMes}")
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "citas por hacer\neste mes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.weight(1f).padding(bottom = 4.dp)
                    )
                    Box(
                        Modifier
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            when (uiState.diasRestantes) {
                                0 -> "Último día"
                                1 -> "Queda 1 día"
                                else -> "Quedan ${uiState.diasRestantes} días"
                            },
                            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(uiState.porcentaje / 100f)
                                .height(9.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF7EE2A8))
                        )
                    }
                    Text(
                        "${uiState.ejecutadasDelMes} de ${uiState.programadasDelMes} ya ejecutadas",
                        color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold, fontSize = 11.sp
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.porHacerPorDisciplina.forEach { (disciplina, porHacer) ->
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { onVerDisciplina(disciplina) }
                                .padding(horizontal = 13.dp, vertical = 11.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                if (disciplina == "ELECTRICO") {
                                    Icon(
                                        androidx.compose.material.icons.Icons.Filled.Bolt, contentDescription = null,
                                        tint = Color.White, modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.nombreDisciplina(disciplina),
                                    color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("›", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                            Text("$porHacer", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
                            Text("por hacer", color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)
                        }
                    }
                }
                if (uiState.vencidas > 0) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable(onClick = onVerPendientes)
                            .padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(9.dp).clip(RoundedCornerShape(5.dp)).background(SubestacionColors.Red))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${uiState.vencidas} sin hacer de meses cerrados",
                            color = SubestacionColors.Red, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text("Ver ›", color = SubestacionColors.Red, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubestacionNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SubestacionShapes.CardLarge)
            .background(SubestacionColors.CardBackground)
            .border(1.5.dp, borderColor, SubestacionShapes.CardLarge)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            SubestacionType.CardTitle(title)
            Spacer(Modifier.height(3.dp))
            SubestacionType.CardMeta(subtitle)
        }
        Text("›", color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
    }
}
