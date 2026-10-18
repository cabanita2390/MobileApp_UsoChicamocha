package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.OrdenAsignada
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.EstadoPill
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionColors
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionShapes
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme.SubestacionType

/**
 * Órdenes de trabajo (OT-EB) que el supervisor le asignó al operario para corregir hallazgos.
 * Van arriba del Home: son lo urgente. Tocar una abre la captura con la orden ya vinculada.
 */
@Composable
fun PrioritariasSection(ordenes: List<OrdenAsignada>, onAbrir: (OrdenAsignada) -> Unit) {
    if (ordenes.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubestacionType.SectionLabel("Prioritarias")
            Spacer(Modifier.width(8.dp))
            SubestacionType.Hint("${ordenes.size} ${if (ordenes.size == 1) "orden de trabajo" else "órdenes de trabajo"}")
        }
        ordenes.forEach { orden -> OrdenCard(orden, onClick = { onAbrir(orden) }) }
    }
}

@Composable
private fun OrdenCard(orden: OrdenAsignada, onClick: () -> Unit) {
    val alta = orden.prioridad == "ALTA"
    Column(
        Modifier
            .fillMaxWidth()
            .clip(SubestacionShapes.CardLarge)
            .background(SubestacionColors.CardBackground)
            .border(1.5.dp, if (alta || orden.vencida) SubestacionColors.RedBorder else SubestacionColors.AmberBorder, SubestacionShapes.CardLarge)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(orden.consecutivo, color = SubestacionColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            EstadoPill(
                if (alta) "Prioridad alta" else "Prioridad media",
                bg = if (alta) SubestacionColors.RedBg else SubestacionColors.AmberBg,
                fg = if (alta) SubestacionColors.Red else SubestacionColors.Amber
            )
            Spacer(Modifier.weight(1f))
            Text(
                (if (orden.vencida) "Venció " else "Vence ") + fechaCorta(orden.fechaLimite),
                color = if (orden.vencida) SubestacionColors.Red else SubestacionColors.TextTertiary,
                fontWeight = FontWeight.Bold, fontSize = 11.sp
            )
        }
        SubestacionType.CardTitle(orden.estacionNombre)
        Text(
            orden.instrucciones, color = SubestacionColors.TextSecondary, fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        orden.motivoDevolucion?.let {
            Text(
                "Devuelta: $it", color = SubestacionColors.Red, fontWeight = FontWeight.Bold, fontSize = 11.5.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clip(SubestacionShapes.Pill).background(SubestacionColors.RedBg).padding(8.dp, 5.dp)
            )
        }
        Text("Registrar corrección ›", color = SubestacionColors.Purple, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
    }
}

/** "2026-10-25" → "25/10". */
internal fun fechaCorta(iso: String): String =
    iso.split("-").takeIf { it.size == 3 }?.let { "${it[2]}/${it[1]}" } ?: iso
