package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OilTypeOption(
    val code: String,                  // "MINERAL", "SEMI_SYNTHETIC", "SYNTHETIC"
    val displayName: String,           // "Aceite Mineral"
    val emoji: String,                 // "🛢️"
    val interval: String,              // "8,000 km"
    val description: String,           // "Cambio cada 8,000 km"
    val kmRange: Int = 0,              // 8000
    val hourRange: Int = 0             // para maquinaria
)

@Composable
fun OilTypeSelector(
    selectedType: String?,
    onTypeSelected: (OilTypeOption) -> Unit,
    assetType: String = "VEHICLE"  // "VEHICLE", "MOTORCYCLE", "MACHINERY"
) {
    val oilTypes = when (assetType) {
        "MOTORCYCLE" -> listOf(
            OilTypeOption(
                code = "MINERAL",
                displayName = "Aceite Mineral",
                emoji = "🛢️",
                interval = "5,000-8,000 km",
                description = "Cambio cada 5,000-8,000 km",
                kmRange = 8000
            ),
            OilTypeOption(
                code = "SEMI_SYNTHETIC",
                displayName = "Semi-Sintético",
                emoji = "🔄",
                interval = "8,000-10,000 km",
                description = "Cambio cada 8,000-10,000 km",
                kmRange = 10000
            ),
            OilTypeOption(
                code = "SYNTHETIC",
                displayName = "Sintético Premium",
                emoji = "⚡",
                interval = "10,000-12,000 km",
                description = "Cambio cada 10,000-12,000 km",
                kmRange = 12000
            )
        )
        "MACHINERY" -> listOf(
            OilTypeOption(
                code = "MINERAL",
                displayName = "Aceite Mineral",
                emoji = "🛢️",
                interval = "500-800 horas",
                description = "Cambio cada 500-800 horas",
                hourRange = 800
            ),
            OilTypeOption(
                code = "SEMI_SYNTHETIC",
                displayName = "Semi-Sintético",
                emoji = "🔄",
                interval = "1,000-1,200 horas",
                description = "Cambio cada 1,000-1,200 horas",
                hourRange = 1200
            ),
            OilTypeOption(
                code = "SYNTHETIC",
                displayName = "Sintético Premium",
                emoji = "⚡",
                interval = "1,500-2,000 horas",
                description = "Cambio cada 1,500-2,000 horas",
                hourRange = 2000
            )
        )
        else -> listOf(  // "VEHICLE" - por defecto
            OilTypeOption(
                code = "MINERAL",
                displayName = "Aceite Mineral",
                emoji = "🛢️",
                interval = "5,000-8,000 km",
                description = "Cambio cada 5,000-8,000 km",
                kmRange = 8000
            ),
            OilTypeOption(
                code = "SEMI_SYNTHETIC",
                displayName = "Semi-Sintético",
                emoji = "🔄",
                interval = "10,000-12,000 km",
                description = "Cambio cada 10,000-12,000 km",
                kmRange = 12000
            ),
            OilTypeOption(
                code = "SYNTHETIC",
                displayName = "Sintético Premium",
                emoji = "⚡",
                interval = "15,000 km",
                description = "Cambio cada 15,000 km",
                kmRange = 15000
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Tipo de Aceite (*)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            oilTypes.forEach { oilType ->
                OilTypeCard(
                    oilType = oilType,
                    isSelected = selectedType == oilType.code,
                    onClick = { onTypeSelected(oilType) }
                )
            }
        }
    }
}

@Composable
fun OilTypeCard(
    oilType: OilTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF5F3FF) else Color.White
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        oilType.emoji,
                        fontSize = 20.sp
                    )
                    Column {
                        Text(
                            oilType.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            oilType.interval,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (isSelected) {
                Surface(
                    modifier = Modifier
                        .size(24.dp),
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFF7C3AED)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Seleccionado",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}
