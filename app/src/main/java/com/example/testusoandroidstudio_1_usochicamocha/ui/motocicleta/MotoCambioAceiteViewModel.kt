package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MotoRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MotoOilChangeRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.AssetOilChangeItem
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.CambioAceiteCore
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.oilchange.CambioAceiteStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MotoCambioAceiteViewModel @Inject constructor(
    motoRepository: MotoRepository,
    private val oilChangeRepository: MotoOilChangeRepository,
    getLocalOilsUseCase: GetLocalOilsUseCase,
    private val syncMotosUseCase: SyncMotosUseCase,
    syncOilsUseCase: SyncOilsUseCase,
    tokenManager: TokenManager
) : ViewModel() {

    private val strategy = object : CambioAceiteStrategy {
        override fun assetsFlow(): Flow<List<AssetOilChangeItem>> =
            motoRepository.getLocalMotos().map { list ->
                list.map { AssetOilChangeItem(it.id, it.placa, it.marca) }
            }

        override suspend fun onInit() {
            try {
                syncMotosUseCase()
            } catch (e: Exception) {
                Log.e("MotoCambioAcVM", "Error syncing motos: ${e.message}")
            }
        }

        override fun kmPrefillOnSelect(asset: AssetOilChangeItem): String = ""

        override suspend fun save(
            asset: AssetOilChangeItem, oilType: String, oil: Oil, quantity: Double?,
            km: Int, interval: Int, airFilterChanged: Boolean
        ) {
            val entity = MotoOilChangeEntity(
                placa = asset.placa,
                timestamp = System.currentTimeMillis(),
                oilType = oilType,
                oilBrandId = oil.id.toLong(),
                oilBrandName = oil.name,
                quantity = quantity,
                kmAtChange = km,
                intervalKm = interval,
                airFilterChanged = airFilterChanged
            )
            oilChangeRepository.saveLocally(entity)
            oilChangeRepository.syncPending()
        }
    }

    private val core = CambioAceiteCore(
        strategy, getLocalOilsUseCase, syncOilsUseCase, tokenManager,
        logTag = "MotoCambioAcVM"
    )

    val uiState = core.uiState

    init {
        core.start(viewModelScope)
    }

    fun onMotoSelected(asset: AssetOilChangeItem) = core.onAssetSelected(asset)
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
