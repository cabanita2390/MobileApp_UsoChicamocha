package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toVehiculoItem
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLocalVehiculosUseCase @Inject constructor(
    private val repository: VehiculoInspectionRepository
) {
    operator fun invoke(): Flow<List<VehiculoItem>> =
        repository.getLocalVehiclesFlow().map { entities ->
            entities
                .filter { !it.tipoVehiculo.equals("MOTOCICLETA", ignoreCase = true) }
                .map { it.toVehiculoItem() }
        }
}
