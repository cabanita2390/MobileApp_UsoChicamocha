package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.captura

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ActividadCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EstacionCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionEdicion
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.EditarEjecucionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.GuardarEjecucionLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerActividadesCacheUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerDetalleEjecucionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEstacionesCacheUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerPendientesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.EstadoCita
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.estadoDeCita
import com.example.testusoandroidstudio_1_usochicamocha.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.min

/**
 * Estado del wizard de captura de una visita de mantenimiento civil (4 pasos:
 * Contexto/Actividad/Resultado/Evidencia). Un solo StateFlow con todos los campos
 * del formulario, mismo patrón que FormViewModel.FormUiState — no hay librería de
 * paginación, el paso actual solo condiciona qué sub-sección se muestra.
 *
 * Modo edición (editar una ejecución ya sincronizada, con motivoEdicion e historial)
 * queda fuera de este ViewModel a propósito — ver M6 en el plan de implementación.
 */
data class CapturaUiState(
    val currentStep: Int = 1,
    val programacionId: Long? = null,

    // Modo edición (M6) — corrige una ejecución ya sincronizada, requiere red y un
    // motivo de al menos 15 caracteres. Estación/disciplina/programacionId no se tocan.
    val editandoId: Long? = null,
    val cargandoEdicion: Boolean = false,
    val motivoEdicion: String = "",
    val errorEdicion: String? = null,

    // Paso 1 — Contexto
    val fecha: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val estacionId: Long? = null,
    val estacionNombre: String = "",
    val responsableNombre: String = "",
    val mesEjecucion: Int = LocalDate.now().monthValue,
    val semanaEjecucion: Int = semanaDeDia(LocalDate.now().dayOfMonth),
    val periodoAjustadoManualmente: Boolean = false,

    // Selector de disciplina (mockup de Contexto). El MVP solo captura CIVIL — el
    // catálogo de estaciones/actividades que ya se carga sigue siendo siempre el de
    // Civil, esto es solo el control visual. Elegir ELECTRICO/ELECTROMECANICO no
    // cambia este valor (ver `onDisciplinaSeleccionada`): dispara un aviso de "en
    // desarrollo" y el estado real se queda en CIVIL.
    val disciplinaSeleccionada: String = "CIVIL",
    val mostrarAlertaDisciplinaEnDesarrollo: Boolean = false,
    /** Qué disciplina no disponible tocó el técnico — para mostrarla por nombre en el diálogo. */
    val disciplinaSeleccionadaPendiente: String? = null,

    // Paso 2 — Actividad
    val tipoActividad: String = "",
    val tipoMantenimiento: String = "",
    val modoLibre: Boolean = false,
    val actividadId: Long? = null,
    val actividadNombre: String? = null,
    val descripcionLibre: String = "",

    // Registro libre: si la estación elegida tiene una cita pendiente/vencida del
    // cronograma, se sugiere acá en vez de dejar que el técnico cree un registro
    // huérfano que no resuelve nada en Cronograma/Pendientes.
    val citaSugerida: CitaProgramada? = null,
    val citaSugeridaEstado: EstadoCita? = null,

    // Paso 3 — Resultado
    val resultado: String = "",
    val observaciones: String = "",

    // Paso 4 — Evidencia
    val fotos: List<Uri> = emptyList(),

    // Catálogos (cache offline)
    val estaciones: List<EstacionCatalogo> = emptyList(),
    val actividades: List<ActividadCatalogo> = emptyList(),

    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false
) {
    val esEdicion: Boolean get() = editandoId != null

    val stepName: String
        get() = when (currentStep) {
            1 -> "CONTEXTO"
            2 -> "ACTIVIDAD"
            3 -> "RESULTADO"
            else -> "EVIDENCIA"
        }

    /** Mismas 3 categorías que `OBS_BY_TIPO` del diseño: cambian según tipoActividad. */
    val observacionesRapidas: List<String>
        get() = OBS_BY_TIPO[tipoActividad] ?: OBS_BY_TIPO_DEFAULT

    val observacionesRapidasTitulo: String
        get() = when (tipoActividad) {
            "MANTENIMIENTO" -> "MÁS USADAS EN MANTENIMIENTO"
            "INSPECCION" -> "MÁS USADAS EN INSPECCIÓN"
            else -> "MÁS USADAS"
        }
}

private val OBS_BY_TIPO_DEFAULT = listOf(
    "Atención de emergencia por temporada de lluvias",
    "Se requiere material adicional para terminar",
    "Se reporta hallazgo a supervisión",
    "Trabajo suspendido por condiciones de seguridad"
)

private val OBS_BY_TIPO = mapOf(
    "MANTENIMIENTO" to listOf(
        "Se realiza limpieza de pozo de succión",
        "Se realiza mantenimiento de compuerta",
        "Se realiza corte y retiro de malezas",
        "Se aplica pintura anticorrosiva en estructura",
        "Se realiza muro para elevar cerramiento",
        "Se resanan fisuras en placa de concreto"
    ),
    "INSPECCION" to listOf(
        "Se inspecciona y no se evidencian novedades",
        "Se evidencia acumulación de sedimentos en pozo",
        "Se evidencia fisura en placa de concreto",
        "Cerramiento y vías internas en buen estado",
        "Cubierta con láminas sueltas",
        "Maleza alta en perímetro, requiere corte"
    ),
    "DEFAULT" to OBS_BY_TIPO_DEFAULT
)

/** min(4, ceil(díaDelMes / 7)) — misma fórmula que usa el diseño ya validado con el backend. */
private fun semanaDeDia(dia: Int): Int = min(4, ceil(dia / 7.0).toInt())

private fun mesDeFecha(fechaIso: String): Int =
    runCatching { LocalDate.parse(fechaIso).monthValue }.getOrDefault(LocalDate.now().monthValue)

private fun semanaDeFecha(fechaIso: String): Int =
    runCatching { semanaDeDia(LocalDate.parse(fechaIso).dayOfMonth) }.getOrDefault(1)

@HiltViewModel
class CapturaViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val guardarEjecucionLocalUseCase: GuardarEjecucionLocalUseCase,
    private val obtenerEstacionesCacheUseCase: ObtenerEstacionesCacheUseCase,
    private val obtenerActividadesCacheUseCase: ObtenerActividadesCacheUseCase,
    private val obtenerDetalleEjecucionUseCase: ObtenerDetalleEjecucionUseCase,
    private val editarEjecucionUseCase: EditarEjecucionUseCase,
    private val obtenerPendientesUseCase: ObtenerPendientesUseCase,
    private val localSyncCoordinator: LocalSyncCoordinator,
    private val tokenManager: com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CapturaUiState())
    val uiState = _uiState.asStateFlow()

    /** Citas pendientes/vencidas cacheadas para sugerir en modo libre — se pide una
     * sola vez por instancia del ViewModel, no en cada tap de estación. */
    private var citasPendientesCache: List<Pair<CitaProgramada, EstadoCita>>? = null

    init {
        viewModelScope.launch {
            tokenManager.getUsername().collect { username ->
                _uiState.update { it.copy(responsableNombre = username ?: "") }
            }
        }
        viewModelScope.launch {
            obtenerEstacionesCacheUseCase().collect { lista ->
                _uiState.update { s ->
                    // Si cargarDesdeCita() ya fijó estacionId pero el catálogo local
                    // todavía no había llegado, resuelve el nombre ahora que llegó.
                    val nombre = if (s.estacionNombre.isBlank() && s.estacionId != null) {
                        lista.find { it.id == s.estacionId }?.nombre ?: s.estacionNombre
                    } else s.estacionNombre
                    s.copy(estaciones = lista, estacionNombre = nombre)
                }
            }
        }
        viewModelScope.launch {
            obtenerActividadesCacheUseCase().collect { lista ->
                _uiState.update { s ->
                    val nombre = if (s.actividadNombre.isNullOrBlank() && s.actividadId != null) {
                        lista.find { it.id == s.actividadId }?.nombre ?: s.actividadNombre
                    } else s.actividadNombre
                    s.copy(actividades = lista, actividadNombre = nombre)
                }
            }
        }
        // Asegura que el catálogo llegue pronto en el primer uso del módulo; si no hay
        // señal, WorkManager lo deja encolado (constraint NetworkType.CONNECTED) y no
        // rompe nada — el wizard sigue usable con lo que ya haya en cache.
        viewModelScope.launch {
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.ManualSync(LocalSyncCoordinator.SyncType.SUBSTATION_CATALOG)
            )
        }
    }

    /**
     * Precarga desde una cita del cronograma (M5: Cronograma/Pendientes ya tienen en
     * memoria estación+actividad+si está vencida, así que las pasan resueltas por la
     * ruta de navegación en vez de forzar al wizard a re-consultarlas). Replica
     * `openCita()` del diseño: tipoActividad = INSPECCION si el nombre de la
     * actividad empieza con "Inspecci", si no MANTENIMIENTO; tipoMantenimiento =
     * CORRECTIVO si la cita está vencida, si no PREVENTIVO.
     */
    fun cargarDesdeCita(
        programacionId: Long,
        estacionId: Long,
        actividadId: Long,
        esInspeccion: Boolean,
        vencida: Boolean
    ) {
        if (programacionId <= 0L) return
        val nombreEstacion = _uiState.value.estaciones.find { it.id == estacionId }?.nombre
        val nombreActividad = _uiState.value.actividades.find { it.id == actividadId }?.nombre
        _uiState.update {
            it.copy(
                programacionId = programacionId,
                estacionId = if (estacionId > 0L) estacionId else it.estacionId,
                estacionNombre = nombreEstacion ?: it.estacionNombre,
                actividadId = if (actividadId > 0L) actividadId else it.actividadId,
                actividadNombre = nombreActividad ?: it.actividadNombre,
                modoLibre = false,
                tipoActividad = if (esInspeccion) "INSPECCION" else "MANTENIMIENTO",
                tipoMantenimiento = if (vencida) "CORRECTIVO" else "PREVENTIVO"
            )
        }
    }

    /**
     * Modo edición (M6): carga una ejecución ya sincronizada para corregirla. Estación,
     * disciplina, programacionId y uuidCliente NO se editan (el backend tampoco lo
     * permite — EjecucionEditRequest no los trae).
     */
    fun cargarParaEditar(ejecucionId: Long) {
        if (_uiState.value.editandoId == ejecucionId) return
        _uiState.update { it.copy(editandoId = ejecucionId, cargandoEdicion = true, errorEdicion = null) }
        viewModelScope.launch {
            obtenerDetalleEjecucionUseCase(ejecucionId)
                .onSuccess { d ->
                    _uiState.update {
                        it.copy(
                            cargandoEdicion = false,
                            fecha = d.fecha,
                            estacionId = d.estacionId,
                            estacionNombre = d.estacionNombre,
                            mesEjecucion = d.mesEjecucion,
                            semanaEjecucion = d.semanaEjecucion,
                            periodoAjustadoManualmente = true,
                            tipoActividad = d.tipoActividad,
                            tipoMantenimiento = d.tipoMantenimiento,
                            modoLibre = d.actividadId == null,
                            actividadId = d.actividadId,
                            actividadNombre = d.actividadNombre,
                            descripcionLibre = d.descripcionLibre ?: "",
                            resultado = d.resultado,
                            observaciones = d.observaciones
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(cargandoEdicion = false, errorEdicion = e.message ?: "No se pudo cargar el registro para editar.")
                    }
                }
        }
    }

    /**
     * Entrada desde "Actividad no programada" (Home) / "+ Registrar algo que no está
     * en el cronograma" (Cronograma). No precarga nada del formulario — solo deja
     * listo en memoria el catálogo de citas pendientes/vencidas para que, si el
     * técnico termina eligiendo del catálogo una actividad que coincide con una cita
     * real, se detecte sola (ver `actualizarCitaSugeridaPorActividad`).
     */
    fun iniciarLibre() {
        // No forzamos modoLibre ni los tipos acá: una estación puede tener 10-20
        // actividades del catálogo, así que adivinar por estación sola no sirve. Se
        // deja que el técnico elija normalmente en el paso 2 — si la actividad que
        // elige del catálogo coincide con una cita pendiente/vencida de esa estación
        // (comparación exacta estación+actividad, ver `onActividadSeleccionada`), se
        // detecta sola; si usa texto libre, por definición no es del catálogo y no
        // hay nada que comparar.
        viewModelScope.launch { cargarPendientesParaSugerencia() }
    }

    /**
     * Trae (una sola vez por instancia del ViewModel) las citas pendientes/vencidas
     * del año en curso, para poder detectar cuando la actividad elegida del catálogo
     * en realidad ya estaba en el cronograma de esa estación — si no, ese registro
     * queda huérfano (no resuelve la cita) y la estación se sigue viendo como vencida.
     * `suspend` (no lanza su propia corrutina) para que los llamadores puedan esperar
     * a que `citasPendientesCache` quede listo antes de comparar contra él.
     */
    private suspend fun cargarPendientesParaSugerencia() {
        if (citasPendientesCache != null) return
        val hoy = LocalDate.now()
        obtenerPendientesUseCase(hoy.year, hoy.monthValue)
            .onSuccess { citas ->
                citasPendientesCache = citas
                    .map { it to estadoDeCita(it, hoy) }
                    .filter { it.second == EstadoCita.PENDIENTE || it.second == EstadoCita.VENCIDA }
            }
            .onFailure { citasPendientesCache = emptyList() }
    }

    /** INSPECCION/MANTENIMIENTO que le correspondería a una cita según el nombre de su
     * actividad — misma regla que `cargarDesdeCita`, para no duplicar el criterio. */
    private fun tipoActividadDeCita(actividadNombre: String): String =
        if (actividadNombre.startsWith("Inspecci", ignoreCase = true)) "INSPECCION" else "MANTENIMIENTO"

    /** PREVENTIVO/CORRECTIVO que le correspondería a una cita según si está vencida —
     * misma regla que `cargarDesdeCita`. */
    private fun tipoMantenimientoDeCita(estado: EstadoCita): String =
        if (estado == EstadoCita.VENCIDA) "CORRECTIVO" else "PREVENTIVO"

    /**
     * Coincidencia validada desde 4 puntos — no basta con estación+actividad, porque
     * la misma actividad puede tener varias citas en distintos meses (trimestral,
     * anual): también debe coincidir el mes/año que el técnico ya puso en el paso 1,
     * y el tipo de actividad / tipo de mantenimiento que ya eligió arriba en este
     * mismo paso deben ser justo los que esa cita implica. Si algo de eso varía, no
     * es la misma actividad — no se sugiere nada (tal como se decidió: "si varía es
     * otro tipo de actividad").
     */
    private fun actualizarCitaSugeridaPorActividad() {
        val s = _uiState.value
        val estacionId = s.estacionId
        val actividadId = s.actividadId
        if (estacionId == null || actividadId == null) {
            _uiState.update { it.copy(citaSugerida = null, citaSugeridaEstado = null) }
            return
        }
        val anioFecha = runCatching { LocalDate.parse(s.fecha).year }.getOrDefault(LocalDate.now().year)
        val match = citasPendientesCache?.firstOrNull { (cita, estado) ->
            cita.estacionId == estacionId &&
                cita.actividadId == actividadId &&
                cita.mes == s.mesEjecucion &&
                cita.anio == anioFecha &&
                tipoActividadDeCita(cita.actividadNombre) == s.tipoActividad &&
                tipoMantenimientoDeCita(estado) == s.tipoMantenimiento
        }
        _uiState.update { it.copy(citaSugerida = match?.first, citaSugeridaEstado = match?.second) }
    }

    /** El técnico confirma que el registro libre es en realidad la cita sugerida —
     * lo convierte en un registro normal ligado a esa cita, igual que si hubiera
     * entrado desde Cronograma. */
    fun vincularCitaSugerida() {
        val cita = _uiState.value.citaSugerida ?: return
        val vencida = _uiState.value.citaSugeridaEstado == EstadoCita.VENCIDA
        cargarDesdeCita(
            programacionId = cita.programacionId,
            estacionId = cita.estacionId,
            actividadId = cita.actividadId,
            esInspeccion = cita.actividadNombre.startsWith("Inspecci", ignoreCase = true),
            vencida = vencida
        )
        _uiState.update { it.copy(citaSugerida = null, citaSugeridaEstado = null) }
    }

    /** El técnico confirma que es aparte de esa cita — no se vuelve a sugerir para
     * esta estación mientras siga seleccionada. */
    fun descartarCitaSugerida() {
        _uiState.update { it.copy(citaSugerida = null, citaSugeridaEstado = null) }
    }

    fun onMotivoEdicionChange(texto: String) {
        _uiState.update { it.copy(motivoEdicion = texto) }
    }

    fun onFechaChange(fecha: String) {
        _uiState.update {
            it.copy(
                fecha = fecha,
                mesEjecucion = if (!it.periodoAjustadoManualmente) mesDeFecha(fecha) else it.mesEjecucion,
                semanaEjecucion = if (!it.periodoAjustadoManualmente) semanaDeFecha(fecha) else it.semanaEjecucion
            )
        }
        actualizarCitaSugeridaPorActividad()
    }

    fun onEstacionSeleccionada(estacion: EstacionCatalogo) {
        // Cambiar de estación invalida cualquier coincidencia previa — se vuelve a
        // calcular cuando el técnico elija una actividad de esta nueva estación.
        _uiState.update { it.copy(estacionId = estacion.id, estacionNombre = estacion.nombre, citaSugerida = null, citaSugeridaEstado = null) }
        viewModelScope.launch { cargarPendientesParaSugerencia() }
    }

    /**
     * Selector de disciplina del paso Contexto (mockup). Solo CIVIL está habilitada en
     * el MVP (`captura_movil_habilitada=FALSE` en backend para las otras dos) — elegir
     * ELECTRICO/ELECTROMECANICO no debe dejar seleccionar esa disciplina de verdad
     * (el catálogo cargado y lo que se envía a las rutas de substation siguen siendo
     * CIVIL), así que el estado se queda en CIVIL y solo se activa el flag para que la
     * UI muestre el diálogo de aviso.
     */
    fun onDisciplinaSeleccionada(disciplina: String) {
        if (disciplina == "CIVIL") {
            _uiState.update {
                it.copy(disciplinaSeleccionada = "CIVIL", mostrarAlertaDisciplinaEnDesarrollo = false, disciplinaSeleccionadaPendiente = null)
            }
        } else {
            _uiState.update { it.copy(mostrarAlertaDisciplinaEnDesarrollo = true, disciplinaSeleccionadaPendiente = disciplina) }
        }
    }

    /** Cierra el aviso de "disciplina en desarrollo" — la disciplina real ya se quedó en CIVIL. */
    fun onAlertaDisciplinaEnDesarrolloCerrada() {
        _uiState.update {
            it.copy(disciplinaSeleccionada = "CIVIL", mostrarAlertaDisciplinaEnDesarrollo = false, disciplinaSeleccionadaPendiente = null)
        }
    }

    fun onMesManualChange(mes: Int) {
        _uiState.update { it.copy(mesEjecucion = mes, periodoAjustadoManualmente = true) }
        actualizarCitaSugeridaPorActividad()
    }

    fun onSemanaManualChange(semana: Int) {
        _uiState.update { it.copy(semanaEjecucion = semana, periodoAjustadoManualmente = true) }
    }

    fun onRestablecerPeriodoAuto() {
        _uiState.update {
            it.copy(
                periodoAjustadoManualmente = false,
                mesEjecucion = mesDeFecha(it.fecha),
                semanaEjecucion = semanaDeFecha(it.fecha)
            )
        }
        actualizarCitaSugeridaPorActividad()
    }

    fun onTipoActividadChange(tipo: String) {
        _uiState.update { it.copy(tipoActividad = tipo) }
        actualizarCitaSugeridaPorActividad()
    }

    fun onTipoMantenimientoChange(tipo: String) {
        _uiState.update { it.copy(tipoMantenimiento = tipo) }
        actualizarCitaSugeridaPorActividad()
    }

    fun onToggleModoLibre() {
        _uiState.update {
            it.copy(
                modoLibre = !it.modoLibre,
                actividadId = null,
                actividadNombre = null,
                descripcionLibre = if (it.modoLibre) "" else it.descripcionLibre,
                // Texto libre por definición no es del catálogo — no hay nada que
                // comparar contra el cronograma.
                citaSugerida = null,
                citaSugeridaEstado = null
            )
        }
    }

    /**
     * Si el registro venía prellenado de una cita del cronograma (`programacionId` no
     * nulo, ver `cargarDesdeCita`) y el técnico elige una actividad DISTINTA a la de
     * esa cita, hay que desvincular (`programacionId = null`) — si no, el registro se
     * seguiría enviando ligado a la cita vieja y esta quedaría marcada "cumplida" sin
     * serlo de verdad (bug reportado en campo: se cambió de actividad a mitad de la
     * captura y la cita original del cronograma quedó falsamente resuelta).
     *
     * Tras desvincular, se reutiliza el mismo mecanismo de "modo libre" para revisar
     * si existe una cita real que sí corresponda a la nueva actividad+estación+mes+año
     * — si la hay, se ofrece confirmarla (`citaSugerida`, igual que en modo libre); si
     * no, el registro queda como actividad no programada, que es el comportamiento
     * correcto.
     *
     * No aplica si es la misma actividad que ya tenía la cita (no hay nada que
     * desvincular) ni en la primera selección sin `programacionId` previo (flujo
     * normal). El flujo de edición (`cargarParaEditar`) nunca fija `programacionId`
     * en este ViewModel — por diseño esa pantalla no permite tocarlo — así que esta
     * función es un no-op para ese campo cuando se llama durante una edición.
     */
    fun onActividadSeleccionada(actividad: ActividadCatalogo) {
        val previo = _uiState.value
        val cambioDeActividadConCitaVinculada =
            previo.programacionId != null && previo.actividadId != actividad.id
        _uiState.update {
            it.copy(
                actividadId = actividad.id,
                actividadNombre = actividad.nombre,
                programacionId = if (cambioDeActividadConCitaVinculada) null else it.programacionId
            )
        }
        viewModelScope.launch {
            cargarPendientesParaSugerencia()
            actualizarCitaSugeridaPorActividad()
        }
    }

    fun onDescripcionLibreChange(texto: String) {
        _uiState.update { it.copy(descripcionLibre = texto) }
    }

    fun onResultadoChange(resultado: String) {
        _uiState.update { it.copy(resultado = resultado) }
    }

    fun onObservacionesChange(texto: String) {
        _uiState.update { it.copy(observaciones = texto) }
    }

    fun onObservacionRapidaClick(frase: String) {
        _uiState.update { current ->
            val actual = current.observaciones
            val nueva = if (actual.contains(frase)) {
                actual.replace(frase, "").replace(Regex("^\\.\\s*"), "").trim()
            } else if (actual.isBlank()) {
                frase
            } else {
                actual.trimEnd().trimEnd('.') + ". " + frase
            }
            current.copy(observaciones = nueva)
        }
    }

    fun onFotoSeleccionada(uri: Uri) {
        viewModelScope.launch {
            val comprimida = ImageUtils.compressAndSaveImage(context, uri) ?: return@launch
            _uiState.update { it.copy(fotos = it.fotos + comprimida) }
        }
    }

    fun onFotoRemovida(uri: Uri) {
        _uiState.update { it.copy(fotos = it.fotos.filter { f -> f != uri }) }
    }

    /**
     * Reglas idénticas a `valid(step)` del diseño ya ajustado con el backend (sin
     * "Otro"). En modo edición, el paso 4 exige el motivo (≥15 caracteres) en vez de
     * la foto — no se editan fotos, ver decisión en el plan de implementación.
     */
    fun esPasoValido(step: Int): Boolean {
        val s = _uiState.value
        return when (step) {
            1 -> s.estacionId != null && s.fecha.isNotBlank()
            2 -> s.tipoActividad.isNotBlank() && s.tipoMantenimiento.isNotBlank() &&
                    (if (s.modoLibre) s.descripcionLibre.trim().length > 3 else s.actividadId != null)
            3 -> s.resultado.isNotBlank() && s.observaciones.isNotBlank()
            4 -> if (s.esEdicion) s.motivoEdicion.trim().length >= 15
                 else (s.resultado == "CONFORME" || s.fotos.isNotEmpty())
            else -> true
        }
    }

    fun onSiguiente() {
        val s = _uiState.value
        if (!esPasoValido(s.currentStep)) return
        if (s.currentStep < 4) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        } else if (s.esEdicion) {
            guardarEdicion()
        } else {
            guardar()
        }
    }

    fun onAnterior(): Boolean {
        val s = _uiState.value
        if (s.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
            return true
        }
        return false
    }

    private fun guardar() {
        val s = _uiState.value
        if (!esPasoValido(4) || s.estacionId == null) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val ejecucion = Ejecucion(
                uuidCliente = UUID.randomUUID().toString(),
                fecha = s.fecha,
                mesEjecucion = s.mesEjecucion,
                semanaEjecucion = s.semanaEjecucion,
                estacionId = s.estacionId,
                estacionNombre = s.estacionNombre,
                tipoMantenimiento = s.tipoMantenimiento,
                tipoActividad = s.tipoActividad,
                actividadId = if (s.modoLibre) null else s.actividadId,
                actividadNombre = if (s.modoLibre) null else s.actividadNombre,
                programacionId = s.programacionId,
                esProgramada = s.programacionId != null,
                motivoNoCatalogado = if (s.modoLibre) "NO_PROGRAMADO" else null,
                resultado = s.resultado,
                observaciones = s.observaciones,
                descripcionLibre = if (s.modoLibre) s.descripcionLibre.trim() else null
            )
            guardarEjecucionLocalUseCase(ejecucion, s.fotos.map { it.toString() })
            localSyncCoordinator.coordinateSync(
                LocalSyncCoordinator.SyncTrigger.SubstationSaved("captura")
            )
            _uiState.update { it.copy(isSaving = false, saveCompleted = true) }
        }
    }

    fun onConfirmacionMostrada() {
        _uiState.update { it.copy(saveCompleted = false) }
    }

    private fun guardarEdicion() {
        val s = _uiState.value
        val id = s.editandoId ?: return
        if (!esPasoValido(4)) return

        _uiState.update { it.copy(isSaving = true, errorEdicion = null) }
        viewModelScope.launch {
            val edicion = EjecucionEdicion(
                fecha = s.fecha,
                mesEjecucion = s.mesEjecucion,
                semanaEjecucion = s.semanaEjecucion,
                tipoMantenimiento = s.tipoMantenimiento,
                tipoActividad = s.tipoActividad,
                actividadId = if (s.modoLibre) null else s.actividadId,
                motivoNoCatalogado = if (s.modoLibre) "NO_PROGRAMADO" else null,
                resultado = s.resultado,
                observaciones = s.observaciones,
                descripcionLibre = if (s.modoLibre) s.descripcionLibre.trim() else null,
                motivoEdicion = s.motivoEdicion.trim()
            )
            editarEjecucionUseCase(id, edicion)
                .onSuccess { _uiState.update { it.copy(isSaving = false, saveCompleted = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSaving = false, errorEdicion = e.message ?: "No se pudo guardar la edición.")
                    }
                }
        }
    }
}
