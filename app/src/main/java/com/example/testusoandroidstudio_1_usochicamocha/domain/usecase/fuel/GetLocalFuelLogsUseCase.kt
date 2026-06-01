package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.fuel

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelLogEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FuelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalFuelLogsUseCase @Inject constructor(
    private val repository: FuelRepository
) {
    operator fun invoke(): Flow<List<FuelLogEntity>> = repository.getAllFlow()

    fun byAsset(assetType: String, assetId: Long): Flow<List<FuelLogEntity>> =
        repository.getByAssetFlow(assetType, assetId)
}
