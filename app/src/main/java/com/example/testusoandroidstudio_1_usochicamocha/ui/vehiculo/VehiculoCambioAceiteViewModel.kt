package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoOilChangeRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.AssetOilChangeItem
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.CambioAceiteCore
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.CambioAceiteStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class VehiculoCambioAceiteViewModel @Inject constructor(
    vehiculoInspectionRepository: VehiculoInspectionRepository,
    private val oilChangeRepository: VehiculoOilChangeRepository,
    getLocalOilsUseCase: GetLocalOilsUseCase,
    syncOilsUseCase: SyncOilsUseCase,
    tokenManager: TokenManager
) : ViewModel() {

    private val strategy = object : CambioAceiteStrategy {
        override fun assetsFlow(): Flow<List<AssetOilChangeItem>> =
            vehiculoInspectionRepository.getLocalVehiclesFlow().map { list ->
                // Excluir MOTOCICLETA (que tiene su propia sección)
                list.filter { !it.tipoVehiculo.equals("MOTOCICLETA", ignoreCase = true) }
                    .map { AssetOilChangeItem(it.idVehiculo, it.placa, it.marca, it.kilometrajeActual) }
            }

        override fun kmPrefillOnSelect(asset: AssetOilChangeItem): String =
            asset.kilometrajeActual?.toString() ?: ""

        override suspend fun save(
            asset: AssetOilChangeItem, oilType: String, oil: Oil, quantity: Double?,
            km: Int, interval: Int, airFilterChanged: Boolean
        ) {
            val entity = VehiculoOilChangeEntity(
                placa = asset.placa,
                timestamp = System.currentTimeMillis(),
                oilType = oilType,
                oilBrandId = oil.id.toLong(),
                oilBrandName = oil.name,
                quantity = quantity,
                kmAtChange = km,
                intervalKm = interval,
                airFilterChanged = airFilterChanged,
                assetType = "VEHICLE"
            )
            oilChangeRepository.saveLocally(entity)
            oilChangeRepository.syncPending()
        }
    }

    private val core = CambioAceiteCore(
        strategy, getLocalOilsUseCase, syncOilsUseCase, tokenManager,
        logTag = "VehiculoCambioAceiteVM"
    )

    val uiState = core.uiState

    init {
        core.start(viewModelScope)
    }

    fun onVehicleSelected(asset: AssetOilChangeItem) = core.onAssetSelected(asset)
    fun onOilTypeChange(type: String) = core.onOilTypeChange(type)
    fun onOilSelected(oil: Oil) = core.onOilSelected(oil)
    fun onKmAtChangeChange(km: String) = core.onKmAtChangeChange(km)
    fun onIntervalKmChange(km: String) = core.onIntervalKmChange(km)
    fun onQuantityChange(q: String) = core.onQuantityChange(q)
    fun onAirFilterChanged(value: Boolean) = core.onAirFilterChanged(value)
    fun clearError() = core.clearError()
    fun onSubmissionSuccessHandled() = core.onSubmissionSuccessHandled()
    fun syncOils() = core.syncOils()
    fun submit() = core.submit()
}
