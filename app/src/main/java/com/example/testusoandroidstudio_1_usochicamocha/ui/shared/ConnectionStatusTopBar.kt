package com.example.testusoandroidstudio_1_usochicamocha.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Píldora chica (punto + texto) con el estado de conexión. Vive DENTRO de la fila del
 * header (en `actions`, junto a los íconos), no como fila propia debajo — mismo
 * espíritu que el botón de sincronizar de Subestaciones: todo el header en una sola
 * línea, sin ocupar una franja completa aparte.
 */
@Composable
fun ConnectionStatusTopBar(isConnected: Boolean) {
    val bg = if (isConnected) Color(0xFFE4F7EC) else Color(0xFFFDEEE9)
    val fg = if (isConnected) Color(0xFF1E7A4D) else Color(0xFFA83C1C)
    val text = if (isConnected) "Conectado" else "Sin conexión"

    Row(
        modifier = Modifier
            .padding(end = 6.dp)
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(fg, CircleShape)
        )
        Box(Modifier.width(6.dp))
        Text(
            text = text,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
