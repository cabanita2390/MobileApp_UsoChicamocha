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
 * para la parte de captura offline (Room + cola de sync). Cronograma/Pendientes/
 * indicadores/Detalle también son offline-first desde esta sesión (antes eran
 * online-first puro y dejaban esas 4 pantallas vacías/rotas en modo avión): la UI lee
 * de Room (`getCumplimientoLocal*Flow`, `getDetalleLocalFlow`) y la red solo alimenta
 * ese caché en segundo plano (`sincronizarCumplimiento*`, `sincronizarDetalle`) — ver
 * CumplimientoCacheEntity/EjecucionDetalleCacheEntity.
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

    // --- Consultas online (usadas hoy solo por el wizard de captura — CapturaViewModel;
    //     necesitan estar "al día" al momento de capturar, así que se dejan online-first) ---
    suspend fun getProgramacion(estacionId: Long, anio: Int, mes: Int): Result<List<ProgramacionCita>>
    suspend fun getCumplimientoPorMes(anio: Int, mes: Int): Result<List<CitaProgramada>>
    suspend fun getCumplimientoPorEstacion(estacionId: Long, anio: Int): Result<List<CitaProgramada>>
    suspend fun getPendientesDelAnio(anio: Int, mesActual: Int): Result<List<CitaProgramada>>
    suspend fun getDetalleEjecucion(id: Long): Result<EjecucionDetalle>

    /** null en el Result exitoso = todavía no hay ejecución para esa cita (404 esperado, no un error). */
    suspend fun getEjecucionPorProgramacion(programacionId: Long): Result<EjecucionDetalle?>

    suspend fun editarEjecucion(id: Long, edicion: EjecucionEdicion): Result<EjecucionDetalle>

    // --- Cumplimiento cacheado (offline-first: Cronograma/Pendientes/Home) ---

    /** Para Cronograma: un mes puntual, todas las estaciones, leído de Room. */
    fun getCumplimientoLocalPorMesFlow(anio: Int, mes: Int): Flow<List<CitaProgramada>>

    /** Para Pendientes/Home: todo el año hasta `mesActual`, leído de Room. */
    fun getCumplimientoLocalDelAnioFlow(anio: Int, mesActual: Int): Flow<List<CitaProgramada>>

    /** Refresca el caché de un mes puntual desde el backend (best-effort, no bloquea la UI). */
    suspend fun sincronizarCumplimientoMes(anio: Int, mes: Int): Result<Unit>

    /** Refresca el caché de los meses 1..mesActual del año (usado por Pendientes/Home y el sync periódico). */
    suspend fun sincronizarCumplimientoDelAnio(anio: Int, mesActual: Int): Result<Unit>

    // --- Detalle de ejecución cacheado bajo demanda (offline-first: pantalla Detalle) ---

    /** null = todavía no se ha consultado (con éxito) esta ejecución desde este dispositivo. */
    fun getDetalleLocalFlow(id: Long): Flow<EjecucionDetalle?>

    /** Trae el detalle del backend y lo cachea en Room (best-effort, no bloquea la UI). */
    suspend fun sincronizarDetalle(id: Long): Result<Unit>
}
