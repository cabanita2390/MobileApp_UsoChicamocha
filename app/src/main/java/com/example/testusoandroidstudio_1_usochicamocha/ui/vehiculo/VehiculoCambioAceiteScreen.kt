package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.CambioAceiteScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.CambioAceiteScreenConfig

@Composable
fun VehiculoCambioAceiteScreen(
    viewModel: VehiculoCambioAceiteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CambioAceiteScreen(
        uiState = uiState,
        config = CambioAceiteScreenConfig(
            subtitle = "Vehículo",
            assetLabel = "Vehículo (*)",
            assetDialogTitle = "Seleccionar vehículo",
            assetSearchPlaceholder = "Buscar placa, marca...",
            assetEmptyMessage = "Sin vehículos. Sincronice el catálogo.",
            intervalSupportingText = null,
            assetDisplayText = { "${it.placa} — ${it.marca}" },
            assetListItemText = { "${it.placa} — ${it.marca} (${it.kilometrajeActual} km)" }
        ),
        onAssetSelected = viewModel::onVehicleSelected,
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
