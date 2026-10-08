package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observaciones más usadas por tipo de actividad, guardadas en el último sync (sirven sin conexión). */
class ObtenerObservacionesFrecuentesUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(): Flow<Map<String, List<String>>> = repository.getObservacionesFrecuentes()
}
