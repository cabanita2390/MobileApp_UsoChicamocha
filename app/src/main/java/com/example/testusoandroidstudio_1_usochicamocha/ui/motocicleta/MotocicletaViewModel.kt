package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.annotation.SuppressLint
import android.util.Log
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.DocumentoMotoDao
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalUbicacionesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SaveInspeccionMotoLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SyncInspeccionMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
// import com.example.testusoandroidstudio_1_usochicamocha.util.Constants // Removido por solicitud del usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

/** Estado de un documento individual (SOAT, Tecno, Licencia) */
data class DocumentoState(
    val estadoDoc: String = "",         // Vigente / Próximo a Vencer / Vencido
    val diasRestantes: Long = 0L,       // Días de diferencia (negativo = vencido)
    val imagenUrl: String? = null,      // URL de la imagen guardada en servidor
    val imagenUri: Uri? = null,         // URI local de imagen nueva (cámara/galería)
    val yaRegistrado: Boolean = false,  // true si ya existe en el sistema
    val vigenciaMaster: String = ""    // Fecha de referencia inalterable (Base de Datos Maestra)
)

data class MotocicletaUiState(
    val motocicletas: List<Moto> = emptyList(),
    val ubicaciones: List<Ubicacion> = emptyList(),
    val isLoadingData: Boolean = false,

    val selectedMoto: Moto? = null,
    val selectedUbicacion: Ubicacion? = null,
    val kilometraje: String = "",
    val kilometrajeMinimo: Int = 0,
    val kilometrajeError: String? = null,
    val kmEsInvalido: Boolean = false,
    val kmAlertMessage: String = "",
    val showKmAlert: Boolean = false,       // Rojo
    val showKmYellowAlert: Boolean = false, // Amarillo
    val kmYellowConfirmed: Boolean = false,
    val kmRedConfirmed: Boolean = false,    // Nuevo

    // Documentos agrupados en DocumentoState
    val soat: DocumentoState = DocumentoState(),
    val revisionTecno: DocumentoState = DocumentoState(),
    val licencia: DocumentoState = DocumentoState(),

    val checkExtintor: String = "No Aplica",
    val isLoadingDocumentos: Boolean = false,
    val estadoVehiculo: String = "",
    val conscienteResponsabilidad: String = "Si",
    val aprobadoRuta: String = "Si",
    val observaciones: String = "",
    val responsable: String = "",

    // Inspección Mecánica Moto (Bueno / Regular / Malo)
    val checkNivelAceite: String = "",
    val checkEstadoLlantas: String = "",
    val checkEstadoLuces: String = "",

    val isSaveButtonEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val errorMessage: String? = null,

    // Hub features
    val pendingInspecciones: List<InspeccionMotoPendiente> = emptyList(),
    val isSyncingMotos: Boolean = false,
    val isSyncingUbicaciones: Boolean = false,
    val isSyncingDocumentos: Boolean = false,
    val isSyncingPending: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class MotocicletaViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val getLocalMotosUseCase: GetLocalMotosUseCase,
    private val getLocalUbicacionesUseCase: GetLocalUbicacionesUseCase,
    private val saveInspeccionMotoLocalUseCase: SaveInspeccionMotoLocalUseCase,
    private val syncInspeccionMotoUseCase: SyncInspeccionMotoUseCase,
    private val getPendingInspeccionesMotoUseCase: com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.GetPendingInspeccionesMotoUseCase,
    private val documentoMotoDao: DocumentoMotoDao,
    private val localSyncCoordinator: LocalSyncCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotocicletaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMotos()
        loadUbicaciones()
        viewModelScope.launch {
            tokenManager.getInspectorInfo().first()?.let { info ->
                _uiState.update { it.copy(responsable = info) }
            }
        }
        observePending()
        observeSyncStatuses()
    }

    private fun observePending() {
        getPendingInspeccionesMotoUseCase().onEach { list ->
            _uiState.update { it.copy(pendingInspecciones = list) }
        }.launchIn(viewModelScope)
    }

    private fun observeSyncStatuses() {
        // Observamos estados del coordinador
        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MOTOS_ONLY)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingMotos = isRunning) }
        }.launchIn(viewModelScope)

        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.UBICACIONES_ONLY)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingUbicaciones = isRunning) }
        }.launchIn(viewModelScope)

        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.DOCUMENTS_ONLY)
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingDocumentos = isRunning) }
        }.launchIn(viewModelScope)

        localSyncCoordinator.observeSyncTrigger(
            LocalSyncCoordinator.SyncTrigger.FormSaved("Motocicleta")
        ).onEach { isRunning ->
            _uiState.update { it.copy(isSyncingPending = isRunning) }
        }.launchIn(viewModelScope)
    }

    fun onSyncAllClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncMessage = "Sincronizando datos...") }
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MOTOS_ONLY))
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.UBICACIONES_ONLY))
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.DOCUMENTS_ONLY))
        }
    }

    fun onSyncPlacasYUnidadesClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncMessage = "Sincronizando placas y unidades...") }
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MOTOS_ONLY))
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.UBICACIONES_ONLY))
        }
    }

    // Mantenidos por compatibilidad interna
    fun onSyncMotosClicked() {
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MOTOS_ONLY))
            _uiState.update { it.copy(syncMessage = "Sincronizando placas...") }
        }
    }

    fun onSyncUbicacionesClicked() {
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.UBICACIONES_ONLY))
            _uiState.update { it.copy(syncMessage = "Sincronizando unidades...") }
        }
    }

    fun onSyncDocumentosClicked() {
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.DOCUMENTS_ONLY))
            _uiState.update { it.copy(syncMessage = "Sincronizando documentos...") }
        }
    }

    fun onSyncPendingClicked() {
        viewModelScope.launch {
            if (_uiState.value.pendingInspecciones.isEmpty()) {
                _uiState.update { it.copy(syncMessage = "No hay inspecciones pendientes") }
                return@launch
            }
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.FormSaved("Motocicleta"))
            _uiState.update { it.copy(syncMessage = "Sincronizando pendientes...") }
        }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    private fun loadMotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingData = true) }
            getLocalMotosUseCase().collect { motos ->
                _uiState.update { it.copy(motocicletas = motos, isLoadingData = false) }
                if (motos.isEmpty()) {
                    android.util.Log.d("MotocicletaVM", "Empty motos list, triggering MASTER_DATA sync")
                    localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MASTER_DATA))
                }
            }
        }
    }

    private fun loadUbicaciones() {
        viewModelScope.launch {
            getLocalUbicacionesUseCase().collect { ubicaciones ->
                _uiState.update { it.copy(ubicaciones = ubicaciones) }
                if (ubicaciones.isEmpty()) {
                    android.util.Log.d("MotocicletaVM", "Empty locations list, triggering sync")
                    localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.MASTER_DATA))
                }
            }
        }
    }

    fun onMotoSelected(moto: Moto) {
        _uiState.update { it.copy(selectedMoto = moto) }
        loadDocumentosForMoto(moto.placa)
        validate()
    }

    fun onUbicacionSelected(ubicacion: Ubicacion) {
        _uiState.update { it.copy(selectedUbicacion = ubicacion) }
        validate()
    }

    fun onKilometrajeChange(value: String) {
        // Limpiar TODOS los estados de alerta anteriores al escribir nuevo valor
        _uiState.update { it.copy(
            kilometraje = value,
            kmYellowConfirmed = false,
            kmRedConfirmed = false,     // Reset
            showKmAlert = false,        // Limpiar alerta roja previa
            showKmYellowAlert = false   // Limpiar alerta amarilla previa
        ) }

        val km = value.toIntOrNull() ?: 0
        val kmMin = _uiState.value.kilometrajeMinimo
        
        // Validación Roja (Bloqueante) silenciosa para el botón
        val isLowerThanMin = km > 0 && km < kmMin
        _uiState.update { it.copy(
            kmEsInvalido = isLowerThanMin,
            kilometrajeError = null // Sin mensajes de error en campo
        ) }
        validate()
    }

    /**
     * Se activa cuando el campo de kilometraje pierde el foco (onBlur).
     */
    fun onKilometrajeBlur() {
        val km = _uiState.value.kilometraje.toIntOrNull() ?: return
        val kmMin = _uiState.value.kilometrajeMinimo
        val diff = km - kmMin
        if (km <= 0) return

        when {
            diff < 0 -> _uiState.update { it.copy(
                showKmAlert = true,
                kmAlertMessage = "El kilometraje ingresado es menor al último kilometraje registrado. Por favor, verifíquelo."
            )}
            diff == 0 || diff >= 300 -> { // KM_THRESHOLD hardcoded
                val msg = if (diff == 0)
                    "El kilometraje ingresado es igual al último registrado. ¿Confirma que es correcto?"
                else
                    "Detectamos un incremento inusual en el kilometraje. ¿Está seguro de que es correcto?"
                _uiState.update { it.copy(
                    showKmYellowAlert = true,
                    kmYellowConfirmed = false,
                    kmAlertMessage = msg
                )}
            }
        }
    }

    fun onKmAlertDismiss() {
        _uiState.update { it.copy(
            showKmAlert = false,
            kmEsInvalido = false,
            kmRedConfirmed = false
        ) }
    }

    fun onConfirmRedKmException() {
        _uiState.update { it.copy(showKmAlert = false, kmRedConfirmed = true) }
        validate()
    }

    fun onCancelRedKmHighlight() {
        _uiState.update { it.copy(showKmAlert = false, kmRedConfirmed = false) }
    }

    fun onConfirmKmException() {
        _uiState.update { it.copy(showKmYellowAlert = false, kmYellowConfirmed = true) }
        validate()
    }

    fun onCancelKmHighlight() {
        _uiState.update { it.copy(showKmYellowAlert = false, kmYellowConfirmed = false) }
    }

    fun onKmYellowAlertDismiss() {
        _uiState.update { it.copy(showKmYellowAlert = false) }
    }

    fun onEstadoVehiculoChange(value: String) {
        _uiState.update { it.copy(estadoVehiculo = value) }
        validate()
    }

    fun onObservacionesChange(value: String) {
        _uiState.update { it.copy(observaciones = value) }
        validate()
    }

    fun onCheckNivelAceiteChange(value: String) {
        _uiState.update { it.copy(checkNivelAceite = value) }
        validate()
    }

    fun onCheckEstadoLlantasChange(value: String) {
        _uiState.update { it.copy(checkEstadoLlantas = value) }
        validate()
    }

    fun onCheckEstadoLucesChange(value: String) {
        _uiState.update { it.copy(checkEstadoLuces = value) }
        validate()
    }

    // ─── CARGA DE DOCUMENTOS ────────────────────────────────────────────────
    private fun loadDocumentosForMoto(placa: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoadingDocumentos = true,
                soat          = DocumentoState(),
                revisionTecno = DocumentoState(),
                licencia      = DocumentoState()
            ) }
            
            // 1. CARGA INMEDIATA DESDE CACHE LOCAL
            try {
                val cached = documentoMotoDao.getByPlaca(placa)
                if (cached.isNotEmpty()) {
                    android.util.Log.d("DocsMoto", "💾 [Cache] Cargando ${cached.size} registros inmediatamente")
                    aplicarDocumentosDesdeCache(cached)
                }
            } catch (e: Exception) {
                android.util.Log.e("DocsMoto", "⚠️ Error cargando cache inicial: ${e.message}")
            }

            // 2. CARGA DESDE EL SERVIDOR (Sincronización)
            try {
                android.util.Log.d("DocsMoto", "📡 [API] Solicitando datos para placa: $placa")
                val resp = apiService.getDocumentosByPlaca(placa)
                val apiDocs = if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
                
                if (resp.isSuccessful) {
                    android.util.Log.d("DocsMoto", "✅ [API] Recibidos ${apiDocs.size} documentos")

                    // Actualizar el cache local con lo del servidor (Limpiar primero para evitar duplicados)
                    val entities = apiDocs.mapNotNull { apiDoc ->
                        val fechaApi = apiDoc.fechaVencimiento ?: apiDoc.mesyear ?: ""
                        if (fechaApi.isNotBlank()) {
                            com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity(
                                tipoDocumento = apiDoc.tipoDocumento,
                                placa = placa,
                                vigencia = fechaApi,
                                kilometrajeActual = apiDoc.vehiculoKilometrajeActual ?: 0,
                                imagenUrl = apiDoc.imagenUrl
                            )
                        } else null
                    }
                    if (entities.isNotEmpty()) {
                        documentoMotoDao.refreshForPlaca(placa, entities)
                    }

                    val kmMinimo = apiDocs.mapNotNull { it.vehiculoKilometrajeActual }.firstOrNull() ?: 0

                    _uiState.update { s ->
                        val soatApi  = apiDocs.find { it.tipoDocumento == "SOAT" }
                        val tecnoApi = apiDocs.find { it.tipoDocumento == "REVISION_TECNO" }
                        val licApi   = apiDocs.find { it.tipoDocumento == "LICENCIA" }

                        val soatFecha  = soatApi?.fechaVencimiento?.takeIf { it.isNotBlank() } ?: soatApi?.mesyear ?: ""
                        val tecnoFecha = tecnoApi?.fechaVencimiento?.takeIf { it.isNotBlank() } ?: tecnoApi?.mesyear ?: ""
                        val licFecha   = licApi?.fechaVencimiento?.takeIf { it.isNotBlank() } ?: licApi?.mesyear ?: ""

                        val (soatEst, soatDias)   = calcularEstadoPorDias(soatFecha)
                        val (tecnoEst, tecnoDias) = calcularEstadoPorDias(tecnoFecha)
                        val (licEst, licDias)     = calcularEstadoPorDias(licFecha)

                        android.util.Log.d("DocsMoto", "🔄 [API] SOAT: $soatEst($soatDias d) TECNO: $tecnoEst($tecnoDias d) LIC: $licEst($licDias d)")

                        s.copy(
                            soat          = s.soat.copy(vigenciaMaster = soatFecha, estadoDoc = soatEst, diasRestantes = soatDias, yaRegistrado = true, imagenUrl = soatApi?.imagenUrl),
                            revisionTecno = s.revisionTecno.copy(vigenciaMaster = tecnoFecha, estadoDoc = tecnoEst, diasRestantes = tecnoDias, yaRegistrado = true, imagenUrl = tecnoApi?.imagenUrl),
                            licencia      = s.licencia.copy(vigenciaMaster = licFecha, estadoDoc = licEst, diasRestantes = licDias, yaRegistrado = true, imagenUrl = licApi?.imagenUrl),
                            kilometrajeMinimo = kmMinimo,
                            isLoadingDocumentos = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingDocumentos = false) }
                }

            } catch (e: Exception) {
                android.util.Log.e("DocsMoto", "💥 Error API: ${e.message}", e)
                _uiState.update { it.copy(isLoadingDocumentos = false) }
            }
            validate()
        }
    }

    fun clearLocalCache() {
        val placa = _uiState.value.selectedMoto?.placa ?: return
        viewModelScope.launch {
            try {
                documentoMotoDao.deleteByPlaca(placa)
                _uiState.update { it.copy(
                    soat = DocumentoState(),
                    revisionTecno = DocumentoState(),
                    licencia = DocumentoState(),
                    errorMessage = "Caché de $placa limpiado. Recargando..."
                )}
                android.util.Log.d("DocsMoto", "🗑️ Caché limpiado para placa $placa")
                loadDocumentosForMoto(placa)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error limpiando caché: ${e.message}") }
            }
        }
    }

    private fun aplicarDocumentosDesdeCache(cached: List<com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity>) {
        val soat  = cached.find { it.tipoDocumento == "SOAT" }
        val tecno = cached.find { it.tipoDocumento == "REVISION_TECNO" }
        val lic   = cached.find { it.tipoDocumento == "LICENCIA" }

        val (soatEst, soatDias)   = calcularEstadoPorDias(soat?.vigencia ?: "")
        val (tecnoEst, tecnoDias) = calcularEstadoPorDias(tecno?.vigencia ?: "")
        val (licEst, licDias)     = calcularEstadoPorDias(lic?.vigencia ?: "")

        _uiState.update { s ->
            s.copy(
                soat          = if (soat != null) s.soat.copy(vigenciaMaster = soat.vigencia, estadoDoc = soatEst, diasRestantes = soatDias, yaRegistrado = true, imagenUrl = soat.imagenUrl) else s.soat,
                revisionTecno = if (tecno != null) s.revisionTecno.copy(vigenciaMaster = tecno.vigencia, estadoDoc = tecnoEst, diasRestantes = tecnoDias, yaRegistrado = true, imagenUrl = tecno.imagenUrl) else s.revisionTecno,
                licencia      = if (lic != null) s.licencia.copy(vigenciaMaster = lic.vigencia, estadoDoc = licEst, diasRestantes = licDias, yaRegistrado = true, imagenUrl = lic.imagenUrl) else s.licencia,
                kilometrajeMinimo = cached.maxOfOrNull { it.kilometrajeActual } ?: 0
            )
        }.also { validate() }
    }

    private suspend fun cargarDesdeCache(placa: String) {
        val cached = documentoMotoDao.getByPlaca(placa)
        if (cached.isNotEmpty()) {
            val cachedItems = cached.map { Pair(it.vigencia, Triple(it.tipoDocumento, it.imagenUrl, it.kilometrajeActual)) }
            aplicarDocumentos(cachedItems)
        } else {
            _uiState.update { it.copy(isLoadingDocumentos = false) }
        }
    }

    private fun aplicarDocumentos(items: List<Pair<String, Triple<String, String?, Int>>>) {
        var soatFecha = ""; var tecnoFecha = ""; var licenciaFecha = ""
        var soatUrl: String? = null; var tecnoUrl: String? = null; var licenciaUrl: String? = null
        var kmMinimo = 0

        items.forEach { (vigencia, info) ->
            val (tipo, imgUrl, km) = info
            when (tipo) {
                "SOAT"           -> { soatFecha = vigencia; soatUrl = imgUrl }
                "REVISION_TECNO" -> { tecnoFecha = vigencia; tecnoUrl = imgUrl }
                "LICENCIA"       -> { licenciaFecha = vigencia; licenciaUrl = imgUrl }
            }
            if (km > 0) kmMinimo = km
        }

        val (soatEst, soatDias)     = calcularEstadoPorDias(soatFecha)
        val (tecnoEst, tecnoDias)   = calcularEstadoPorDias(tecnoFecha)
        val (licEst, licDias)       = calcularEstadoPorDias(licenciaFecha)

        _uiState.update {
            it.copy(
                soat          = it.soat.copy(vigenciaMaster = soatFecha, estadoDoc = soatEst, diasRestantes = soatDias, imagenUrl = soatUrl, yaRegistrado = soatFecha.isNotBlank()),
                revisionTecno = it.revisionTecno.copy(vigenciaMaster = tecnoFecha, estadoDoc = tecnoEst, diasRestantes = tecnoDias, imagenUrl = tecnoUrl, yaRegistrado = tecnoFecha.isNotBlank()),
                licencia      = it.licencia.copy(vigenciaMaster = licenciaFecha, estadoDoc = licEst, diasRestantes = licDias, imagenUrl = licenciaUrl, yaRegistrado = licenciaFecha.isNotBlank()),
                kilometrajeMinimo = kmMinimo,
                isLoadingDocumentos = false
            )
        }
    }

    /**
     * Calcula el estado de vigencia basado en la fecha del backend.
     * @SuppressLint("NewApi") se usa por Desugaring de Java 8+.
     */
    @SuppressLint("NewApi")
    private fun calcularEstadoPorDias(fechaDB: String): Pair<String, Long> {
        if (fechaDB.isBlank()) return Pair("", 0L)
        return try {
            val hoy = LocalDate.now()
            val normalizada = when {
                fechaDB.length == 10 && fechaDB.contains("-") && fechaDB.indexOf("-") == 4 -> fechaDB
                fechaDB.length == 7 && fechaDB.contains("-") && fechaDB.indexOf("-") == 4 -> "$fechaDB-01"
                fechaDB.length == 10 && fechaDB.contains("-") && fechaDB.indexOf("-") == 2 -> {
                    val p = fechaDB.split("-")
                    if (p.size == 3) "${p[2]}-${p[1]}-${p[0]}" else fechaDB
                }
                else -> fechaDB
            }
            val fechaVenc = LocalDate.parse(normalizada, DateTimeFormatter.ISO_LOCAL_DATE)
            val dias = ChronoUnit.DAYS.between(hoy, fechaVenc)
            Log.d("VencLogic", "Moto - fechaDB=$fechaDB → venc=$fechaVenc, hoy=$hoy, dias=$dias")
            val estado = when {
                dias < 0   -> "Vencido"
                dias <= 30 -> "Próximo a Vencer"
                else       -> "Vigente"
            }
            Pair(estado, dias)
        } catch (e: Exception) {
            Log.e("VencLogic", "Error parseando fecha moto: $fechaDB", e)
            Pair("", 0L)
        }
    }

    private fun validate() {
        val s = _uiState.value
        val kmInt = s.kilometraje.toIntOrNull()
        val kmOk = when {
            s.kilometraje.isBlank() -> false
            kmInt == null -> false
            s.kilometrajeMinimo > 0 && kmInt < s.kilometrajeMinimo -> false
            else -> true
        }

        // Validación de documentos simplificada: ya no bloquea el guardado

        val valid = s.selectedMoto != null &&
                s.selectedUbicacion != null &&
                s.kilometraje.isNotBlank() &&
                s.estadoVehiculo.isNotBlank() &&
                s.checkNivelAceite.isNotBlank() &&
                s.checkEstadoLlantas.isNotBlank() &&
                s.checkEstadoLuces.isNotBlank()

        _uiState.update { it.copy(isSaveButtonEnabled = valid) }
    }

    fun onSaveClick() {
        if (!_uiState.value.isSaveButtonEnabled || _uiState.value.isSaving) return

        val s = _uiState.value
        val km = s.kilometraje.toIntOrNull() ?: 0
        val kmMin = s.kilometrajeMinimo
        val diff = km - kmMin

        // Alerta ROJA: km menor al registrado — no bloquea si ya confirmó
        if (km > 0 && diff < 0 && !s.kmRedConfirmed) {
            _uiState.update { it.copy(
                showKmAlert = true,
                kmAlertMessage = "El kilometraje ingresado es menor al último kilometraje registrado. Por favor, verifíquelo."
            ) }
            return
        }

        // Alerta AMARILLA: incremento inusual (>=300) o igual — no bloquea si ya confirmó
        if (km > 0 && !s.kmYellowConfirmed && (diff == 0 || diff >= 300)) { // KM_THRESHOLD hardcoded
            val msg = if (diff == 0)
                "El kilometraje ingresado es igual al último registrado. ¿Confirma que es correcto?"
            else
                "Detectamos un incremento inusual en el kilometraje. ¿Está seguro de que es correcto?"
            _uiState.update { it.copy(
                showKmYellowAlert = true,
                kmAlertMessage = msg
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val s = _uiState.value

                val inspeccion = InspeccionMotoPendiente(
                    uuid = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    idVehiculo = s.selectedMoto!!.id,
                    kilometrajeReportado = s.kilometraje.toInt(),
                    estadoVehiculo = s.estadoVehiculo,
                    observacionesFinales = s.observaciones,
                    conscienteResponsabilidad = s.conscienteResponsabilidad,
                    aprobadoRuta = s.aprobadoRuta,

                    // Guardar solo el estado real, igual que en vehículos.
                    checkSoat     = s.soat.estadoDoc.ifBlank { "Sin registro" },
                    checkTecno    = s.revisionTecno.estadoDoc.ifBlank { "Sin registro" },
                    checkLicencia = s.licencia.estadoDoc.ifBlank { "Sin registro" },
                    checkExtintor = s.checkExtintor,

                    // Inspección Mecánica Moto
                    checkNivelAceite  = s.checkNivelAceite,
                    checkEstadoLlantas = s.checkEstadoLlantas,
                    checkEstadoLuces  = s.checkEstadoLuces,

                    // Fechas para persistencia (fecha BD - master)
                    fechaSoat     = s.soat.vigenciaMaster.ifBlank { "" },
                    fechaTecno    = s.revisionTecno.vigenciaMaster.ifBlank { "" },
                    fechaLicencia = s.licencia.vigenciaMaster.ifBlank { "" },

                    idUbicacion = s.selectedUbicacion!!.id,
                    placaVehiculo = s.selectedMoto!!.placa,
                    tipoVehiculo = "MOTOCICLETA",
                    isSynced = false
                )

                saveInspeccionMotoLocalUseCase(inspeccion)
                
                // --- ACTUALIZAR CACHÉ LOCAL ---
                try {
                    val listaDocs = listOf(
                        DocumentoMotoEntity(placa = s.selectedMoto!!.placa, tipoDocumento = "SOAT", vigencia = s.soat.vigenciaMaster, kilometrajeActual = s.kilometraje.toInt(), imagenUrl = s.soat.imagenUrl),
                        DocumentoMotoEntity(placa = s.selectedMoto!!.placa, tipoDocumento = "REVISION_TECNO", vigencia = s.revisionTecno.vigenciaMaster, kilometrajeActual = s.kilometraje.toInt(), imagenUrl = s.revisionTecno.imagenUrl),
                        DocumentoMotoEntity(placa = s.selectedMoto!!.placa, tipoDocumento = "LICENCIA", vigencia = s.licencia.vigenciaMaster, kilometrajeActual = s.kilometraje.toInt(), imagenUrl = s.licencia.imagenUrl)
                    )
                    documentoMotoDao.refreshForPlaca(s.selectedMoto!!.placa, listaDocs)
                } catch (e: Exception) {
                    android.util.Log.e("DocsMoto", "⚠️ No se pudo actualizar el cache local: ${e.message}")
                }
                
                localSyncCoordinator.coordinateSync(
                    LocalSyncCoordinator.SyncTrigger.FormSaved("Motocicleta")
                )

                delay(2000)
                _uiState.update { it.copy(isSaving = false, saveCompleted = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Error local: ${e.localizedMessage}") }
            }
        }
    }

    fun onNavigationDone() { _uiState.update { it.copy(saveCompleted = false) } }
    fun onErrorDismissed() { _uiState.update { it.copy(errorMessage = null) } }
}