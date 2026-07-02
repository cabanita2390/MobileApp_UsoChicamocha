package com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import kotlinx.coroutines.flow.Flow

/** Ítem genérico de activo (vehículo o moto) para el selector de cambio de aceite.
 * kilometrajeActual es null cuando el origen de datos no lo trae en el catálogo local (motos). */
data class AssetOilChangeItem(
    val id: Int,
    val placa: String,
    val marca: String? = null,
    val kilometrajeActual: Int? = null
)

data class CambioAceiteUiState(
    val assets: List<AssetOilChangeItem> = emptyList(),
    val selectedAsset: AssetOilChangeItem? = null,
    val oilBrands: List<Oil> = emptyList(),
    val selectedOil: Oil? = null,
    val oilType: String = "motor",
    val kmAtChange: String = "",
    val intervalKm: String = "",
    val quantity: String = "",
    val airFilterChanged: Boolean = false,
    val isLoading: Boolean = false,
    val isSyncingOils: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null,
    val isRoleAllowed: Boolean = true
)

/** Lo que difiere de verdad entre "cambio de aceite de vehículo" y "de moto":
 * de dónde salen los activos, qué precargar al seleccionar uno, y cómo se guarda. */
interface CambioAceiteStrategy {
    fun assetsFlow(): Flow<List<AssetOilChangeItem>>
    suspend fun onInit() {}
    fun kmPrefillOnSelect(asset: AssetOilChangeItem): String
    suspend fun save(
        asset: AssetOilChangeItem,
        oilType: String,
        oil: Oil,
        quantity: Double?,
        km: Int,
        interval: Int,
        airFilterChanged: Boolean
    )
}
