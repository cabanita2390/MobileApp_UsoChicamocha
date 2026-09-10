package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paleta, tipografía y formas sacadas directamente de
 * "Formulario Civil en pasos/Subestaciones Civil.dc.html" (el diseño de Claude Design
 * ya cerrado con el backend). No es un MaterialTheme nuevo para toda la app — es un
 * set de tokens que solo usan las pantallas de `ui/subestacion/`, para no afectar
 * Form/Vehículo/Moto que ya tienen su propio estilo (Material3 default).
 *
 * La tipografía del mock usa "Manrope" (Google Fonts) — se usa acá la fuente sans
 * default de la plataforma en su lugar (misma jerarquía de pesos/tamaños), para no
 * depender de Google Play Services / fuentes descargables en tiempo de ejecución en
 * un app de campo. Si más adelante se agrega Manrope de verdad, alcanza con pasar un
 * `fontFamily` a los `Text` de `SubestacionType` — no hay que tocar nada más.
 */
object SubestacionColors {
    val Purple = Color(0xFF5B3FA0)
    val PurpleDark = Color(0xFF40296F)
    val PurpleSurface = Color(0xFFEFE9F6)
    val PurpleSurfaceHover = Color(0xFFE3DAEE)
    val PurpleBorder = Color(0xFFDED3EE)
    val PurpleBorderLight = Color(0xFFE6E1EE)
    val PurpleTint = Color(0xFFF7F3FC)

    val PageBackground = Color(0xFFF0EEE9)
    val ScreenBackground = Color(0xFFFBF9FD)
    val CardBackground = Color(0xFFFFFFFF)
    val SectionHeaderBackground = Color(0xFFF3F1F7)
    val Divider = Color(0xFFF0EDF5)

    val TextPrimary = Color(0xFF1F1B2E)
    val TextSecondary = Color(0xFF4A4360)
    val TextTertiary = Color(0xFF7A7290)
    val TextQuaternary = Color(0xFF9A93AB)
    val TextMeta = Color(0xFF8B8399)

    val Green = Color(0xFF1E7A4D)
    val GreenBg = Color(0xFFE4F7EC)
    val GreenBorder = Color(0xFFBFE6CF)

    val Amber = Color(0xFF7A5A12)
    val AmberBg = Color(0xFFFDF6E6)
    val AmberBorder = Color(0xFFF0E0BD)

    val Red = Color(0xFFA83C1C)
    val RedBg = Color(0xFFFDEEE9)
    val RedBorder = Color(0xFFF3CBBD)

    val ChipBg = Color(0xFFF7F5FA)
    val ChipBorder = Color(0xFFE2DCEE)

    val PhotoGradientStart = Color(0xFFCFC4E2)
    val PhotoGradientEnd = Color(0xFFA294C4)

    val DashedUploadBorder = Color(0xFFC9BCE0)
    val DashedUploadBg = Color(0xFFF7F3FC)

    val Danger = Color(0xFFE0492F)
}

object SubestacionShapes {
    val Card = RoundedCornerShape(16.dp)
    val CardLarge = RoundedCornerShape(18.dp)
    val Input = RoundedCornerShape(13.dp)
    val Button = RoundedCornerShape(16.dp)
    val Pill = RoundedCornerShape(7.dp)
    val PillRound = RoundedCornerShape(20.dp)
    val Chip = RoundedCornerShape(14.dp)
}

/** Jerarquía tipográfica 1:1 con los `font:` inline del mock (peso/tamaño/color). */
object SubestacionType {
    @Composable
    fun ScreenTitle(text: String, modifier: Modifier = Modifier) = Text(
        text, modifier = modifier, color = SubestacionColors.TextPrimary,
        fontWeight = FontWeight.ExtraBold, fontSize = 19.sp
    )

    @Composable
    fun ScreenSubtitle(text: String, modifier: Modifier = Modifier) = Text(
        text, modifier = modifier, color = SubestacionColors.TextTertiary,
        fontWeight = FontWeight.Medium, fontSize = 11.5.sp
    )

    /** Etiqueta de sección tipo "FECHA DE EJECUCIÓN" / "TIPO DE ACTIVIDAD". */
    @Composable
    fun SectionLabel(text: String, modifier: Modifier = Modifier) = Text(
        text.uppercase(), modifier = modifier, color = SubestacionColors.TextSecondary,
        fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, letterSpacing = 0.4.sp
    )

    @Composable
    fun CardTitle(text: String, modifier: Modifier = Modifier, color: Color = SubestacionColors.TextPrimary) = Text(
        text, modifier = modifier, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
    )

    @Composable
    fun CardMeta(text: String, modifier: Modifier = Modifier) = Text(
        text, modifier = modifier, color = SubestacionColors.TextTertiary,
        fontWeight = FontWeight.Medium, fontSize = 12.sp
    )

    @Composable
    fun Hint(text: String, modifier: Modifier = Modifier, color: Color = SubestacionColors.TextQuaternary) = Text(
        text, modifier = modifier, color = color, fontWeight = FontWeight.Medium, fontSize = 11.sp
    )

    @Composable
    fun HeroNumber(text: String, modifier: Modifier = Modifier) = Text(
        text, modifier = modifier, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp
    )
}

/** Badge de estado tipo píldora — EJECUTADA/VENCIDA/PENDIENTE/PROGRAMADA, etc. */
@Composable
fun EstadoPill(texto: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(bg, SubestacionShapes.Pill)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            texto.uppercase(), color = fg, fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp, letterSpacing = 0.6.sp
        )
    }
}

/**
 * Tarjeta de lista con barra de acento de color a la izquierda — el patrón que usa
 * el mock en Cronograma/Pendientes/Cola para cada cita/registro.
 */
@Composable
fun AccentListCard(
    accentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .clip(SubestacionShapes.CardLarge)
            .background(SubestacionColors.CardBackground)
            .border(1.5.dp, borderColor, SubestacionShapes.CardLarge)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Box(
            Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(accentColor)
        )
        Box(Modifier.padding(14.dp)) { content() }
    }
}

/** Colores de tono semántico (acento/píldora/borde) para un estado — mismo patrón que `tone`/`qTone` del mock. */
data class SubestacionTono(val accent: Color, val pillBg: Color, val pillFg: Color, val border: Color)

object SubestacionTonos {
    val Ejecutada = SubestacionTono(SubestacionColors.Green, SubestacionColors.GreenBg, SubestacionColors.Green, SubestacionColors.GreenBorder)
    val Vencida = SubestacionTono(SubestacionColors.Red, SubestacionColors.RedBg, SubestacionColors.Red, SubestacionColors.RedBorder)
    val Pendiente = SubestacionTono(SubestacionColors.Purple, SubestacionColors.PurpleSurface, SubestacionColors.Purple, SubestacionColors.PurpleBorder)
    val Programada = SubestacionTono(SubestacionColors.TextQuaternary, SubestacionColors.ChipBg, SubestacionColors.TextQuaternary, SubestacionColors.ChipBorder)
}

/**
 * Borde punteado real (el mock usa `border: 2px dashed ...` en el tile de cámara y
 * en la tarjeta "+ Registrar algo que no está en el cronograma" — Compose no tiene
 * un `BorderStroke` con estilo dashed nativo, se logra dibujando el trazo con
 * `PathEffect.dashPathEffect`).
 */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    val strokePx = strokeWidth.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(strokePx / 2, strokePx / 2),
        size = Size(size.width - strokePx, size.height - strokePx),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = strokePx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f)
        )
    )
}
