package com.example.testusoandroidstudio_1_usochicamocha.ui.form

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// AÑADIDO: Importaciones necesarias para WorkManager
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
// AÑADIDO: Importa tus workers
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SaveFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.GetLocalMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.ImageUtils
import com.example.testusoandroidstudio_1_usochicamocha.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

// ... (El data class FormUiState no cambia)
data class FormUiState(
    val horometro: String = "",
    val observaciones: String = "",
    val estadoFugas: String = "",
    val estadoFrenos: String = "",
    val estadoCorreasPoleas: String = "",
    val estadoLlantasCarriles: String = "",
    val estadoEncendido: String = "",
    val estadoElectrico: String = "",
    val estadoMecanico: String = "",
    val estadoTemperatura: String = "",
    val estadoAceite: String = "",
    val estadoHidraulico: String = "",
    val estadoRefrigerante: String = "",
    val estadoEstructural: String = "",
    val vigenciaExtintor: String = "",
    val previewingImageUri: Uri? = null,
    val selectedImageUris: List<Uri> = emptyList(),
    val machines: List<Machine> = emptyList(),
    val selectedMachine: Machine? = null,
    val saveCompleted: Boolean = false,
    val greasingStatus: String = "",
    val greasingAction: String = "",
    val greasingObservations: String = "",
    val isSaveButtonEnabled: Boolean = false,
    // Bloqueo de inspección por documentos vencidos / próximos a vencer
    val documentosVencidos: List<String> = emptyList(),
    val documentosPorVencer: List<String> = emptyList(),
    val showDocumentoVencidoDialog: Boolean = false,
    val showDocumentoPorVencerDialog: Boolean = false
)

/** Días antes del vencimiento para marcar "Próximo a Vencer" — alineado con el umbral
 * AMARILLO del backend (DocumentAlertCalculator.java) para no desincronizar la app. */
private const val UMBRAL_DIAS_POR_VENCER = 45


@HiltViewModel
class FormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saveFormUseCase: SaveFormUseCase,
    private val getLocalMachinesUseCase: GetLocalMachinesUseCase,
    private val tokenManager: TokenManager,
    private val localSyncCoordinator: LocalSyncCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState = _uiState.asStateFlow()

    // ... (init y el resto de funciones no cambian)
    init {
        viewModelScope.launch {
            getLocalMachinesUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(machines = resource.data ?: emptyList()) }
                    }
                    is Resource.Error -> {
                        Log.e("FormViewModel", "Error loading machines: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        // Opcional: manejar estado de carga
                    }
                }
            }
        }
        if (_uiState.value.vigenciaExtintor.isEmpty()) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            onExtinguisherDateChange(year, month)
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val isFormValid = state.selectedMachine != null &&
                state.horometro.isNotBlank() &&
                state.estadoFugas.isNotBlank() &&
                state.estadoFrenos.isNotBlank() &&
                state.estadoCorreasPoleas.isNotBlank() &&
                state.estadoLlantasCarriles.isNotBlank() &&
                state.estadoEncendido.isNotBlank() &&
                state.estadoElectrico.isNotBlank() &&
                state.estadoMecanico.isNotBlank() &&
                state.estadoTemperatura.isNotBlank() &&
                state.estadoAceite.isNotBlank() &&
                state.estadoHidraulico.isNotBlank() &&
                state.estadoRefrigerante.isNotBlank() &&
                state.estadoEstructural.isNotBlank() &&
                state.observaciones.isNotBlank() &&
                state.greasingStatus.isNotBlank() &&
                (state.greasingStatus == "No" || (state.greasingStatus == "Sí" && state.greasingAction.isNotBlank())) &&
                state.vigenciaExtintor.isNotBlank() && // AÑADIDO: Validar que la fecha del extintor esté seleccionada
                state.documentosVencidos.isEmpty()

        _uiState.update { it.copy(isSaveButtonEnabled = isFormValid) }
    }

    fun onMachineSelected(machine: Machine) {
        _uiState.update { it.copy(selectedMachine = machine) }
        evaluarBloqueoDocumentos(machine)
        validateForm()
    }

    /** Calcula estado SOAT / Seguro Todo Riesgo de la máquina seleccionada y decide si
     * la inspección debe bloquearse (documento vencido) o solo advertirse (próximo a vencer). */
    private fun evaluarBloqueoDocumentos(machine: Machine) {
        val (estSoat, _) = calcularEstadoPorDias(machine.soatExpirationDate)
        val (estSeguro, _) = calcularEstadoPorDias(machine.runtExpirationDate)

        val vencidos = buildList {
            if (estSoat == "Vencido") add("SOAT de la máquina")
            if (estSeguro == "Vencido") add("Seguro Todo Riesgo de la máquina")
        }
        val porVencer = buildList {
            if (estSoat == "Próximo a Vencer") add("SOAT de la máquina")
            if (estSeguro == "Próximo a Vencer") add("Seguro Todo Riesgo de la máquina")
        }
        _uiState.update {
            it.copy(
                documentosVencidos = vencidos,
                documentosPorVencer = porVencer,
                showDocumentoVencidoDialog = vencidos.isNotEmpty(),
                showDocumentoPorVencerDialog = vencidos.isEmpty() && porVencer.isNotEmpty()
            )
        }
    }

    fun onDocumentoVencidoDialogDismiss() {
        _uiState.update { it.copy(showDocumentoVencidoDialog = false) }
    }

    fun onDocumentoPorVencerDialogDismiss() {
        _uiState.update { it.copy(showDocumentoPorVencerDialog = false) }
    }

    /** Calcula el estado de vigencia basado en la fecha del backend (mismo criterio
     * usado en VehiculoViewModel/MotocicletaViewModel para vehículos y motos). */
    @SuppressLint("NewApi")
    private fun calcularEstadoPorDias(fechaDB: String?): Pair<String, Long> {
        if (fechaDB.isNullOrBlank()) return Pair("", 0L)
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
            val estado = when {
                dias < 0 -> "Vencido"
                dias <= UMBRAL_DIAS_POR_VENCER -> "Próximo a Vencer"
                else -> "Vigente"
            }
            Pair(estado, dias)
        } catch (e: Exception) {
            Log.e("VencLogic", "Error parseando fecha máquina: $fechaDB", e)
            Pair("", 0L)
        }
    }

    fun onHorometroChange(value: String) {
        _uiState.update { it.copy(horometro = value) }
        validateForm()
    }

    fun onObservacionesChange(value: String) {
        _uiState.update { it.copy(observaciones = value) }
        validateForm()
    }

    fun onGreasedStatusChange(status: String) {
        _uiState.update {
            if (status == "No") {
                it.copy(greasingStatus = status, greasingAction = "", greasingObservations = "")
            } else {
                it.copy(greasingStatus = status)
            }
        }
        validateForm()
    }

    fun onGreasingActionChange(action: String) {
        if (_uiState.value.greasingStatus == "Sí") {
            _uiState.update { it.copy(greasingAction = action) }
            validateForm()
        }
    }

    fun onGreasingObservationsChange(observations: String) {
        if (_uiState.value.greasingStatus == "Sí") {
            _uiState.update { it.copy(greasingObservations = observations) }
            validateForm()
        }
    }

    fun onExtinguisherDateChange(year: Int, month: Int) {
        val formattedMonth = String.format("%02d", month + 1)
        _uiState.update { it.copy(vigenciaExtintor = "$year-$formattedMonth") }
        validateForm()
    }

    /**
     * CORREGIDO: Ahora usamos la utilidad de compresión de forma estática,
     * pasándole el 'context' que el ViewModel tiene disponible.
     */
    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val compressedUri = ImageUtils.compressAndSaveImage(context, uri)
            if (compressedUri != null) {
                _uiState.update { it.copy(selectedImageUris = it.selectedImageUris + compressedUri) }
            } else {
                // Opcional: Manejar el error de compresión, e.g., mostrar un Toast
            }
        }
    }

    fun onImageRemoved(uri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedImageUris = currentState.selectedImageUris.filter { it != uri },
                previewingImageUri = if (currentState.previewingImageUri == uri) null else currentState.previewingImageUri
            )
        }
    }
    fun onPreviewImageClicked(uri: Uri) {
        _uiState.update { it.copy(previewingImageUri = uri) }
    }

    fun onDismissPreview() {
        _uiState.update { it.copy(previewingImageUri = null) }
    }

    fun onEstadoChange(fieldName: String, value: String) {
        _uiState.update { currentState ->
            when (fieldName) {
                "Fugas" -> currentState.copy(estadoFugas = value)
                "Frenos" -> currentState.copy(estadoFrenos = value)
                "CorreasPoleas" -> currentState.copy(estadoCorreasPoleas = value)
                "LlantasCarriles" -> currentState.copy(estadoLlantasCarriles = value)
                "Encendido" -> currentState.copy(estadoEncendido = value)
                "Electrico" -> currentState.copy(estadoElectrico = value)
                "Mecanico" -> currentState.copy(estadoMecanico = value)
                "Temperatura" -> currentState.copy(estadoTemperatura = value)
                "Aceite" -> currentState.copy(estadoAceite = value)
                "Hidraulico" -> currentState.copy(estadoHidraulico = value)
                "Refrigerante" -> currentState.copy(estadoRefrigerante = value)
                "Estructural" -> currentState.copy(estadoEstructural = value)
                else -> currentState
            }
        }
        validateForm()
    }


    fun onSaveClick() {
        if (!_uiState.value.isSaveButtonEnabled) return

        val selectedMachineId = _uiState.value.selectedMachine?.id?.toLong() ?: return

        viewModelScope.launch {
            val currentState = _uiState.value
            val currentUserId = tokenManager.getUserId().firstOrNull()?.toLong()

            if (currentUserId == null) {
                return@launch
            }

            val finalGreasingAction = when (currentState.greasingStatus) {
                "Sí" -> currentState.greasingAction
                "No" -> "No se engrasó"
                else -> ""
            }
            val timestamp = System.currentTimeMillis()

            val form = Form(
                localId = 0,
                serverId = null,
                UUID = UUID.randomUUID().toString(),
                timestamp = timestamp,
                machineId = selectedMachineId,
                userId = currentUserId,
                hourmeter = currentState.horometro,
                observations = currentState.observaciones,
                leakStatus = currentState.estadoFugas,
                brakeStatus = currentState.estadoFrenos,
                beltsPulleysStatus = currentState.estadoCorreasPoleas,
                tireLanesStatus = currentState.estadoLlantasCarriles,
                carIgnitionStatus = currentState.estadoEncendido,
                electricalStatus = currentState.estadoElectrico,
                mechanicalStatus = currentState.estadoMecanico,
                temperatureStatus = currentState.estadoTemperatura,
                oilStatus = currentState.estadoAceite,
                hydraulicStatus = currentState.estadoHidraulico,
                coolantStatus = currentState.estadoRefrigerante,
                structuralStatus = currentState.estadoEstructural,
                expirationDateFireExtinguisher = currentState.vigenciaExtintor,
                greasingAction = finalGreasingAction,
                greasingObservations = if (currentState.greasingStatus == "Sí") currentState.greasingObservations else "",
                isUnexpected = false,
                isSynced = false,
                isSyncing = false
            )
            val imageUrisAsString = currentState.selectedImageUris.map { it.toString() }

            // 1. Guardar en la base de datos local
            saveFormUseCase(form, imageUrisAsString)

            // 2. Usar el coordinador para iniciar la sincronización
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.FormSaved("Inspection")
            )

            // 3. Actualizar la UI para indicar que se completó
            _uiState.update { it.copy(saveCompleted = true) }
        }
    }

    // La función triggerImmediateSync ya no es necesaria porque usamos el coordinador
    // pero si se mantiene por compatibilidad, debería estar vacía o delegar.
    // La eliminamos para limpiar.

    fun onNavigationDone() {
        _uiState.update { it.copy(saveCompleted = false) }
    }
}
