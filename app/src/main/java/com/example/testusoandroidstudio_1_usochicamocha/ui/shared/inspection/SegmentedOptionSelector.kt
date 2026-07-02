package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.inspection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Fila de botones segmentados para elegir una opción de una lista corta y
 * coloreada — antes vivía triplicado como `VehicleStatusSelector` (Bueno/Regular/
 * Malo fijo), `YesNoSelector` (Si/No) en VehiculoScreen.kt, y `MotoStatusSelector`
 * (Óptimo/Regular/Malo configurable + testTag) en MotocicletaScreen.kt. */
@Composable
fun SegmentedOptionSelector(
    label: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    options: List<Pair<String, Color>>,
    optionFontSize: TextUnit = 14.sp,
    tagPrefix: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (option, bgColor) ->
                val isSelected = option == selectedOption
                Surface(
                    modifier = Modifier.weight(1f).height(48.dp)
                        .let { if (tagPrefix != null) it.testTag("${tagPrefix}_$option") else it }
                        .border(2.dp, if (isSelected) bgColor else Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onOptionSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) bgColor else bgColor.copy(alpha = 0.15f)
                ) {
                    Box(Modifier.background(Color.Transparent), contentAlignment = Alignment.Center) {
                        Text(
                            option,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.Black,
                            fontSize = optionFontSize
                        )
                    }
                }
            }
        }
    }
}
