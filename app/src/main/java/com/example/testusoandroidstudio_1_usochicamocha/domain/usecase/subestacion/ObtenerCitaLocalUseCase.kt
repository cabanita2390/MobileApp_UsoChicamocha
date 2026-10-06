package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

/** Una cita del cronograma tal como está en el caché local (offline). */
class ObtenerCitaLocalUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(programacionId: Long): CitaProgramada? = repository.getCitaLocal(programacionId)
}
