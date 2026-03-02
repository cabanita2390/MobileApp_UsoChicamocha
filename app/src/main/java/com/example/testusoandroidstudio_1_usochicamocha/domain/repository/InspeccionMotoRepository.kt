package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente
import kotlinx.coroutines.flow.Flow

interface InspeccionMotoRepository {
    /** Guarda la inspección localmente en Room (siempre, con o sin internet) */
    suspend fun saveLocally(inspeccion: InspeccionMotoPendiente)

    /** Sincroniza una inspección pendiente con el backend */
    suspend fun syncOne(inspeccion: InspeccionMotoPendiente): Result<Unit>

    /** Retorna el Flow de inspecciones pendientes de sincronizar */
    fun getPending(): Flow<List<InspeccionMotoPendiente>>

    /** Versión suspend para el worker de sincronización */
    suspend fun getPendingList(): List<InspeccionMotoPendiente>

    /** Limpia locks colgados al inicio del worker */
    suspend fun resetStuckSyncing()
}
