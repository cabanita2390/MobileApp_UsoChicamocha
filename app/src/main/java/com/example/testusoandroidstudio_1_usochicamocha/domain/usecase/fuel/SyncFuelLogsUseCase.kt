package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.fuel

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FuelRepository
import javax.inject.Inject

class SyncFuelLogsUseCase @Inject constructor(
    private val repository: FuelRepository
) {
    suspend operator fun invoke() = repository.syncPending()
}
