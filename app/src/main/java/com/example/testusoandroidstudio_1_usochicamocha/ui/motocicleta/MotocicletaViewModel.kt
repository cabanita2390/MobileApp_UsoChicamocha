package com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.InspeccionMotoRequest
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ubicacion
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.InspeccionMotoPendiente
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalUbicacionesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.SyncMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SaveInspeccionMotoLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.inspeccionmoto.SyncInspeccionMotoUseCase
import com.example.testusoandroidstudio_1_usochicamocha.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

/** Estado de un documento (SOAT / Revisión / Licencia) */
data class DocumentoState(
    val vigencia: String = "",           // "YYYY-MM" or "YYYY-MM-DD"
    val imagenUri: Uri? = null,
    val remoteUrl: String? = null,       // URL from server for pre-existing documents
    val yaRegistrado: Boolean = false,   // true → pre-filled desde BD
    val isNearExpiry: Boolean = false,   // true if within 1 month
    val estadoDoc: String = ""           // "Vigente" | "Próximo a vencer" | "Vencido"
)

data class MotocicletaUiState(
    // Dropdown data (ahora usa modelos de dominio desde Room)
    val motocicletas: List<Moto> = emptyList(),
    val ubicaciones: List<Ubicacion> = emptyList(),
    val isLoadingData: Boolean = false,

    // Selections
    val selectedMoto: Moto? = null,
    val selectedUbicacion: Ubicacion? = null,
    val kilometraje: String = "",

    // Documentos
    val soat: DocumentoState = DocumentoState(),
    val revisionTecno: DocumentoState = DocumentoState(),
    val licencia: DocumentoState = DocumentoState(),
    val isLoadingDocumentos: Boolean = false,

    // Estado y observaciones
    val estadoGeneral: String = "",
    val observaciones: String = "",
    val responsable: String = "",

    // UI helpers
    val isSaveButtonEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MotocicletaViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
    private val apiService: ApiService,
    private val getLocalMotosUseCase: GetLocalMotosUseCase,
    private val getLocalUbicacionesUseCase: GetLocalUbicacionesUseCase,
    private val syncMotosUseCase: SyncMotosUseCase,
    private val saveInspeccionMotoLocalUseCase: SaveInspeccionMotoLocalUseCase,
    private val syncInspeccionMotoUseCase: SyncInspeccionMotoUseCase
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
    }

    /** Lee las motos desde Room (caché local). Se actualiza reactivamente cuando el sync las refresca. */
    private fun loadMotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingData = true) }
            getLocalMotosUseCase().collect { motos ->
                _uiState.update { it.copy(motocicletas = motos, isLoadingData = false) }
            }
        }
    }

    /** Lee las ubicaciones desde Room (caché local). Se actualiza reactivamente cuando el sync las refresca. */
    private fun loadUbicaciones() {
        viewModelScope.launch {
            getLocalUbicacionesUseCase().collect { ubicaciones ->
                _uiState.update { it.copy(ubicaciones = ubicaciones) }
            }
        }
    }

    fun onMotoSelected(moto: Moto) {
        _uiState.update { it.copy(selectedMoto = moto) }
        loadDocumentosForMoto(moto.placa)
        validate()
    }

    private fun loadDocumentosForMoto(placa: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDocumentos = true) }
            try {
                val resp = apiService.getDocumentosByPlaca(placa)
                val docs = resp.body() ?: emptyList()
                var soat = _uiState.value.soat
                var revision = _uiState.value.revisionTecno
                var licencia = _uiState.value.licencia

                docs.forEach { doc ->
                    val vigencia = doc.fechaVencimiento ?: doc.mesyear ?: ""
                    val estadoCalculado = calcularEstado(vigencia)

                    val estado = DocumentoState(
                        vigencia = vigencia,
                        yaRegistrado = vigencia.isNotBlank(),
                        isNearExpiry = estadoCalculado == "Próximo a vencer",
                        estadoDoc = estadoCalculado,
                        remoteUrl = doc.imagenUrl
                    )
                    when (doc.tipoDocumento) {
                        "SOAT" -> soat = estado
                        "REVISION_TECNO" -> revision = estado
                        "LICENCIA" -> licencia = estado
                    }
                }

                _uiState.update {
                    it.copy(
                        soat = soat,
                        revisionTecno = revision,
                        licencia = licencia,
                        isLoadingDocumentos = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingDocumentos = false) }
            }
            validate()
        }
    }

    fun onUbicacionSelected(ubicacion: Ubicacion) {
        _uiState.update { it.copy(selectedUbicacion = ubicacion) }
        validate()
    }

    fun registrarNuevaPlaca(placa: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                val response = apiService.registrarPlaca(placa)
                if (response.isSuccessful) {
                    // Sincronizamos motos desde el servidor para que la nueva placa
                    // aparezca de inmediato en el dropdown (Room Flow actualiza la UI reactivamente)
                    syncMotosUseCase()
                } else {
                    _uiState.update { it.copy(errorMessage = "Error al registrar placa: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de conexión al registrar placa") }
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onKilometrajeChange(value: String) {
        _uiState.update { it.copy(kilometraje = value) }
        validate()
    }

    // --- SOAT ---
    fun onSoatVigenciaChange(year: Int, month: Int, day: Int) {
        val dateStr = if (day == -1) String.format("%04d-%02d", year, month + 1)
                     else String.format("%04d-%02d-%02d", year, month + 1, day)
        val nuevoEstado = calcularEstado(dateStr)
        _uiState.update { it.copy(soat = it.soat.copy(vigencia = dateStr, estadoDoc = nuevoEstado)) }
        validate()
    }
    fun onSoatImagenSelected(uri: Uri) = setDocumentoImagen(uri, "SOAT")
    fun onSoatImagenRemoved() { _uiState.update { it.copy(soat = it.soat.copy(imagenUri = null)) } }

    // --- Revisión Técnico-Mecánica ---
    fun onRevisionVigenciaChange(year: Int, month: Int, day: Int) {
        val dateStr = if (day == -1) String.format("%04d-%02d", year, month + 1)
                     else String.format("%04d-%02d-%02d", year, month + 1, day)
        val nuevoEstado = calcularEstado(dateStr)
        _uiState.update { it.copy(revisionTecno = it.revisionTecno.copy(vigencia = dateStr, estadoDoc = nuevoEstado)) }
        validate()
    }
    fun onRevisionImagenSelected(uri: Uri) = setDocumentoImagen(uri, "REVISION_TECNO")
    fun onRevisionImagenRemoved() { _uiState.update { it.copy(revisionTecno = it.revisionTecno.copy(imagenUri = null)) } }

    // --- Licencia ---
    fun onLicenciaVigenciaChange(year: Int, month: Int, day: Int) {
        val dateStr = if (day == -1) String.format("%04d-%02d", year, month + 1)
                     else String.format("%04d-%02d-%02d", year, month + 1, day)
        val nuevoEstado = calcularEstado(dateStr)
        _uiState.update { it.copy(licencia = it.licencia.copy(vigencia = dateStr, estadoDoc = nuevoEstado)) }
        validate()
    }
    fun onLicenciaImagenSelected(uri: Uri) = setDocumentoImagen(uri, "LICENCIA")
    fun onLicenciaImagenRemoved() { _uiState.update { it.copy(licencia = it.licencia.copy(imagenUri = null)) } }

    private fun setDocumentoImagen(uri: Uri, tipo: String) {
        viewModelScope.launch {
            val compressed = ImageUtils.compressAndSaveImage(context, uri) ?: uri
            _uiState.update { state ->
                when (tipo) {
                    "SOAT" -> state.copy(soat = state.soat.copy(imagenUri = compressed))
                    "REVISION_TECNO" -> state.copy(revisionTecno = state.revisionTecno.copy(imagenUri = compressed))
                    else -> state.copy(licencia = state.licencia.copy(imagenUri = compressed))
                }
            }
        }
    }

    fun onEstadoGeneralChange(value: String) {
        _uiState.update { it.copy(estadoGeneral = value) }
        validate()
    }

    fun onObservacionesChange(value: String) {
        _uiState.update { it.copy(observaciones = value) }
        validate()
    }

    private fun calcularEstado(vigencia: String): String {
        if (vigencia.isBlank()) return "Vencido"
        try {
            val parts = vigencia.split("-")
            if (parts.size < 2) return "Vencido"

            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = if (parts.size == 3) parts[2].toInt() else 1

            val targetCal = Calendar.getInstance()
            targetCal.set(year, month, day, 23, 59, 59)

            val hoy = Calendar.getInstance()
            val unMesDespues = Calendar.getInstance()
            unMesDespues.add(Calendar.MONTH, 1)

            return when {
                targetCal.before(hoy) -> "Vencido"
                !targetCal.after(unMesDespues) -> "Próximo a vencer"
                else -> "Vigente"
            }
        } catch (e: Exception) {
            return "Vencido"
        }
    }

    private fun validate() {
        val s = _uiState.value
        val soatOk = s.soat.vigencia.isNotBlank() && s.soat.estadoDoc.isNotBlank()
        val tecnoOk = s.revisionTecno.vigencia.isNotBlank() && s.revisionTecno.estadoDoc.isNotBlank()
        val licenciaOk = s.licencia.vigencia.isNotBlank() && s.licencia.estadoDoc.isNotBlank()

        val valid = s.selectedMoto != null &&
                s.selectedUbicacion != null &&
                s.kilometraje.isNotBlank() &&
                soatOk && tecnoOk && licenciaOk &&
                s.estadoGeneral.isNotBlank() &&
                s.observaciones.isNotBlank()
        _uiState.update { it.copy(isSaveButtonEnabled = valid) }
    }

    fun onSaveClick() {
        if (!_uiState.value.isSaveButtonEnabled || _uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val s = _uiState.value

                // 1. Construir el modelo de inspección pendiente con UUID único
                val inspeccion = InspeccionMotoPendiente(
                    uuid = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    idVehiculo = s.selectedMoto!!.id,
                    idUbicacion = s.selectedUbicacion!!.id,
                    kilometrajeReportado = s.kilometraje.toIntOrNull() ?: 0,
                    estadoGeneral = s.estadoGeneral,
                    observacionesFinales = s.observaciones,
                    vigenciaSoat = s.soat.vigencia.takeIf { it.isNotBlank() },
                    estadoSoat = s.soat.estadoDoc.takeIf { it.isNotBlank() },
                    vigenciaRevision = s.revisionTecno.vigencia.takeIf { it.isNotBlank() },
                    estadoRevision = s.revisionTecno.estadoDoc.takeIf { it.isNotBlank() },
                    vigenciaLicencia = s.licencia.vigencia.takeIf { it.isNotBlank() },
                    estadoLicencia = s.licencia.estadoDoc.takeIf { it.isNotBlank() },
                    imagenSoat = s.soat.imagenUri?.toString(),
                    imagenRevision = s.revisionTecno.imagenUri?.toString(),
                    imagenLicencia = s.licencia.imagenUri?.toString()
                )

                // 2. Guardar en Room SIEMPRE (funciona sin internet)
                saveInspeccionMotoLocalUseCase(inspeccion)

                // 3. Notificar éxito al usuario de inmediato — el dato ya está guardado localmente
                _uiState.update { it.copy(isSaving = false, saveCompleted = true) }

                // 4. Intentar sincronizar con el servidor en segundo plano
                //    Si falla (sin internet), el SyncDataWorker lo reintentará automáticamente
                launch {
                    syncInspeccionMotoUseCase(inspeccion)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Error al guardar: ${e.localizedMessage}") }
            }
        }
    }

    fun onNavigationDone() { _uiState.update { it.copy(saveCompleted = false) } }
    fun onErrorDismissed() { _uiState.update { it.copy(errorMessage = null) } }
}
