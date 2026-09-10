package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.cola

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.EstadoPill
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionType
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColaScreen(
    networkStatus: Boolean,
    viewModel: ColaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val pendientes by viewModel.pendientes.collectAsState()
    val confirmadosSesion by viewModel.confirmadosSesion.collectAsState()

    Scaffold(
        topBar = {
            Column {
                SubestacionTopBar(
                    title = "Cola de sincronización",
                    subtitle = "Registros locales sin enviar todavía",
                    onBack = onNavigateBack,
                    onSyncClick = null,
                    networkStatus = networkStatus
                )
            }
        },
        containerColor = SubestacionColors.ScreenBackground
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().background(SubestacionColors.ScreenBackground).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            EstadoSyncBanner(networkStatus, pendientes.size)

            Button(
                onClick = { viewModel.sincronizarAhora() },
                enabled = networkStatus,
                shape = SubestacionShapes.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SubestacionColors.Purple,
                    disabledContainerColor = SubestacionColors.PurpleBorderLight
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Sincronizar ahora", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }

            if (pendientes.isEmpty()) {
                Column(
                    Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).background(SubestacionColors.GreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = SubestacionColors.Green, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Cola vacía", color = SubestacionColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (confirmadosSesion > 0)
                            "$confirmadosSesion registro(s) confirmados en esta sesión y retirados de la cola."
                        else
                            "No hay nada esperando. Los registros aparecen aquí solo mientras se suben.",
                        textAlign = TextAlign.Center,
                        color = SubestacionColors.TextTertiary,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.weight(1f)) {
                    items(pendientes, key = { it.uuidCliente }) { ejecucion -> ColaItem(ejecucion) }
                }
            }

            // Nota fija del mock: esta pantalla es solo una vitrina de "lo que falta
            // subir", no guarda ningún registro histórico de sincronizaciones pasadas.
            Text(
                "Esta pantalla no guarda historial: al confirmarse, el registro sale de la cola y queda en su estación.",
                textAlign = TextAlign.Center,
                color = SubestacionColors.TextTertiary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}

private data class TonoBanner(val texto: String, val bg: Color, val border: Color, val dot: Color)

@Composable
private fun EstadoSyncBanner(networkStatus: Boolean, pendientesCount: Int) {
    val tono = when {
        !networkStatus -> TonoBanner(
            "Sin señal — se guarda en el equipo y se envía solo al reconectar",
            SubestacionColors.AmberBg, SubestacionColors.AmberBorder, SubestacionColors.Amber
        )
        pendientesCount > 0 -> TonoBanner(
            "Con señal — enviando $pendientesCount registro(s) automáticamente",
            SubestacionColors.GreenBg, SubestacionColors.GreenBorder, SubestacionColors.Green
        )
        else -> TonoBanner(
            "Con señal — todo sincronizado", SubestacionColors.GreenBg, SubestacionColors.GreenBorder, SubestacionColors.Green
        )
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(SubestacionShapes.CardLarge)
            .background(tono.bg)
            .border(1.5.dp, tono.border, SubestacionShapes.CardLarge)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(11.dp).clip(CircleShape).background(tono.dot))
        Spacer(Modifier.width(12.dp))
        Text(tono.texto, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun ColaItem(ejecucion: Ejecucion) {
    val (etiqueta, pillBg, pillFg) = when {
        ejecucion.syncFallido -> Triple("Error", SubestacionColors.RedBg, SubestacionColors.Red)
        ejecucion.isSyncing -> Triple("Enviando", SubestacionColors.PurpleSurface, SubestacionColors.Purple)
        ejecucion.isSynced -> Triple("Enviado", SubestacionColors.GreenBg, SubestacionColors.Green)
        else -> Triple("En cola", SubestacionColors.AmberBg, SubestacionColors.Amber)
    }
    // Mock: detalle = "{fotos} foto(s) · {texto según estado}" — siempre da contexto,
    // no solo cuando falla.
    val textoEstado = when {
        ejecucion.syncFallido -> "falló, se reintenta solo"
        ejecucion.isSyncing -> "subiendo ahora…"
        ejecucion.isSynced -> "confirmado por el servidor"
        else -> "en cola, esperando señal"
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(SubestacionShapes.CardLarge)
            .background(SubestacionColors.CardBackground)
            .border(1.5.dp, SubestacionColors.PurpleBorderLight, SubestacionShapes.CardLarge)
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EstadoPill(etiqueta, pillBg, pillFg)
            Spacer(Modifier.width(8.dp))
            SubestacionType.Hint("${ejecucion.fecha} · ${ejecucion.estacionNombre}")
        }
        Spacer(Modifier.height(9.dp))
        Text(
            ejecucion.actividadNombre ?: ejecucion.descripcionLibre ?: "Sin actividad",
            color = SubestacionColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            maxLines = 1
        )
        Spacer(Modifier.height(9.dp))
        SubestacionType.Hint(
            "${ejecucion.fotosCount} foto(s) · $textoEstado",
            color = if (ejecucion.syncFallido) SubestacionColors.Red else SubestacionColors.TextQuaternary
        )
    }
}
