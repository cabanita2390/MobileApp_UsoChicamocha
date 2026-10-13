package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import javax.inject.Inject

/** Acciones de la Cola sobre fotos cuyo archivo ya no existe en el equipo. */
class FotosPerdidasUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend fun reemplazar(ejecucionUuid: String, nuevaUri: String): Boolean =
        repository.reemplazarFotoPerdida(ejecucionUuid, nuevaUri)

    suspend fun descartar(ejecucionUuid: String) = repository.descartarFotosPerdidas(ejecucionUuid)
}
