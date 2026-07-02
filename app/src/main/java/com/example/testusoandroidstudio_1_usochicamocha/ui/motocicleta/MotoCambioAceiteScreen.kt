package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.CambioAceiteScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.CambioAceiteScreenConfig

@Composable
fun MotoCambioAceiteScreen(
    viewModel: MotoCambioAceiteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CambioAceiteScreen(
        uiState = uiState,
        config = CambioAceiteScreenConfig(
            subtitle = "Motocicleta",
            assetLabel = "Motocicleta (*)",
            assetDialogTitle = "Seleccionar motocicleta",
            assetSearchPlaceholder = "Buscar placa...",
            assetEmptyMessage = "Sin motocicletas. Sincronice el catálogo.",
            intervalSupportingText = "Típico: 2000-3000 km para motos",
            assetDisplayText = {
                if (!it.marca.isNullOrBlank()) "${it.placa} — ${it.marca}" else it.placa
            },
            assetListItemText = {
                if (!it.marca.isNullOrBlank()) "${it.placa} — ${it.marca}" else it.placa
            }
        ),
        onAssetSelected = viewModel::onMotoSelected,
        onOilSelected = viewModel::onOilSelected,
        onKmAtChangeChange = viewModel::onKmAtChangeChange,
        onIntervalKmChange = viewModel::onIntervalKmChange,
        onQuantityChange = viewModel::onQuantityChange,
        onAirFilterChanged = viewModel::onAirFilterChanged,
        onSyncOils = viewModel::syncOils,
        onSubmit = viewModel::submit,
        onSubmissionSuccessHandled = viewModel::onSubmissionSuccessHandled,
        onClearError = viewModel::clearError,
        onNavigateBack = onNavigateBack
    )
}
