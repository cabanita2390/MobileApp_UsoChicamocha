package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar

/**
 * Header universal del mock (`Formulario Civil en pasos/Subestaciones Civil.dc.html`,
 * líneas ~22-31): botón atrás cuadrado morado a la izquierda, título+subtítulo al
 * centro, botón de sincronización (⇅) cuadrado morado a la derecha con una píldora
 * roja de conteo si hay registros pendientes — igual en TODAS las pantallas de
 * Subestaciones, no un TopAppBar distinto por pantalla. `onSyncClick` en null oculta
 * el botón (se usa así en la propia pantalla de Cola).
 */
@Composable
fun SubestacionTopBar(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)?,
    onSyncClick: (() -> Unit)?,
    networkStatus: Boolean? = null,
    modifier: Modifier = Modifier
) {
    val pendingCount = if (onSyncClick != null) {
        val badgeViewModel: PendingSyncBadgeViewModel = hiltViewModel()
        val count by badgeViewModel.pendingCount.collectAsState()
        count
    } else 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SubestacionColors.ScreenBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            SubestacionSquareIconButton(
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = SubestacionColors.TextSecondary) },
                onClick = onBack
            )
        }
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            SubestacionType.ScreenTitle(title)
            SubestacionType.ScreenSubtitle(subtitle, modifier = Modifier.padding(top = 2.dp))
        }
        if (networkStatus != null) {
            ConnectionStatusTopBar(isConnected = networkStatus)
        }
        if (onSyncClick != null) {
            SubestacionSquareIconButton(
                icon = { Icon(Icons.Filled.SwapVert, contentDescription = "Cola de sincronización", tint = SubestacionColors.Purple) },
                onClick = onSyncClick,
                badgeCount = pendingCount
            )
        }
    }
}

/** Expuesto para pantallas con header custom (ej. el wizard de Captura, que agrega un flag de estado entre el título y este botón). */
@Composable
fun SubestacionSyncIconButton(onClick: () -> Unit) {
    val badgeViewModel: PendingSyncBadgeViewModel = hiltViewModel()
    val count by badgeViewModel.pendingCount.collectAsState()
    SubestacionSquareIconButton(
        icon = { Icon(Icons.Filled.SwapVert, contentDescription = "Cola de sincronización", tint = SubestacionColors.Purple) },
        onClick = onClick,
        badgeCount = count
    )
}

@Composable
private fun SubestacionSquareIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Box(
        Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SubestacionColors.PurpleSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon()
        if (badgeCount > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(SubestacionColors.Danger),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (badgeCount > 9) "9+" else badgeCount.toString(),
                    color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp
                )
            }
        }
    }
}
