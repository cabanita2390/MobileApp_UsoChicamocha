package com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toVehiculoItem
// import com.example.testusoandroidstudio_1_usochicamocha.util.Constants // Removido por solicitud del usuario
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import androidx.work.*
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoInspectionEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.workers.SyncDataWorker
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Oil
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.VehiculoInspectionRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.GetLocalOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.SaveVehiculoInspectionUseCase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─── CONSTANTES DE UMBRAL DE KILOMETRAJE ────────────────────────────────────
/** Diferencia límite para el rango VERDE (hasta 200km es normal) */
private const val KM_YELLOW_THRESHOLD = 200
/** Diferencia mínima para mostrar alerta ROJA por exceso (incremento extremo, en km) */
private const val KM_RED_THRESHOLD = 1000

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
    
    val nivelAceite: String = "",
    val nivelRefrigerante: String = "",
    val nivelFrenos: String = "",
    val estadoLlantas: String = "",
    val lucesGeneral: String = "",
    val estadoVisual: String = "",
    val limpiezaGeneral: String = "",
    // Documentación — fechas del backend (referencia, solo lectura)
    val fechaVencSoatDB: String = "",
    val fechaVencTecnoDB: String = "",
    val fechaVencLicencioDB: String = "",
    val vigenciaExtintorDB: String = "",
    // Documentación — estado calculado automáticamente (hoy vs fecha BD)
    val estadoSoat: String = "",
    val estadoTecno: String = "",
    val estadoLicencia: String = "",
    val estadoExtintor: String = "",
    // Días restantes de cada documento (negativo = ya venció)
    val diasRestantesSoat: Long = 0,
    val diasRestantesTecno: Long = 0,
    val diasRestantesLicencia: Long = 0,
    val diasRestantesExtintor: Long = 0,
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
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    // AlertDialogs y lógica de doble "No" en campos de cierre
    val showConscienteAlert: Boolean = false,
    val conscienteNoCount: Int = 0,
    val showAprobadoAlert: Boolean = false,
    val aprobadoNoCount: Int = 0,
    val shouldExitForm: Boolean = false,
    // ─ Alerta de kilometraje ───────────────────────────────────────
    val showKmAlert: Boolean = false,        // Rojo: Menor al anterior OR exceso >=800
    val showKmYellowAlert: Boolean = false,  // Amarillo: Exceso >=300 y <800, o igual
    val kmAlertMessage: String = "",
    val kmEsInvalido: Boolean = false,    // true = bloquea el botón Guardar
    val kmYellowConfirmed: Boolean = false, // true = el usuario aceptó el aviso amarillo
    val kilometrajeMinimo: Int = 0,
    val kilometrajeDB: String = "",
    val kilometrajeError: String? = null, // Error inmediato debajo del campo
    val kmColorEstado: Int? = null,  // null=vacío/sin vehículo, 0=verde, 1=amarillo, 2=rojo
    // Cambio de aceite (opcional; se envía tras sincronizar la inspección)
    val vehicleOilBrands: List<Oil> = emptyList(),
    val registrarCambioAceite: Boolean = false,
    val oilType: String = "",          // "motor" o "hydraulic"
    val selectedOil: Oil? = null,
    val oilIntervalKm: String = "",
    val oilQuantity: String = "",
    val oilAirFilterChanged: Boolean = false,
)

// ─── VIEWMODEL ───────────────────────────────────────────────────────────────
@HiltViewModel
class VehiculoViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val repository: VehiculoInspectionRepository,
    private val saveVehiculoInspectionUseCase: SaveVehiculoInspectionUseCase,
    private val localSyncCoordinator: LocalSyncCoordinator,
    private val getLocalOilsUseCase: GetLocalOilsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoUiState())
    val uiState: StateFlow<VehiculoUiState> = _uiState.asStateFlow()

    init {
        observeVehiclesCatalog()
        loadUsername()
        observeVehicleOilBrands()
    }

    private fun observeVehicleOilBrands() {
        viewModelScope.launch {
            getLocalOilsUseCase().collect { list ->
                val forVehicle = list.filter { o ->
                    val t = o.type.trim()
                    t.equals("OIL_VEHICLE", ignoreCase = true) ||
                        t.contains("VEHICLE", ignoreCase = true) ||
                        t.equals("MOTOR", ignoreCase = true)
                }
                // Si no hay tipos explícitos de vehículo en BD, mostrar todo el catálogo (evita lista vacía).
                val shown = if (forVehicle.isNotEmpty()) forVehicle else list
                _uiState.update { it.copy(vehicleOilBrands = shown) }
            }
        }
    }

    // ── Carga el username del inspector logueado (campo de solo lectura) ──────
    private fun loadUsername() {
        viewModelScope.launch {
            val username = tokenManager.getUsername().first() ?: ""
            _uiState.update { it.copy(responsableInspeccion = username) }
        }
    }

    // ── Observa vehículos desde el repositorio (Room) — excluye motos ────────
    private fun observeVehiclesCatalog() {
        repository.getLocalVehiclesFlow()
            .onEach { entities ->
                val list = entities
                    .filter { !it.tipoVehiculo.equals("MOTOCICLETA", ignoreCase = true) }
                    .map { it.toVehiculoItem() }
                _uiState.update { it.copy(vehicles = list) }
            }
            .launchIn(viewModelScope)
    }

    // ── Carga y calcula estado de documentos al seleccionar vehículo ─────────
    private fun loadDocumentosVehiculo(idVehiculo: Int) {
        val placa = _uiState.value.selectedVehicle?.placa ?: return
        _uiState.update { it.copy(isLoadingDocs = true) }

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
                    val soatDB  = doc.fechaVencSoat     ?: ""
                    val tecnoDB = doc.fechaVencTecno    ?: ""
                    val licDB   = doc.fechaVencLicencia ?: ""
                    val extDB   = doc.fechaVencExtintor ?: ""

                    val (estSoat, diasSoat)   = calcularEstadoPorDias(soatDB)
                    val (estTecno, diasTecno) = calcularEstadoPorDias(tecnoDB)
                    val (estLic, diasLic)     = calcularEstadoPorDias(licDB)
                    val (estExt, diasExt)     = calcularEstadoPorDias(extDB)

                    _uiState.update { s ->
                        s.copy(
                            fechaVencSoatDB      = soatDB,
                            fechaVencTecnoDB     = tecnoDB,
                            fechaVencLicencioDB  = licDB,
                            vigenciaExtintorDB   = extDB,
                            estadoSoat           = estSoat,
                            estadoTecno          = estTecno,
                            estadoLicencia       = estLic,
                            estadoExtintor       = estExt,
                            diasRestantesSoat    = diasSoat,
                            diasRestantesTecno   = diasTecno,
                            diasRestantesLicencia = diasLic,
                            diasRestantesExtintor = diasExt,
                            urlImagenSoat        = doc.urlImagenSoat,
                            urlImagenTecno       = doc.urlImagenTecno,
                            urlImagenLicencia    = doc.urlImagenLicencia,
                            urlImagenExtintor    = doc.urlImagenExtintor,
                            isLoadingDocs        = false
                        )
                    }

                    // 3. ACTUALIZAR CACHE CON LO DEL API
                    try {
                        val docsToCache = listOf(
                            DocumentoVehiculoEntity(placa = placa, tipoDocumento = "SOAT",     vigencia = soatDB,  imagenUrl = doc.urlImagenSoat,     kilometrajeActual = 0),
                            DocumentoVehiculoEntity(placa = placa, tipoDocumento = "TECNO",    vigencia = tecnoDB, imagenUrl = doc.urlImagenTecno,    kilometrajeActual = 0),
                            DocumentoVehiculoEntity(placa = placa, tipoDocumento = "LICENCIA", vigencia = licDB,   imagenUrl = doc.urlImagenLicencia, kilometrajeActual = 0),
                            DocumentoVehiculoEntity(placa = placa, tipoDocumento = "EXTINTOR", vigencia = extDB,   imagenUrl = doc.urlImagenExtintor, kilometrajeActual = 0)
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
        val soat  = cached.find { it.tipoDocumento == "SOAT" }
        val tecno = cached.find { it.tipoDocumento == "TECNO" }
        val lic   = cached.find { it.tipoDocumento == "LICENCIA" }
        val ext   = cached.find { it.tipoDocumento == "EXTINTOR" }

        val (estSoat, diasSoat)   = calcularEstadoPorDias(soat?.vigencia ?: "")
        val (estTecno, diasTecno) = calcularEstadoPorDias(tecno?.vigencia ?: "")
        val (estLic, diasLic)     = calcularEstadoPorDias(lic?.vigencia ?: "")
        val (estExt, diasExt)     = calcularEstadoPorDias(ext?.vigencia ?: "")

        _uiState.update { s ->
            s.copy(
                fechaVencSoatDB      = soat?.vigencia  ?: "",
                fechaVencTecnoDB     = tecno?.vigencia ?: "",
                fechaVencLicencioDB  = lic?.vigencia   ?: "",
                vigenciaExtintorDB   = ext?.vigencia   ?: "",
                estadoSoat           = estSoat,
                estadoTecno          = estTecno,
                estadoLicencia       = estLic,
                estadoExtintor       = estExt,
                diasRestantesSoat    = diasSoat,
                diasRestantesTecno   = diasTecno,
                diasRestantesLicencia = diasLic,
                diasRestantesExtintor = diasExt,
                urlImagenSoat        = soat?.imagenUrl,
                urlImagenTecno       = tecno?.imagenUrl,
                urlImagenLicencia    = lic?.imagenUrl,
                urlImagenExtintor    = ext?.imagenUrl,
                isLoadingDocs        = false
            )
        }
    }



    // ── Previene que el diálogo se cierre accidentalmente ───────────────────
    fun onKmAlertDismiss() {
        _uiState.update { it.copy(
            showKmAlert = false,
            kmEsInvalido = false
        ) }
    }

    fun onCancelRedKmHighlight() {
        _uiState.update { it.copy(showKmAlert = false) }
    }

    // ── Datos generales ─────────────────────────────────────────────────────────────────
    fun setVehicles(list: List<VehiculoItem>) { _uiState.update { it.copy(vehicles = list) } }
    fun onKilometrajeChange(v: String) {
        // 1. Actualizar el valor y limpiar TODOS los estados de alerta anteriores
        _uiState.update { it.copy(
            kilometraje = v,
            kmYellowConfirmed = false,
            showKmAlert = false,        // Limpiar alerta roja previa
            showKmYellowAlert = false   // Limpiar alerta amarilla previa
        ) }
        
        // 2. Si el campo está vacío, limpiar errores inmediatamente
        if (v.isBlank()) {
            _uiState.update { it.copy(
                kmEsInvalido = false,
                kilometrajeError = null,
                kmAlertMessage = "",
                kmColorEstado = null
            ) }
            validateForm()
            return
        }

        // 3. Manejo de validación reactiva (ROJO: Menor al mínimo)
        val km = v.toIntOrNull() ?: 0
        val kmMin = _uiState.value.kilometrajeMinimo
        val diff = if (km > 0 && kmMin > 0) km - kmMin else Int.MIN_VALUE

        val nuevoColorEstado: Int? = when {
            km <= 0 -> null
            kmMin == 0 -> null
            diff < 0 -> 2
            diff in 0..KM_YELLOW_THRESHOLD -> 0
            diff in (KM_YELLOW_THRESHOLD + 1) until KM_RED_THRESHOLD -> 1
            else -> 2
        }

        // Solo actualizamos kmEsInvalido silenciosamente para deshabilitar el botón "Guardar"
        val isLowerThanMin = km > 0 && kmMin > 0 && km < kmMin
        _uiState.update { it.copy(
            kmEsInvalido = isLowerThanMin,
            kilometrajeError = null,
            kmColorEstado = nuevoColorEstado
        ) }
        validateForm()
    }

    /**
     * Se activa cuando el campo de kilometraje pierde el foco (onBlur).
     * - diff < 0           → Alerta ROJA  (menor al registrado)
     * - diff == 0          → Alerta AMARILLA (igual al registrado)
     * - 300 <= diff < 800  → Alerta AMARILLA (incremento inusual)
     * - diff >= 800        → Alerta ROJA  (incremento excesivo)
     * - 0 < diff < 300     → Sin alerta (rango normal, campo verde)
     */
    fun onKilometrajeBlur() {
        val km = _uiState.value.kilometraje.toIntOrNull() ?: return
        val kmMin = _uiState.value.kilometrajeMinimo
        val diff = km - kmMin
        if (km <= 0) return

        when {
            // ROJO: menor al último registrado
            diff < 0 -> _uiState.update { it.copy(
                showKmAlert = true,
                kmAlertMessage = "El kilometraje ingresado es menor al último kilometraje registrado. Por favor, verifíquelo."
            )}
            // ROJO: exceso extremo >=800 km
            diff >= KM_RED_THRESHOLD -> _uiState.update { it.copy(
                showKmAlert = true,
                kmAlertMessage = "El incremento de kilometraje es muy elevado. Verifique que el valor sea correcto."
            )}
            // AMARILLO: incremento superior a 200km y menor a 1000km
            diff > KM_YELLOW_THRESHOLD -> {
                _uiState.update { it.copy(
                    showKmYellowAlert = true,
                    kmYellowConfirmed = false,
                    kmAlertMessage = "Detectamos un incremento inusual en el kilometraje. ¿Está seguro de que es correcto?"
                )}
            }
            // VERDE: 0 <= diff <= 200 → sin alerta, rango normal
        }
    }

    fun onConfirmKmException() {
        _uiState.update { it.copy(showKmYellowAlert = false, kmYellowConfirmed = true) }
        validateForm()
    }

    fun onCancelKmHighlight() {
        _uiState.update { it.copy(showKmYellowAlert = false, kmYellowConfirmed = false) }
    }

    fun onKmYellowAlertDismiss() {
        _uiState.update { it.copy(showKmYellowAlert = false) }
    }

    fun onVehicleSelected(v: VehiculoItem) {
        _uiState.update { it.copy(
            selectedVehicle = v,
            kilometrajeMinimo = v.kilometrajeActual,
            kilometrajeDB = v.kilometrajeActual.toString()
        ) }
        loadDocumentosVehiculo(v.idVehiculo)
        val kmStr = _uiState.value.kilometraje
        val km = kmStr.toIntOrNull()
        if (km != null) {
            onKilometrajeChange(kmStr) 
        }
        validateForm()
    }


    // ── Documentación — confirmación manual del inspector ────────────────────
    fun onDocEstadoChange(doc: String, estado: String) {
        _uiState.update { s ->
            when (doc) {
                "Soat"     -> s.copy(estadoSoat = estado)
                "Tecno"    -> s.copy(estadoTecno = estado)
                "Licencia" -> s.copy(estadoLicencia = estado)
                "Extintor" -> s.copy(estadoExtintor = estado)
                else       -> s
            }
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

    /**
     * Calcula el estado de vigencia basado en la fecha del backend.
     * @SuppressLint("NewApi") se usa porque hemos habilitado Java 8+ Desugaring.
     */
    @SuppressLint("NewApi")
    private fun calcularEstadoPorDias(fechaDB: String): Pair<String, Long> {
        if (fechaDB.isBlank()) return Pair("", 0L)
        return try {
            val hoy = LocalDate.now()
            // Normalizar formato: si es YYYY-MM lo convertimos a YYYY-MM-01
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
            Log.d("VencLogic", "fechaDB=$fechaDB → venc=$fechaVenc, hoy=$hoy, dias=$dias")
            val estado = when {
                dias < 0  -> "Vencido"
                dias <= 30 -> "Próximo a Vencer"
                else       -> "Vigente"
            }
            Pair(estado, dias)
        } catch (e: Exception) {
            Log.e("VencLogic", "Error parseando fecha: $fechaDB", e)
            Pair("", 0L)
        }
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


    fun onObservacionesChange(v: String) { 
        _uiState.update { it.copy(observaciones = v) }
        validateForm()
    }

    private fun validateForm() {
        val s = _uiState.value
        val oilOk = !s.registrarCambioAceite || (
            s.oilType.isNotBlank() &&
                s.selectedOil != null &&
                s.oilIntervalKm.toIntOrNull() != null &&
                (s.oilIntervalKm.toIntOrNull() ?: 0) > 0
            )
        val valid = s.selectedVehicle != null && s.kilometraje.isNotBlank() &&
                s.nivelAceite.isNotBlank() && s.nivelRefrigerante.isNotBlank() &&
                s.nivelFrenos.isNotBlank() && s.estadoLlantas.isNotBlank() &&
                s.lucesGeneral.isNotBlank() && s.estadoVisual.isNotBlank() &&
                s.limpiezaGeneral.isNotBlank() &&
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
                s.responsableInspeccion.isNotBlank() &&
                s.kilometraje.isNotBlank() &&
                oilOk
        _uiState.update { it.copy(isSaveButtonEnabled = valid) }
    }

    fun onRegistrarCambioAceiteChange(v: Boolean) {
        _uiState.update { it.copy(registrarCambioAceite = v, oilType = if (v) "motor" else "") }
        validateForm()
    }

    fun onOilTypeChange(v: String) {
        _uiState.update { it.copy(oilType = v, selectedOil = null) }
        validateForm()
    }

    fun onOilSelected(oil: Oil?) {
        _uiState.update { it.copy(selectedOil = oil) }
        validateForm()
    }

    fun onOilIntervalKmChange(v: String) {
        _uiState.update { it.copy(oilIntervalKm = v.filter { ch -> ch.isDigit() }) }
        validateForm()
    }

    fun onOilQuantityChange(v: String) {
        _uiState.update { it.copy(oilQuantity = v) }
    }

    fun onOilAirFilterChanged(v: Boolean) {
        _uiState.update { it.copy(oilAirFilterChanged = v) }
    }

    fun onSaveClick() {
        if (!_uiState.value.isSaveButtonEnabled) return
        val s = _uiState.value
        val km = s.kilometraje.toIntOrNull() ?: 0
        val kmMin = s.kilometrajeMinimo
        val diff = km - kmMin

        // Alerta ROJA: km menor al registrado (BLOQUEANTE)
        if (km > 0 && diff < 0) {
            _uiState.update { it.copy(
                showKmAlert = true,
                kmAlertMessage = "El kilometraje ingresado es menor al último kilometraje registrado. Por favor, verifíquelo."
            ) }
            return
        }

        // Alerta ROJA: exceso extremo >=1000 — BLOQUEANTE
        if (km > 0 && diff >= KM_RED_THRESHOLD) {
            _uiState.update { it.copy(
                showKmAlert = true,
                kmAlertMessage = "El incremento de kilometraje es muy elevado (+$diff km respecto al último registrado: $kmMin km). Verifique que el valor sea correcto."
            ) }
            return
        }

        // Alerta AMARILLA: incremento inusual > 200 — no bloquea si ya confirmó
        if (km > 0 && !s.kmYellowConfirmed && diff in (KM_YELLOW_THRESHOLD + 1) until KM_RED_THRESHOLD) {
            _uiState.update { it.copy(
                showKmYellowAlert = true,
                kmAlertMessage = "Detectamos un incremento inusual (+$diff km respecto al último registrado: $kmMin km). ¿Está seguro de que es correcto?"
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val inspectionUuid = UUID.randomUUID().toString()
                
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
                    vigenciaExtintor       = s.vigenciaExtintorDB,
                    fechaVencSoat          = s.fechaVencSoatDB,
                    fechaVencTecno         = s.fechaVencTecnoDB,
                    fechaVencLicencia      = s.fechaVencLicencioDB,
                    urlImagenSoat          = s.urlImagenSoat.orEmpty(),
                    urlImagenTecno         = s.urlImagenTecno.orEmpty(),
                    urlImagenLicencia      = s.urlImagenLicencia.orEmpty(),
                    urlImagenExtintor      = s.urlImagenExtintor.orEmpty(),
                    registrarCambioAceite  = s.registrarCambioAceite,
                    oilType                = s.oilType.trim(),
                    oilBrandId             = s.selectedOil?.id?.toLong(),
                    oilIntervalKm          = s.oilIntervalKm.toIntOrNull(),
                    oilQuantity            = s.oilQuantity.toDoubleOrNull(),
                    oilAirFilterChanged    = s.oilAirFilterChanged,
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

                delay(1500)
                saveVehiculoInspectionUseCase(entity)
                
                val placa = s.selectedVehicle!!.placa
                val kmVal = s.kilometraje.toIntOrNull() ?: 0
                    
                try {
                    repository.updateVehicleMileage(placa, kmVal)
                } catch (e: Exception) {
                    Log.e("VehiculoVM", "Error actualizando catálogo tras guardado", e)
                }

                triggerSync()
                _uiState.update { it.copy(isLoading = false, showSuccessDialog = true) }
                
            } catch (e: Exception) {
                Log.e("VehiculoVM", "❌ Excepción al guardar inspección", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error local: ${e.localizedMessage}") }
            }
        }
    }

    fun onSyncClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncMessage = "Actualizando datos del vehículo...") }
            
            // Realizar las sincronizaciones
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_CATALOG)
            )
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.VEHICLES_DOCUMENTS)
            )

            // Feedback final
            _uiState.update { it.copy(isSyncing = false, syncMessage = "¡Datos actualizados con éxito!") }
        }
    }

    fun onSyncMessageShown() { _uiState.update { it.copy(syncMessage = null) } }

    private fun triggerSync() {
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