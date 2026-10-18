package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.OrdenAsignada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Órdenes de trabajo (OT-EB) del operario en sesión, desde la caché (offline). */
class ObtenerOrdenesAsignadasUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(): Flow<List<OrdenAsignada>> = repository.getOrdenesAsignadas()
}

/** Una orden de la caché, para abrir la captura con sus datos fijos. */
class ObtenerOrdenAsignadaUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(id: Long): OrdenAsignada? = repository.getOrdenAsignada(id)
}

/** Trae del servidor las órdenes asignadas y actualiza la caché; sin señal, queda la última lista. */
class SincronizarOrdenesAsignadasUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.sincronizarOrdenesAsignadas()
}
