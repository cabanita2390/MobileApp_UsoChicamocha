package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.fuel

import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FuelStationDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelStationEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import javax.inject.Inject

class SyncFuelStationsUseCase @Inject constructor(
    private val apiService: ApiService,
    private val dao: FuelStationDao
) {
    suspend operator fun invoke() {
        val response = apiService.getFuelStations()
        if (response.isSuccessful) {
            val stations = response.body()?.mapNotNull { map ->
                val id = (map["id"] as? Number)?.toLong() ?: return@mapNotNull null
                val name = map["name"] as? String ?: return@mapNotNull null
                FuelStationEntity(id = id, name = name)
            } ?: return
            dao.deleteAll()
            dao.upsertAll(stations)
        }
    }
}
