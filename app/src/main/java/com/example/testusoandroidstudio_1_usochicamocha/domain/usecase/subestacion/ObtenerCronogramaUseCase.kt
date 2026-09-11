package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Cronograma de un mes, todas las estaciones. Offline-first (única consumidora es
 * CronogramaViewModel): [local] lee del caché de cumplimiento en Room, [refrescar]
 * pide el mes al backend en segundo plano y actualiza ese caché — la UI se entera
 * sola vía el Flow, sin bloquearse si no hay señal.
 */
class ObtenerCronogramaUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    fun local(anio: Int, mes: Int): Flow<List<CitaProgramada>> {
        return repository.getCumplimientoLocalPorMesFlow(anio, mes)
    }

    suspend fun refrescar(anio: Int, mes: Int): Result<Unit> {
        return repository.sincronizarCumplimientoMes(anio, mes)
    }
}
