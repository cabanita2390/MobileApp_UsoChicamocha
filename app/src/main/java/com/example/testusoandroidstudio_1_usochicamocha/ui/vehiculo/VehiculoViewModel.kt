package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.toVehiculoItem
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import androidx.work.*
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toVehiculoItem
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.SyncDataWorker
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SaveVehiculoInspectionUseCase
import java.util.UUID
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─── VEHICLE CATALOG ITEM ───────────────────────────────────────────────────
// Representa un vehículo del catálogo de la BD (tabla: vehiculos)
data class VehiculoItem(
    val idVehiculo: Int,
    val placa: String,
    val marca: String = "",
    val tipoVehiculo: String = "",
    val kilometrajeActual: Int = 0
)


// ─── UI STATE ────────────────────────────────────────────────────────────────
data class VehiculoUiState(
    // Datos generales
    val vehicles: List<VehiculoItem> = emptyList(),   // catálogo de vehículos
    val selectedVehicle: VehiculoItem? = null,
    val kilometraje: String = "",
    // ... rest of fields
    val nivelAceite: String = "",
    val nivelRefrigerante: String = "",
    val nivelFrenos: String = "",
    val estadoLlantas: String = "",
    val lucesGeneral: String = "",
    val estadoVisual: String = "",
    val limpiezaGeneral: String = "",
    // Documentación — fechas del backend (referencia, no editables)
    val fechaVencSoatDB: String = "",
    val fechaVencTecnoDB: String = "",
    val fechaVencLicencioDB: String = "",
    val vigenciaExtintorDB: String = "",
    // Documentación — estado calculado comparando fecha ingresada vs fecha DB
    val estadoSoat: String = "",
    val estadoTecno: String = "",
    val estadoLicencia: String = "",
    val estadoExtintor: String = "",
    // Fechas que ingresa el inspector (picker, inicia en hoy)
    val vigenciaExtintor: String = "",
    val fechaVencSoat: String = "",
    val fechaVencTecno: String = "",
    val fechaVencLicencia: String = "",
    val urlImagenSoat: String? = null,
    val urlImagenTecno: String? = null,
    val urlImagenLicencia: String? = null,
    val urlImagenExtintor: String? = null,
    val isLoadingDocs: Boolean = false,
    // Elementos (Si / No)
    val tieneBotiquin: String = "",
    val tieneSeñalizacion: String = "",
    val tieneLineasEmergencia: String = "",
    val tieneLlantaRepuesto: String = "",
    val tieneGatoHidraulico: String = "",
    // Salud conductor (Si / No)
    val saludFisica: String = "",
    val saludMental: String = "",
    val sobrio: String = "",
    val medicamentos: String = "",
    val condicionParaConducir: String = "",
    // Cierre
    val conscienteResponsabilidad: String = "",
    val aprobadoRuta: String = "",
    val observaciones: String = "",
    val responsableInspeccion: String = "",
    // UI control
    val isSaveButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val saveCompleted: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val errorMessage: String? = null,
    // AlertDialogs y lógica de doble "No" en campos de cierre
    val showConscienteAlert: Boolean = false,
    val conscienteNoCount: Int = 0,
    val showAprobadoAlert: Boolean = false,
    val aprobadoNoCount: Int = 0,
    val shouldExitForm: Boolean = false,
    // ─ Alerta de kilometraje ───────────────────────────────────────
    // ─ Alerta de kilometraje ───────────────────────────────────────
    // ─ Alerta de kilometraje ───────────────────────────────────────
    val showKmAlert: Boolean = false,
    val kmAlertMessage: String = "",
    val kmEsInvalido: Boolean = false,    // true = bloquea el botón Guardar
    val kilometrajeMinimo: Int = 0,
    val kilometrajeDB: String = ""
)

// ─── VIEWMODEL ───────────────────────────────────────────────────────────────
@HiltViewModel
class VehiculoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val repository: VehiculoInspectionRepository,
    private val saveVehiculoInspectionUseCase: SaveVehiculoInspectionUseCase,
    private val workManager: WorkManager,
    private val localSyncCoordinator: LocalSyncCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoUiState())
    val uiState: StateFlow<VehiculoUiState> = _uiState.asStateFlow()

    // Job para el debounce de validación de kilometraje
    private var kmValidationJob: Job? = null

    init {
        observeVehiclesCatalog()
        loadUsername()
    }

    // ── Carga el username del inspector logueado (campo de solo lectura) ──────
    private fun loadUsername() {
        viewModelScope.launch {
            val username = tokenManager.getUsername().first() ?: ""
            _uiState.update { it.copy(responsableInspeccion = username) }
        }
    }

    // ── Observa vehículos desde el repositorio (Room) ────────────────────────
    private fun observeVehiclesCatalog() {
        repository.getLocalVehiclesFlow()
            .onEach { entities ->
                val list = entities.map { it.toVehiculoItem() }
                _uiState.update { it.copy(vehicles = list) }
            }
            .launchIn(viewModelScope)
    }

    // ── Pre-llena estados de documentos al seleccionar vehículo ────────────────────────
    private fun loadDocumentosVehiculo(idVehiculo: Int) {
        val placa = _uiState.value.selectedVehicle?.placa ?: return
        _uiState.update { it.copy(isLoadingDocs = true) }
        val cal = Calendar.getInstance()
        val hoy = "${cal.get(Calendar.YEAR)}-${String.format("%02d", cal.get(Calendar.MONTH) + 1)}"
        
        viewModelScope.launch {
            // 1. CARGA INMEDIATA DESDE CACHE
            val cached = repository.getCachedDocuments(placa)
            if (cached.isNotEmpty()) {
                aplicarDocumentosDesdeCache(cached)
            }

            // 2. REFRESCO DESDE API (si hay internet)
            try {
                val response = apiService.getDocumentosVehiculo(idVehiculo)
                if (response.isSuccessful) {
                    val doc = response.body() ?: run {
                        _uiState.update { it.copy(isLoadingDocs = false) }
                        return@launch
                    }
                    // Fechas del backend (referencia para la comparación)
                    val soatDB     = doc.fechaVencSoat?.take(7)     ?: ""
                    val tecnoDB    = doc.fechaVencTecno?.take(7)    ?: ""
                    val licDB      = doc.fechaVencLicencia?.take(7) ?: ""
                    val extDB      = doc.fechaVencExtintor?.take(7) ?: ""
                    
                    _uiState.update { s ->
                        s.copy(
                            // Fechas DB (referencia, se muestran al inspector)
                            fechaVencSoatDB     = soatDB,
                            fechaVencTecnoDB    = tecnoDB,
                            fechaVencLicencioDB = licDB,
                            vigenciaExtintorDB  = extDB,
                            // El picker inicia en HOY; el estado se calcula vs la fecha DB
                            fechaVencSoat     = hoy,
                            fechaVencTecno    = hoy,
                            fechaVencLicencia = hoy,
                            vigenciaExtintor  = hoy,
                            estadoSoat     = calcularEstadoVsDB(hoy, soatDB),
                            estadoTecno    = calcularEstadoVsDB(hoy, tecnoDB),
                            estadoLicencia = calcularEstadoVsDB(hoy, licDB),
                            estadoExtintor = calcularEstadoVsDB(hoy, extDB),
                            urlImagenSoat = doc.urlImagenSoat,
                            urlImagenTecno = doc.urlImagenTecno,
                            urlImagenLicencia = doc.urlImagenLicencia,
                            urlImagenExtintor = doc.urlImagenExtintor,
                            isLoadingDocs  = false
                        )
                    }
                    
                    // 3. ACTUALIZAR CACHE CON LO DEL API
                    try {
                        val docsToCache = listOf(
                            DocumentoVehiculoEntity(placa = placa, tipoDocumento = "SOAT",     vigencia = soatDB, imagenUrl = doc.urlImagenSoat,     kilometrajeActual = 0),
                            DocumentoVehiculoEntity(placa = placa, tipoDocumento = "TECNO",    vigencia = tecnoDB, imagenUrl = doc.urlImagenTecno,   kilometrajeActual = 0),
                            DocumentoVehiculoEntity(placa = placa, tipoDocumento = "LICENCIA", vigencia = licDB, imagenUrl = doc.urlImagenLicencia,    kilometrajeActual = 0),
                            DocumentoVehiculoEntity(placa = placa, tipoDocumento = "EXTINTOR", vigencia = extDB, imagenUrl = doc.urlImagenExtintor,   kilometrajeActual = 0)
                        )
                        repository.refreshCachedDocuments(placa, docsToCache)
                    } catch (e: Exception) {
                        Log.e("VehiculoVM", "Error actualizando cache desde API", e)
                    }

                    Log.d("VehiculoVM", "✅ Documentos cargados para id=$idVehiculo")
                } else {
                    Log.w("VehiculoVM", "Sin documentos para id=$idVehiculo (${response.code()})")
                    _uiState.update { it.copy(isLoadingDocs = false) }
                }
            } catch (e: Exception) {
                Log.e("VehiculoVM", "Error cargando documentos del vehículo", e)
                _uiState.update { it.copy(isLoadingDocs = false) }
            }
        }
    }

    private fun aplicarDocumentosDesdeCache(cached: List<DocumentoVehiculoEntity>) {
        val soat = cached.find { it.tipoDocumento == "SOAT" }
        val tecno = cached.find { it.tipoDocumento == "TECNO" }
        val lic = cached.find { it.tipoDocumento == "LICENCIA" }
        val ext = cached.find { it.tipoDocumento == "EXTINTOR" }

        val cal = Calendar.getInstance()
        val hoy = "${cal.get(Calendar.YEAR)}-${String.format("%02d", cal.get(Calendar.MONTH) + 1)}"

        _uiState.update { s ->
            s.copy(
                fechaVencSoatDB = soat?.vigencia ?: "",
                fechaVencTecnoDB = tecno?.vigencia ?: "",
                fechaVencLicencioDB = lic?.vigencia ?: "",
                vigenciaExtintorDB = ext?.vigencia ?: "",
                
                // Picker inicia en Hoy
                fechaVencSoat = hoy,
                fechaVencTecno = hoy,
                fechaVencLicencia = hoy,
                vigenciaExtintor = hoy,

                estadoSoat = calcularEstadoVsDB(hoy, soat?.vigencia ?: ""),
                estadoTecno = calcularEstadoVsDB(hoy, tecno?.vigencia ?: ""),
                estadoLicencia = calcularEstadoVsDB(hoy, lic?.vigencia ?: ""),
                estadoExtintor = calcularEstadoVsDB(hoy, ext?.vigencia ?: ""),
                
                urlImagenSoat = soat?.imagenUrl,
                urlImagenTecno = tecno?.imagenUrl,
                urlImagenLicencia = lic?.imagenUrl,
                urlImagenExtintor = ext?.imagenUrl,

                // Kilometraje desde cache
                kilometrajeMinimo = cached.maxOfOrNull { it.kilometrajeActual } ?: 0,
                kilometrajeDB     = (cached.maxOfOrNull { it.kilometrajeActual } ?: 0).toString(),
                
                isLoadingDocs = false
            )
        }
    }



    // ── Validación de kilometraje contra el backend ───────────────────────
    private suspend fun validarKilometrajeConBackend(placa: String, km: Int) {
        try {
            val response = apiService.validarKilometraje(placa, km)
            if (response.isSuccessful) {
                val resultado = response.body() ?: return
                if (resultado.alerta) {
                    // Km incorrecto: muestra alerta Y bloquea el botón Guardar
                    _uiState.update { it.copy(
                        showKmAlert = true,
                        kmAlertMessage = resultado.mensaje,
                        kmEsInvalido = true
                    ) }
                    validateForm()
                } else {
                    // Km correcto: guardamos como nuevo mínimo para validación offline
                    _uiState.update { it.copy(kmEsInvalido = false, kilometrajeMinimo = km, kilometrajeDB = km.toString()) }
                    validateForm()
                    
                    // Guardar en caché para que offline use este valor
                    try {
                        // 1. Actualizar documentos (lo que ya hacíamos)
                        val docsCache = repository.getCachedDocuments(placa)
                        if (docsCache.isNotEmpty()) {
                            val docsActualizados = docsCache.map { it.copy(kilometrajeActual = km) }
                            repository.refreshCachedDocuments(placa, docsActualizados)
                        }
                        
                        // 2. IMPORTANTE: Actualizar el catálogo de vehículos (VehiculoEntity)
                        // Esto es lo que permite que al volver a elegir el vehículo offline, el kmMinimo sea el correcto.
                        repository.updateVehicleMileage(placa, km)
                        
                        Log.d("VehiculoVM", "✅ km mínimo actualizado en catálogo y caché: $km")
                    } catch (e: Exception) {
                        Log.w("VehiculoVM", "No se pudo actualizar km en caché/catálogo: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("VehiculoVM", "Error validando kilometraje (sin conexión): ${e.message}")
        }
    }

    fun onKmAlertDismiss() { _uiState.update { it.copy(showKmAlert = false) } }

    // ── Datos generales ─────────────────────────────────────────────────────────────────
    fun setVehicles(list: List<VehiculoItem>) { _uiState.update { it.copy(vehicles = list) } }
    fun onKilometrajeChange(v: String) {
        val km = v.toIntOrNull() ?: 0
        val kmMin = _uiState.value.kilometrajeMinimo
        
        // Validación local (igual que en motos)
        val isLocalInvalid = km > 0 && km < kmMin
        val alertMsg = if (isLocalInvalid) {
            "El kilometraje ingresado ($km km) es menor al registrado ($kmMin km)."
        } else {
            ""
        }

        _uiState.update { it.copy(
            kilometraje = v, 
            kmEsInvalido = isLocalInvalid,
            // showKmAlert = isLocalInvalid, // REMOVIDO: No mostrar inmediatamente al escribir
            kmAlertMessage = alertMsg
        ) }
        
        validateForm()
        
        // Manejo de Alerta y Backend con Debounce
        val placa = _uiState.value.selectedVehicle?.placa ?: return
        kmValidationJob?.cancel()
        kmValidationJob = viewModelScope.launch {
            delay(800)
            
            // 1. Mostrar alerta local si sigue siendo inválido después del delay
            if (isLocalInvalid) {
                _uiState.update { it.copy(showKmAlert = true) }
            }
            
            // 2. Solo llamar al backend si no hay error local y hay conexión planeada
            if (!isLocalInvalid && km > 0 && km > kmMin) {
                validarKilometrajeConBackend(placa, km)
            }
        }
    }

    fun onVehicleSelected(v: VehiculoItem) {
        _uiState.update { it.copy(
            selectedVehicle = v,
            kilometrajeMinimo = v.kilometrajeActual,
            kilometrajeDB = v.kilometrajeActual.toString()
        ) }
        loadDocumentosVehiculo(v.idVehiculo)
        // Si ya hay un km ingresado, validarlo contra el nuevo vehículo
        val kmStr = _uiState.value.kilometraje
        val km = kmStr.toIntOrNull()
        if (km != null) {
            onKilometrajeChange(kmStr) // Re-validar inmediatamente (incluye backend si es válido)
        }
        validateForm()
    }


    // ── Mecánica ─────────────────────────────────────────────────────────────
    fun onMecanicoChange(field: String, value: String) {
        _uiState.update { s ->
            when (field) {
                "Aceite"       -> s.copy(nivelAceite = value)
                "Refrigerante" -> s.copy(nivelRefrigerante = value)
                "Frenos"       -> s.copy(nivelFrenos = value)
                "Llantas"      -> s.copy(estadoLlantas = value)
                "Luces"        -> s.copy(lucesGeneral = value)
                "Visual"       -> s.copy(estadoVisual = value)
                "Limpieza"     -> s.copy(limpiezaGeneral = value)
                else           -> s
            }
        }
        validateForm()
    }

    // ── Documentación ──────────────────────────────────────────────────────────────
    /** El inspector ingresa el mes y año que ve en el documento físico.
     *  El código compara contra la fecha registrada en el backend (fechaDB):
     *  - Ingresada DESPUÉS de la DB  → Vencido   (como las galletas expiradas)
     *  - Ingresada en el mismo mes o el anterior → Próximo a Vencer
     *  - Ingresada 2+ meses ANTES de la DB → Vigente
     */
    fun onDocFechaVencChange(doc: String, date: String) {
        val s = _uiState.value
        val dbDate = when (doc) {
            "SOAT"     -> s.fechaVencSoatDB
            "Tecno"    -> s.fechaVencTecnoDB
            "Licencia" -> s.fechaVencLicencioDB
            else       -> ""
        }
        val estado = calcularEstadoVsDB(date, dbDate)
        _uiState.update { st ->
            when (doc) {
                "SOAT"     -> st.copy(fechaVencSoat     = date, estadoSoat     = estado)
                "Tecno"    -> st.copy(fechaVencTecno    = date, estadoTecno    = estado)
                "Licencia" -> st.copy(fechaVencLicencia = date, estadoLicencia = estado)
                else       -> st
            }
        }
        validateForm()
    }

    fun onExtintorDateChange(year: Int, month: Int) {
        val vigencia = "$year-${String.format("%02d", month + 1)}"
        val estado = calcularEstadoVsDB(vigencia, _uiState.value.vigenciaExtintorDB)
        _uiState.update { it.copy(vigenciaExtintor = vigencia, estadoExtintor = estado) }
        validateForm()
    }

    /**
     * Compara la fecha ingresada por el inspector vs la fecha de vencimiento de la BD.
     * Ejemplo: DB dice agosto 2026, inspector ingresa septiembre 2026
     *          → septiembre > agosto → Vencido (como las galletas del mes 5 en mes 6)
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

    private fun calcularEstadoExtintor(vigencia: String): String {
        if (vigencia.isBlank()) return ""
        return try {
            val partes  = vigencia.split("-")
            val cal = Calendar.getInstance()
            val valVenc = partes[0].toInt() * 12 + partes[1].toInt()
            val valHoy  = cal.get(Calendar.YEAR) * 12 + (cal.get(Calendar.MONTH) + 1)
            when {
                valVenc < valHoy          -> "Vencido"
                valVenc <= valHoy + 1     -> "Próximo a Vencer"
                else                      -> "Vigente"
            }
        } catch (e: Exception) { "" }
    }



    // ── Elementos ────────────────────────────────────────────────────────────
    fun onElementoChange(item: String, value: String) {
        _uiState.update { s ->
            when (item) {
                "Botiquin"         -> s.copy(tieneBotiquin = value)
                "Señalizacion"     -> s.copy(tieneSeñalizacion = value)
                "LineasEmergencia" -> s.copy(tieneLineasEmergencia = value)
                "LlantaRepuesto"   -> s.copy(tieneLlantaRepuesto = value)
                "GatoHidraulico"   -> s.copy(tieneGatoHidraulico = value)
                else               -> s
            }
        }
        validateForm()
    }

    // ── Salud ────────────────────────────────────────────────────────────────
    fun onSaludChange(item: String, value: String) {
        _uiState.update { s ->
            when (item) {
                "SaludFisica"           -> s.copy(saludFisica = value)
                "SaludMental"           -> s.copy(saludMental = value)
                "Sobrio"                -> s.copy(sobrio = value)
                "Medicamentos"          -> s.copy(medicamentos = value)
                "CondicionParaConducir" -> s.copy(condicionParaConducir = value)
                else                    -> s
            }
        }
        validateForm()
    }

    // ── Cierre ───────────────────────────────────────────────────────────────
    // 1er "No" → AlertDialog con Cancelar (puede quedarse).
    // 2do "No" → AlertDialog sin Cancelar (salida obligatoria).
    fun onConscienteChange(v: String) {
        if (v == "No") {
            val newCount = _uiState.value.conscienteNoCount + 1
            _uiState.update { it.copy(showConscienteAlert = true, conscienteNoCount = newCount) }
        } else {
            _uiState.update { it.copy(conscienteResponsabilidad = "Si", showConscienteAlert = false, conscienteNoCount = 0) }
            validateForm()
        }
    }

    fun onAprobadoRutaChange(v: String) {
        if (v == "No") {
            val newCount = _uiState.value.aprobadoNoCount + 1
            _uiState.update { it.copy(showAprobadoAlert = true, aprobadoNoCount = newCount) }
        } else {
            _uiState.update { it.copy(aprobadoRuta = "Si", showAprobadoAlert = false, aprobadoNoCount = 0) }
            validateForm()
        }
    }

    fun onConscienteAlertDismiss() { _uiState.update { it.copy(showConscienteAlert = false) } }
    fun onAprobadoAlertDismiss()   { _uiState.update { it.copy(showAprobadoAlert  = false) } }
    fun onExitFormHandled()        { _uiState.update { it.copy(shouldExitForm = false) } }


    fun onObservacionesChange(v: String) { _uiState.update { it.copy(observaciones = v) } }

    // ── Validación ─────────────────────────────────────────────────────────────────
    private fun validateForm() {
        val s = _uiState.value
        val valid = s.selectedVehicle != null && s.kilometraje.isNotBlank() &&
                s.nivelAceite.isNotBlank() && s.nivelRefrigerante.isNotBlank() &&
                s.nivelFrenos.isNotBlank() && s.estadoLlantas.isNotBlank() &&
                s.lucesGeneral.isNotBlank() && s.estadoVisual.isNotBlank() &&
                s.limpiezaGeneral.isNotBlank() &&
                // Documentos: el inspector debe confirmar el estado de cada uno
                s.estadoSoat.isNotBlank() && s.estadoTecno.isNotBlank() &&
                s.estadoLicencia.isNotBlank() && s.estadoExtintor.isNotBlank() &&
                s.tieneBotiquin.isNotBlank() && s.tieneSeñalizacion.isNotBlank() &&
                s.tieneLineasEmergencia.isNotBlank() && s.tieneLlantaRepuesto.isNotBlank() &&
                s.tieneGatoHidraulico.isNotBlank() &&
                s.saludFisica.isNotBlank() && s.saludMental.isNotBlank() &&
                s.sobrio.isNotBlank() && s.medicamentos.isNotBlank() &&
                s.condicionParaConducir.isNotBlank() &&
                s.conscienteResponsabilidad.isNotBlank() &&
                s.aprobadoRuta.isNotBlank() &&
                s.responsableInspeccion.isNotBlank() &&  // se rellena automáticamente desde el login
                !s.kmEsInvalido                          // km no puede ser menor al registrado
        _uiState.update { it.copy(isSaveButtonEnabled = valid) }
    }

    // ── Guardar ───────────────────────────────────────────────────────────────
    fun onSaveClick() {
        if (!_uiState.value.isSaveButtonEnabled) return
        val s = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Generamos un UUID único para esta inspección (deduplicación)
                val inspectionUuid = UUID.randomUUID().toString()
                
                // Creamos la entidad para persistencia local
                val entity = VehiculoInspectionEntity(
                    UUID                   = inspectionUuid,
                    timestamp              = System.currentTimeMillis(),
                    placaVehiculo          = s.selectedVehicle?.placa ?: "",
                    marca                  = s.selectedVehicle?.marca ?: "",
                    tipoVehiculo           = s.selectedVehicle?.tipoVehiculo ?: "",
                    kilometrajeReportado   = s.kilometraje.toIntOrNull() ?: 0,
                    responsableInspeccion  = s.responsableInspeccion,
                    aprobadoRuta           = s.aprobadoRuta == "Si",
                    observacionesFinales   = s.observaciones,
                    nivelAceite            = s.nivelAceite,
                    nivelRefrigerante      = s.nivelRefrigerante,
                    nivelFrenos            = s.nivelFrenos,
                    estadoLlantas          = s.estadoLlantas,
                    lucesGeneral           = s.lucesGeneral,
                    estadoVisual           = s.estadoVisual,
                    limpiezaGeneral        = s.limpiezaGeneral,
                    checkSoat              = s.estadoSoat,
                    checkTecno             = s.estadoTecno,
                    checkLicencia          = s.estadoLicencia,
                    checkExtintor          = s.estadoExtintor,
                    vigenciaExtintor       = s.vigenciaExtintor,
                    fechaVencSoat          = s.fechaVencSoat,
                    fechaVencTecno         = s.fechaVencTecno,
                    fechaVencLicencia      = s.fechaVencLicencia,
                    tieneBotiquin          = s.tieneBotiquin == "Si",
                    tieneSeñalizacion      = s.tieneSeñalizacion == "Si",
                    tieneLineasEmergencia  = s.tieneLineasEmergencia == "Si",
                    tieneLlantaRepuesto    = s.tieneLlantaRepuesto == "Si",
                    tieneGatoHidraulico    = s.tieneGatoHidraulico == "Si",
                    saludFisica            = s.saludFisica == "Si",
                    saludMental            = s.saludMental == "Si",
                    sobrio                 = s.sobrio == "Si",
                    medicamentos           = s.medicamentos == "Si",
                    conscienteResponsabilidad = s.conscienteResponsabilidad == "Si",
                    condicionParaConducir  = s.condicionParaConducir == "Si"
                )

                // 1. Efecto UX: Retraso artificial para que el usuario sienta el guardado
                delay(1500)

                // 2. Guardar localmente (Offline-First)
                saveVehiculoInspectionUseCase(entity)
                Log.d("VehiculoVM", "✅ Inspección guardada localmente: $inspectionUuid")

                val placa = s.selectedVehicle!!.placa
                val km = s.kilometraje.toIntOrNull() ?: 0
                    // repository.saveInspectionLocally ya se hizo arriba
                    
                    // 2. ACTUALIZAR CACHE (Solo lo necesario)
                    // IMPORTANTE: NO sobreescribir las vigencias de los documentos con lo del formulario.
                    // Las vigencias en cache deben ser las de la BASE DE DATOS (backend).
                    // Al guardar offline, solo nos interesa actualizar el KILOMETRAJE en el catálogo.
                    
                    try {
                        // Actualizar el catálogo de vehículos con el km de la inspección guardada
                        repository.updateVehicleMileage(placa, km)
                        Log.d("VehiculoVM", "✅ Catálogo actualizado tras guardado offline: $placa -> $km")
                    } catch (e: Exception) {
                        Log.e("VehiculoVM", "Error actualizando catálogo tras guardado", e)
                    }

                // 2. Disparar sincronización inmediata si hay red
                triggerSync()

                // 3. Informar éxito al usuario
                _uiState.update { it.copy(isLoading = false, showSuccessDialog = true) }
                
            } catch (e: Exception) {
                Log.e("VehiculoVM", "❌ Excepción al guardar inspección", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error local: ${e.localizedMessage}") }
            }
        }
    }

    fun onSyncClicked() {
        triggerSync()
    }

    fun triggerSync() {
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(LocalSyncCoordinator.SyncTrigger.FormSaved("Vehiculo"))
        }
    }

    fun onSuccessDialogDismiss() {
        _uiState.update { it.copy(showSuccessDialog = false, saveCompleted = true) }
    }

    fun onErrorDismissed() { _uiState.update { it.copy(errorMessage = null) } }

    fun onNavigationDone() { _uiState.update { it.copy(saveCompleted = false) } }
}
