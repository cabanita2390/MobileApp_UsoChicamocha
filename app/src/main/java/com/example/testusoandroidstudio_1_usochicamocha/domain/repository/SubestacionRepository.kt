package com.example.testusoandroidstudio_1_usochicamocha.domain.repository

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ActividadCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionEdicion
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EstacionCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ProgramacionCita
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del módulo Subestaciones (mantenimiento civil). Análogo a FormRepository
 * para la parte de captura offline (Room + cola de sync); la parte de cronograma/
 * indicadores/detalle es online-first (ver Simplificaciones en el plan de
 * implementación) y no pasa por Room.
 */
interface SubestacionRepository {

    // --- Captura offline (ejecuciones pendientes) ---
    fun getPendingEjecuciones(): Flow<List<Ejecucion>>

    /** Para la pantalla Cola — a diferencia de [getPendingEjecuciones], incluye los que están `isSyncing=true`. */
    fun getEjecucionesCola(): Flow<List<Ejecucion>>

    suspend fun saveEjecucionLocally(ejecucion: Ejecucion, imageUris: List<String>)
    suspend fun syncEjecucion(ejecucion: Ejecucion): Result<Unit>

    // --- Evidencia (comparte ImageDao/ImageEntity con Form/Vehículo) ---
    suspend fun syncEvidencia(ejecucionServerId: Long, imageUri: String): Result<Unit>

    // --- Catálogos cacheados (para que el wizard funcione sin señal) ---
    fun getEstacionesCache(): Flow<List<EstacionCatalogo>>
    fun getActividadesCache(): Flow<List<ActividadCatalogo>>
    suspend fun sincronizarCatalogos(): Result<Unit>

    // --- Consultas online (cronograma, indicadores, detalle) ---
    suspend fun getProgramacion(estacionId: Long, anio: Int, mes: Int): Result<List<ProgramacionCita>>
    suspend fun getCumplimientoPorMes(anio: Int, mes: Int): Result<List<CitaProgramada>>
    suspend fun getCumplimientoPorEstacion(estacionId: Long, anio: Int): Result<List<CitaProgramada>>
    suspend fun getPendientesDelAnio(anio: Int, mesActual: Int): Result<List<CitaProgramada>>
    suspend fun getDetalleEjecucion(id: Long): Result<EjecucionDetalle>

    /** null en el Result exitoso = todavía no hay ejecución para esa cita (404 esperado, no un error). */
    suspend fun getEjecucionPorProgramacion(programacionId: Long): Result<EjecucionDetalle?>

    suspend fun editarEjecucion(id: Long, edicion: EjecucionEdicion): Result<EjecucionDetalle>
}
