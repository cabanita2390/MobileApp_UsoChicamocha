package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/** Composables compartidos entre las pantallas de inspección de Vehículo y Moto.
 * Antes vivían duplicados en VehiculoScreen.kt (SectionCard, DocumentImage) o
 * definidos ahí y re-importados desde MotocicletaScreen.kt (DocLabelRow,
 * EstadoDocumentoChip) — un acoplamiento cruzado innecesario entre los dos módulos. */

private val ColorBueno   = Color(0xFF4CAF50)
private val ColorRegular = Color(0xFFFFA000)
private val ColorMalo    = Color(0xFFD32F2F)

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Muestra el nombre del documento con un ícono representativo.
 * No permite subir fotos — solo identifica visualmente el tipo de documento.
 */
@Composable
fun DocLabelRow(label: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DocumentImage(url: String?, label: String) {
    val context = LocalContext.current
    val isPdf = !url.isNullOrBlank() && url.trimEnd().lowercase().endsWith(".pdf")
    val isImage = !url.isNullOrBlank() && !isPdf
    var showFullscreen by remember { mutableStateOf(false) }

    if (showFullscreen && isImage) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { showFullscreen = false },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                )
                IconButton(
                    onClick = { showFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isPdf) 100.dp else 180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .then(if (isImage) Modifier.clickable { showFullscreen = true } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            when {
                url.isNullOrBlank() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.HideImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Imagen no disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                isPdf -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = ColorMalo,
                        modifier = Modifier.size(36.dp)
                    )
                    Text("Documento PDF", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("Abrir PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
                else -> {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        error = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ErrorOutline, null, tint = ColorMalo)
                                Text("Error al cargar imagen", style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        loading = {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 2.dp)
                        }
                    )
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = "Ampliar imagen",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoDocumentoChip(estado: String, diasRestantes: Long = 0L) {
    if (estado.isBlank()) return
    val (bgColor, textColor, icon) = when (estado) {
        "Vigente"          -> Triple(ColorBueno.copy(alpha = 0.12f),   ColorBueno,   Icons.Default.CheckCircle)
        "Próximo a Vencer" -> Triple(ColorRegular.copy(alpha = 0.12f), ColorRegular, Icons.Default.Warning)
        "Vencido"          -> Triple(ColorMalo.copy(alpha = 0.12f),    ColorMalo,    Icons.Default.Cancel)
        else               -> Triple(Color.Gray.copy(alpha = 0.12f),   Color.Gray,   Icons.Default.Info)
    }
    // Leyenda de días según el estado
    val leyenda = when (estado) {
        "Vigente"          -> "— quedan $diasRestantes días"
        "Próximo a Vencer" -> "— quedan $diasRestantes días"
        "Vencido"          -> "— venció hace ${-diasRestantes} días"
        else               -> ""
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, textColor, RoundedCornerShape(8.dp))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = textColor)
            Spacer(Modifier.width(8.dp))
            Text(
                "$estado $leyenda",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
