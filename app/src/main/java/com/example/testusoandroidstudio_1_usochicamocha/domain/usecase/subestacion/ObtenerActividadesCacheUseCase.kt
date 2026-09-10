package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ActividadCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObtenerActividadesCacheUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(): Flow<List<ActividadCatalogo>> = repository.getActividadesCache()
}
