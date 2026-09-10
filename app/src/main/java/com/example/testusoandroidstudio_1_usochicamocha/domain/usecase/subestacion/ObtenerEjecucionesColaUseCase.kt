package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Para la pantalla Cola — a diferencia de [ObtenerEjecucionesPendientesUseCase], incluye los que están `isSyncing=true` ("Enviando…"). */
class ObtenerEjecucionesColaUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(): Flow<List<Ejecucion>> = repository.getEjecucionesCola()
}
