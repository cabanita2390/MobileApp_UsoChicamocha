package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.DocumentoMotoDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalUbicacionesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SaveInspeccionMotoLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SyncInspeccionMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

/** Estado de un documento individual (SOAT, Tecno, Licencia) */
data class DocumentoState(
    val vigencia: String = "",          // fecha de vencimiento "AAAA-MM" o "AAAA-MM-DD"
    val estadoDoc: String = "",         // Vigente / Próximo a vencer / Vencido
    val imagenUrl: String? = null,      // URL de la imagen guardada en servidor
    val imagenUri: Uri? = null,         // URI local de imagen nueva (cámara/galería)
    val yaRegistrado: Boolean = false,  // true si ya existe en el sistema (para bloqueo)
    val vigenciaMaster: String = ""    // Fecha de referencia inalterable (BAse de Datos Maestra)
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

    // Documentos agrupados en DocumentoState
    val soat: DocumentoState = DocumentoState(),
    val revisionTecno: DocumentoState = DocumentoState(),
    val licencia: DocumentoState = DocumentoState(),

    val checkExtintor: String = "No Aplica",
    val isLoadingDocumentos: Boolean = false,
    val estadoVehiculo: String = "",
    val observaciones: String = "",
    val responsable: String = "",

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
    @ApplicationContext private val context: Context,
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
        _uiState.update { it.copy(kilometraje = value) }

        val valueInt = value.toIntOrNull()
        val kmMinimo = _uiState.value.kilometrajeMinimo
        val errorMsg = when {
            value.isBlank() -> null
            valueInt == null -> "Ingrese un número válido"
            kmMinimo > 0 && valueInt < kmMinimo -> "El kilometraje debe ser mayor o igual al último registrado ($kmMinimo km)"
            else -> null
        }
        _uiState.update { it.copy(kilometrajeError = errorMsg) }
        validate()
    }

    fun onEstadoVehiculoChange(value: String) {
        _uiState.update { it.copy(estadoVehiculo = value) }
        validate()
    }

    fun onObservacionesChange(value: String) {
        _uiState.update { it.copy(observaciones = value) }
        validate()
    }

    fun onSoatVigenciaChange(year: Int, month: Int, day: Int) {
        val fechaSelected = buildFecha(year, month, day)
        val estado = calcularEstadoVsDB(fechaSelected, _uiState.value.soat.vigenciaMaster)
        _uiState.update { it.copy(soat = it.soat.copy(vigencia = fechaSelected, estadoDoc = estado)) }
        validate()
    }

    // ─── REVISIÓN TECNO ─────────────────────────────────────────────────────
    fun onRevisionVigenciaChange(year: Int, month: Int, day: Int) {
        val fechaSelected = buildFecha(year, month, day)
        val estado = calcularEstadoVsDB(fechaSelected, _uiState.value.revisionTecno.vigenciaMaster)
        _uiState.update { it.copy(revisionTecno = it.revisionTecno.copy(vigencia = fechaSelected, estadoDoc = estado)) }
        validate()
    }

    // ─── LICENCIA ───────────────────────────────────────────────────────────
    fun onLicenciaVigenciaChange(year: Int, month: Int, day: Int) {
        val fechaSelected = buildFecha(year, month, day)
        val estado = calcularEstadoVsDB(fechaSelected, _uiState.value.licencia.vigenciaMaster)
        _uiState.update { it.copy(licencia = it.licencia.copy(vigencia = fechaSelected, estadoDoc = estado)) }
        validate()
    }

    // ─── CARGA DE DOCUMENTOS ────────────────────────────────────────────────
    private fun loadDocumentosForMoto(placa: String) {
        viewModelScope.launch {
            // Reset UI state for documents before loading
            val hoy = getTodayDate()
            _uiState.update { it.copy(
                isLoadingDocumentos = true,
                soat          = DocumentoState(vigencia = hoy),
                revisionTecno = DocumentoState(vigencia = hoy),
                licencia      = DocumentoState(vigencia = hoy)
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
                    android.util.Log.d("DocsMoto", "✅ [API] Recibidos ${apiDocs.size} documentos de la última inspección")
                    
                    // Actualizamos el cache local con lo que diga el servidor (Sync cross-device)
                    apiDocs.forEach { apiDoc ->
                        val fechaApi = apiDoc.fechaVencimiento ?: apiDoc.mesyear ?: ""
                        if (fechaApi.isNotBlank()) {
                            documentoMotoDao.insert(
                                com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity(
                                    tipoDocumento = apiDoc.tipoDocumento,
                                    placa = placa,
                                    vigencia = fechaApi,
                                    kilometrajeActual = apiDoc.vehiculoKilometrajeActual ?: 0,
                                    imagenUrl = apiDoc.imagenUrl
                                )
                            )
                        }
                    }

                    // 3. Kilometraje mínimo desde API
                    val kmMinimo = apiDocs.mapNotNull { it.vehiculoKilometrajeActual }.firstOrNull() ?: 0

                    // 4. Aplicar al UI (mezclando con lo que ya tenemos)
                    _uiState.update { s ->
                        val soatApi   = apiDocs.find { it.tipoDocumento == "SOAT" }
                        val tecnoApi  = apiDocs.find { it.tipoDocumento == "REVISION_TECNO" }
                        val licApi    = apiDocs.find { it.tipoDocumento == "LICENCIA" }

                        // Para la FECHA: prioridad al servidor. Si viene blank, usar fecha actual
                        val hoy2 = getTodayDate()
                        val soatFecha  = soatApi?.fechaVencimiento?.toString()?.take(7)?.takeIf { it.isNotBlank() } ?: (soatApi?.mesyear?.takeIf { it.isNotBlank() } ?: hoy2)
                        val tecnoFecha = tecnoApi?.fechaVencimiento?.toString()?.take(7)?.takeIf { it.isNotBlank() } ?: (tecnoApi?.mesyear?.takeIf { it.isNotBlank() } ?: hoy2)
                        val licFecha   = licApi?.fechaVencimiento?.toString()?.take(7)?.takeIf { it.isNotBlank() } ?: (licApi?.mesyear?.takeIf { it.isNotBlank() } ?: hoy2)

                        // Para el ESTADO: LITERALMENTE LA MISMA LÓGICA COMPARATIVA QUE VEHÍCULOS
                        // Comparamos Hoy vs Vencimiento para el estado inicial
                        val hoy = getTodayDate()
                        val soatEstado  = calcularEstadoVsDB(hoy, soatFecha)
                        val tecnoEstado = calcularEstadoVsDB(hoy, tecnoFecha)
                        val licEstado   = calcularEstadoVsDB(hoy, licFecha)

                        android.util.Log.d("DocsMoto", "🔄 [FinalState] PLACA: $placa - SOAT: $soatEstado/$soatFecha TECNO: $tecnoEstado/$tecnoFecha LIC: $licEstado/$licFecha")

                        s.copy(
                            soat          = s.soat.copy(vigencia = hoy2, vigenciaMaster = soatFecha, estadoDoc = soatEstado, yaRegistrado = true, imagenUrl = soatApi?.imagenUrl),
                            revisionTecno = s.revisionTecno.copy(vigencia = hoy2, vigenciaMaster = tecnoFecha, estadoDoc = tecnoEstado, yaRegistrado = true, imagenUrl = tecnoApi?.imagenUrl),
                            licencia      = s.licencia.copy(vigencia = hoy2, vigenciaMaster = licFecha, estadoDoc = licEstado, yaRegistrado = true, imagenUrl = licApi?.imagenUrl),
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
        val soat   = cached.find { it.tipoDocumento == "SOAT" }
        val tecno  = cached.find { it.tipoDocumento == "REVISION_TECNO" }
        val lic    = cached.find { it.tipoDocumento == "LICENCIA" }

        val hoy = getTodayDate()
        _uiState.update { s ->
            s.copy(
                soat          = if (soat != null) s.soat.copy(vigencia = hoy, vigenciaMaster = soat.vigencia, estadoDoc = calcularEstadoVsDB(hoy, soat.vigencia), yaRegistrado = true) else s.soat,
                revisionTecno = if (tecno != null) s.revisionTecno.copy(vigencia = hoy, vigenciaMaster = tecno.vigencia, estadoDoc = calcularEstadoVsDB(hoy, tecno.vigencia), yaRegistrado = true) else s.revisionTecno,
                licencia      = if (lic != null) s.licencia.copy(vigencia = hoy, vigenciaMaster = lic.vigencia, estadoDoc = calcularEstadoVsDB(hoy, lic.vigencia), yaRegistrado = true) else s.licencia,
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
                "SOAT"          -> { soatFecha = vigencia; soatUrl = imgUrl }
                "REVISION_TECNO"-> { tecnoFecha = vigencia; tecnoUrl = imgUrl }
                "LICENCIA"      -> { licenciaFecha = vigencia; licenciaUrl = imgUrl }
            }
            if (km > 0) kmMinimo = km
        }

        _uiState.update {
            it.copy(
                soat         = it.soat.copy(vigencia = soatFecha, estadoDoc = calcularEstado(soatFecha), imagenUrl = soatUrl, yaRegistrado = soatFecha.isNotBlank()),
                revisionTecno= it.revisionTecno.copy(vigencia = tecnoFecha, estadoDoc = calcularEstado(tecnoFecha), imagenUrl = tecnoUrl, yaRegistrado = tecnoFecha.isNotBlank()),
                licencia     = it.licencia.copy(vigencia = licenciaFecha, estadoDoc = calcularEstado(licenciaFecha), imagenUrl = licenciaUrl, yaRegistrado = licenciaFecha.isNotBlank()),
                kilometrajeMinimo = kmMinimo,
                isLoadingDocumentos = false
            )
        }
    }

    private fun buildFecha(year: Int, month: Int, day: Int): String {
        // month ya viene en 1-12 desde el picker
        return if (day > 0)
            "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
        else
            "$year-${month.toString().padStart(2, '0')}"
    }

    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        return "$y-${m.toString().padStart(2, '0')}"
    }

    private fun calcularEstado(vigencia: String): String {
        return calcularEstadoVsDB(getTodayDate(), vigencia)
    }

    /**
     * LITERALMENTE EL MISMO CÁLCULO QUE VEHÍCULOS
     */
    private fun calcularEstadoVsDB(fechaIngresada: String, fechaDB: String): String {
        if (fechaIngresada.isBlank() || fechaDB.isBlank()) return ""
        return try {
            val pi = fechaIngresada.split("-")
            val pd = fechaDB.split("-")
            val valIng = pi[0].toInt() * 12 + pi[1].toInt()
            val valDB  = pd[0].toInt() * 12 + pd[1].toInt()
            when {
                valIng > valDB          -> "Vencido"
                valIng >= valDB - 1     -> "Próximo a Vencer"
                else                    -> "Vigente"
            }
        } catch (e: Exception) { "" }
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

        // Validación de documentos: debe tener fecha Y el estado calculado/confirmado no debe estar vacío
        val docsOk = s.soat.vigencia.isNotBlank() && s.soat.estadoDoc.isNotBlank() &&
                    s.revisionTecno.vigencia.isNotBlank() && s.revisionTecno.estadoDoc.isNotBlank() &&
                    s.licencia.vigencia.isNotBlank() && s.licencia.estadoDoc.isNotBlank()

        val obsOk = s.observaciones.isNotBlank()
        val kmErrorFree = s.kilometrajeError == null

        val valid = s.selectedMoto != null &&
                s.selectedUbicacion != null &&
                kmOk && kmErrorFree && docsOk && obsOk &&
                s.estadoVehiculo.isNotBlank()

        _uiState.update { it.copy(isSaveButtonEnabled = valid) }
    }

    fun onSaveClick() {
        if (!_uiState.value.isSaveButtonEnabled || _uiState.value.isSaving) return
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

                    // Guardar solo el estado real, igual que en vehículos.
                    checkSoat     = s.soat.estadoDoc.ifBlank { "Sin registro" },
                    checkTecno    = s.revisionTecno.estadoDoc.ifBlank { "Sin registro" },
                    checkLicencia = s.licencia.estadoDoc.ifBlank { "Sin registro" },
                    checkExtintor = s.checkExtintor,

                    // Fechas para persistencia/sincronización (NUEVO)
                    fechaSoat     = s.soat.vigencia,
                    fechaTecno    = s.revisionTecno.vigencia,
                    fechaLicencia = s.licencia.vigencia,

                    idUbicacion = s.selectedUbicacion!!.id,
                )

                saveInspeccionMotoLocalUseCase(inspeccion)
                
                // --- NUEVO: Actualizar caché local de documentos con las nuevas fechas ---
                try {
                    val listaDocs = listOf(
                        DocumentoMotoEntity(placa = s.selectedMoto!!.placa, tipoDocumento = "SOAT", vigencia = s.soat.vigencia, kilometrajeActual = s.kilometraje.toInt(), imagenUrl = s.soat.imagenUrl),
                        DocumentoMotoEntity(placa = s.selectedMoto!!.placa, tipoDocumento = "REVISION_TECNO", vigencia = s.revisionTecno.vigencia, kilometrajeActual = s.kilometraje.toInt(), imagenUrl = s.revisionTecno.imagenUrl),
                        DocumentoMotoEntity(placa = s.selectedMoto!!.placa, tipoDocumento = "LICENCIA", vigencia = s.licencia.vigencia, kilometrajeActual = s.kilometraje.toInt(), imagenUrl = s.licencia.imagenUrl)
                    )
                    documentoMotoDao.refreshForPlaca(s.selectedMoto!!.placa, listaDocs)
                    android.util.Log.d("DocsMoto", "💾 [Cache] Actualizado localmente tras guardado exitoso")
                } catch (e: Exception) {
                    android.util.Log.e("DocsMoto", "⚠️ No se pudo actualizar el cache local: ${e.message}")
                }
                
                // 2. Usar el coordinador para iniciar la sincronización (WorkManager)
                localSyncCoordinator.coordinateSync(
                    LocalSyncCoordinator.SyncTrigger.FormSaved("Motocicleta")
                )

                // 3. Simular tiempo de espera para mejor UX y asegurar sync
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
