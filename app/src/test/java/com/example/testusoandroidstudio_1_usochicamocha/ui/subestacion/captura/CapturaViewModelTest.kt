package com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.captura

import android.content.Context
import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ActividadCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionDetalle
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EjecucionEdicion
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.EstacionCatalogo
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.EditarEjecucionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.GuardarEjecucionLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerActividadesCacheUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerDetalleEjecucionUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerEstacionesCacheUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion.ObtenerPendientesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.EstadoCita
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Cubre la lógica de negocio más nueva y riesgosa de `CapturaViewModel`, construida y
 * revisada en esta misma sesión: la detección de "esta actividad libre en realidad ya
 * tenía una cita en el cronograma" (validada desde 4 puntos — estación, actividad,
 * mes/año y tipo de actividad/mantenimiento, no solo por estación, que dejaba
 * demasiados falsos positivos entre las 10-20 actividades de una estación), las
 * validaciones de cada paso del wizard, y el flujo de edición.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CapturaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var context: Context
    @MockK
    lateinit var guardarEjecucionLocalUseCase: GuardarEjecucionLocalUseCase
    @MockK
    lateinit var obtenerEstacionesCacheUseCase: ObtenerEstacionesCacheUseCase
    @MockK
    lateinit var obtenerActividadesCacheUseCase: ObtenerActividadesCacheUseCase
    @MockK
    lateinit var obtenerDetalleEjecucionUseCase: ObtenerDetalleEjecucionUseCase
    @MockK
    lateinit var editarEjecucionUseCase: EditarEjecucionUseCase
    @MockK
    lateinit var obtenerPendientesUseCase: ObtenerPendientesUseCase
    @MockK
    lateinit var localSyncCoordinator: LocalSyncCoordinator
    @MockK
    lateinit var tokenManager: TokenManager

    private lateinit var viewModel: CapturaViewModel

    // Estación y actividad usadas en los casos de "coincide con el cronograma".
    private val estacion = EstacionCatalogo(id = 10L, nombre = "Duitama", tipo = "BOMBEO", frecuenciaBase = "TRIMESTRAL")
    private val actividad = ActividadCatalogo(id = 20L, nombre = "Inspección y mantenimiento compuertas")

    private val hoy = LocalDate.now()

    /** Cita PENDIENTE (mes/año actuales, sin cumplir) que calza exactamente con [estacion]+[actividad]. */
    private val citaCoincide = CitaProgramada(
        programacionId = 100L,
        anio = hoy.year,
        mes = hoy.monthValue,
        estacionId = estacion.id,
        estacionNombre = estacion.nombre,
        estacionTipo = estacion.tipo,
        actividadId = actividad.id,
        actividadNombre = actividad.nombre,
        ejecutado = 0,
        cumple = false
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { obtenerEstacionesCacheUseCase() } returns flowOf(emptyList())
        every { obtenerActividadesCacheUseCase() } returns flowOf(emptyList())
        every { tokenManager.getUsername() } returns flowOf("tecnico.test")
        coEvery { localSyncCoordinator.coordinateSync(any()) } returns Result.success(Unit)
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(emptyList())
    }

    private fun createViewModel() {
        viewModel = CapturaViewModel(
            context,
            guardarEjecucionLocalUseCase,
            obtenerEstacionesCacheUseCase,
            obtenerActividadesCacheUseCase,
            obtenerDetalleEjecucionUseCase,
            editarEjecucionUseCase,
            obtenerPendientesUseCase,
            localSyncCoordinator,
            tokenManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ---- iniciarLibre ya no fuerza nada (se discutió y se quitó el preset) ----

    @Test
    fun `iniciarLibre no fuerza modoLibre ni tipos`() = runTest {
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaCoincide))
        createViewModel()

        viewModel.iniciarLibre()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.modoLibre)
        assertEquals("", viewModel.uiState.value.tipoActividad)
        assertEquals("", viewModel.uiState.value.tipoMantenimiento)
    }

    // ---- Coincidencia validada desde 4 puntos ----

    @Test
    fun `sugiere vincular cuando estacion actividad mes y tipos coinciden con una cita`() = runTest {
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaCoincide))
        createViewModel()

        viewModel.onEstacionSeleccionada(estacion)
        advanceUntilIdle()
        viewModel.onTipoActividadChange("INSPECCION")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()

        assertEquals(citaCoincide, viewModel.uiState.value.citaSugerida)
        assertEquals(EstadoCita.PENDIENTE, viewModel.uiState.value.citaSugeridaEstado)
    }

    @Test
    fun `no sugiere nada si el mes no coincide con la cita`() = runTest {
        val otroMes = if (hoy.monthValue == 12) 6 else 12
        val citaOtroMes = citaCoincide.copy(mes = otroMes)
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaOtroMes))
        createViewModel()

        viewModel.onEstacionSeleccionada(estacion)
        advanceUntilIdle()
        viewModel.onTipoActividadChange("INSPECCION")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.citaSugerida)
    }

    @Test
    fun `no sugiere nada si el tipo de actividad elegido no coincide con el de la cita`() = runTest {
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaCoincide))
        createViewModel()

        viewModel.onEstacionSeleccionada(estacion)
        advanceUntilIdle()
        // La cita implica INSPECCION (el nombre empieza con "Inspecci"); el técnico eligió MANTENIMIENTO.
        viewModel.onTipoActividadChange("MANTENIMIENTO")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.citaSugerida)
    }

    @Test
    fun `no sugiere nada si la actividad elegida no es la de la cita`() = runTest {
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaCoincide))
        createViewModel()

        viewModel.onEstacionSeleccionada(estacion)
        advanceUntilIdle()
        viewModel.onTipoActividadChange("INSPECCION")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        viewModel.onActividadSeleccionada(ActividadCatalogo(id = 999L, nombre = "Otra actividad cualquiera"))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.citaSugerida)
    }

    @Test
    fun `vincularCitaSugerida liga el registro a la cita y limpia la sugerencia`() = runTest {
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaCoincide))
        createViewModel()
        viewModel.onEstacionSeleccionada(estacion)
        advanceUntilIdle()
        viewModel.onTipoActividadChange("INSPECCION")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()
        assertEquals(citaCoincide, viewModel.uiState.value.citaSugerida)

        viewModel.vincularCitaSugerida()

        val estado = viewModel.uiState.value
        assertEquals(citaCoincide.programacionId, estado.programacionId)
        assertFalse(estado.modoLibre)
        assertNull(estado.citaSugerida)
        assertNull(estado.citaSugeridaEstado)
    }

    @Test
    fun `descartarCitaSugerida limpia la sugerencia sin tocar el resto del formulario`() = runTest {
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaCoincide))
        createViewModel()
        viewModel.onEstacionSeleccionada(estacion)
        advanceUntilIdle()
        viewModel.onTipoActividadChange("INSPECCION")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()

        viewModel.descartarCitaSugerida()

        val estado = viewModel.uiState.value
        assertNull(estado.citaSugerida)
        assertEquals(actividad.id, estado.actividadId)
        assertEquals(estacion.id, estado.estacionId)
    }

    @Test
    fun `pasar a texto libre limpia cualquier sugerencia ya calculada`() = runTest {
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(listOf(citaCoincide))
        createViewModel()
        viewModel.onEstacionSeleccionada(estacion)
        advanceUntilIdle()
        viewModel.onTipoActividadChange("INSPECCION")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()
        assertEquals(citaCoincide, viewModel.uiState.value.citaSugerida)

        viewModel.onToggleModoLibre()

        val estado = viewModel.uiState.value
        assertTrue(estado.modoLibre)
        assertNull(estado.actividadId)
        assertNull(estado.citaSugerida)
    }

    // ---- Desvinculación de la cita al cambiar de actividad (bug de campo: se
    // marcaba "cumplida" la cita original aunque en realidad se hizo otra actividad) ----

    @Test
    fun `cambiar de actividad prellenada desde una cita desvincula programacionId`() = runTest {
        createViewModel()
        viewModel.cargarDesdeCita(
            programacionId = 100L, estacionId = estacion.id, actividadId = actividad.id,
            esInspeccion = true, vencida = false
        )

        val otraActividad = ActividadCatalogo(id = 999L, nombre = "Otra actividad cualquiera")
        viewModel.onActividadSeleccionada(otraActividad)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertNull(estado.programacionId)
        assertEquals(otraActividad.id, estado.actividadId)
    }

    @Test
    fun `reseleccionar la misma actividad de la cita no desvincula nada`() = runTest {
        createViewModel()
        viewModel.cargarDesdeCita(
            programacionId = 100L, estacionId = estacion.id, actividadId = actividad.id,
            esInspeccion = true, vencida = false
        )

        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()

        assertEquals(100L, viewModel.uiState.value.programacionId)
    }

    @Test
    fun `primera seleccion de actividad sin cita previa no desvincula nada (sigue null)`() = runTest {
        createViewModel()

        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.programacionId)
        assertEquals(actividad.id, viewModel.uiState.value.actividadId)
    }

    @Test
    fun `cambiar de actividad prellenada desde una cita sugiere la cita real que corresponde a la nueva actividad`() = runTest {
        // Segunda cita del cronograma: misma estación, otra actividad, mismo mes/año.
        val otraActividad = ActividadCatalogo(id = 30L, nombre = "Inspección de cerramiento")
        val citaDeLaOtraActividad = citaCoincide.copy(
            programacionId = 200L, actividadId = otraActividad.id, actividadNombre = otraActividad.nombre
        )
        coEvery { obtenerPendientesUseCase(any(), any()) } returns Result.success(
            listOf(citaCoincide, citaDeLaOtraActividad)
        )
        createViewModel()
        viewModel.cargarDesdeCita(
            programacionId = citaCoincide.programacionId, estacionId = estacion.id,
            actividadId = actividad.id, esInspeccion = true, vencida = false
        )
        advanceUntilIdle()
        viewModel.onTipoActividadChange("INSPECCION")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")

        viewModel.onActividadSeleccionada(otraActividad)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertNull(estado.programacionId)
        assertEquals(citaDeLaOtraActividad, estado.citaSugerida)
    }

    // ---- citaMode: 3 pasos y bloqueo de datos que ya define el cronograma ----

    @Test
    fun `citaMode es true solo cuando hay programacionId y no esta en modo libre`() = runTest {
        createViewModel()
        assertFalse(viewModel.uiState.value.citaMode)
        assertEquals(4, viewModel.uiState.value.nSteps)

        viewModel.cargarDesdeCita(
            programacionId = 100L, estacionId = estacion.id, actividadId = actividad.id,
            esInspeccion = true, vencida = false
        )

        assertTrue(viewModel.uiState.value.citaMode)
        assertEquals(3, viewModel.uiState.value.nSteps)
    }

    @Test
    fun `onToggleModoLibre desde una cita limpia programacionId — cierra el hueco senalado en el fix anterior`() = runTest {
        createViewModel()
        viewModel.cargarDesdeCita(
            programacionId = 100L, estacionId = estacion.id, actividadId = actividad.id,
            esInspeccion = true, vencida = false
        )
        assertEquals(100L, viewModel.uiState.value.programacionId)
        assertTrue(viewModel.uiState.value.citaMode)

        viewModel.onToggleModoLibre()

        val estado = viewModel.uiState.value
        assertNull(estado.programacionId)
        assertTrue(estado.modoLibre)
        assertFalse(estado.citaMode)
        assertEquals(4, estado.nSteps)
    }

    @Test
    fun `onToggleModoLibre fuera de modo cita no tiene programacionId que limpiar y sigue funcionando igual`() = runTest {
        createViewModel()
        // Flujo libre normal (sin cita): activar y desactivar modo libre no debe verse
        // afectado por el fix — programacionId ya era null y se mantiene null.
        viewModel.onToggleModoLibre()
        assertTrue(viewModel.uiState.value.modoLibre)
        assertNull(viewModel.uiState.value.programacionId)

        viewModel.onToggleModoLibre()
        assertFalse(viewModel.uiState.value.modoLibre)
        assertNull(viewModel.uiState.value.programacionId)
    }

    @Test
    fun `paso 1 en modo cita solo exige fecha y tipo de actividad realizada`() = runTest {
        createViewModel()
        // cargarDesdeCita ya deja tipoActividad prellenado segun esInspeccion (regla
        // existente de openCita()) y fecha viene con LocalDate.now() por defecto —
        // el paso 1 en modo cita ya queda valido sin digitar nada mas.
        viewModel.cargarDesdeCita(
            programacionId = 100L, estacionId = estacion.id, actividadId = actividad.id,
            esInspeccion = true, vencida = false
        )
        assertEquals("INSPECCION", viewModel.uiState.value.tipoActividad)
        assertTrue(viewModel.esPasoValido(1))

        // Pero si el tecnico corrige el tipo de actividad realizada (no coincidio
        // exactamente con lo previsto), el paso sigue siendo valido mientras haya algo elegido.
        viewModel.onTipoActividadChange("MANTENIMIENTO")
        assertTrue(viewModel.esPasoValido(1))

        // No hay forma real desde la UI de dejar tipoActividad en blanco en modo cita
        // (cargarDesdeCita siempre lo prellena) — pero la regla de esPasoValido(1) en
        // si misma exige que no este vacio, cubierto indirectamente arriba.
    }

    @Test
    fun `paso 2 en modo cita es el resultado (no hay paso de actividad separado)`() = runTest {
        createViewModel()
        viewModel.cargarDesdeCita(
            programacionId = 100L, estacionId = estacion.id, actividadId = actividad.id,
            esInspeccion = true, vencida = false
        )
        assertFalse(viewModel.esPasoValido(2))

        viewModel.onResultadoChange("CONFORME")
        assertFalse(viewModel.esPasoValido(2))

        viewModel.onObservacionesChange("Todo en orden")
        assertTrue(viewModel.esPasoValido(2))
    }

    @Test
    fun `paso 3 en modo cita es la evidencia, igual regla que el ultimo paso en modo libre`() = runTest {
        createViewModel()
        viewModel.cargarDesdeCita(
            programacionId = 100L, estacionId = estacion.id, actividadId = actividad.id,
            esInspeccion = true, vencida = false
        )
        viewModel.onResultadoChange("CON_HALLAZGOS")
        assertFalse(viewModel.esPasoValido(3))

        viewModel.onResultadoChange("CONFORME")
        assertTrue(viewModel.esPasoValido(3))
    }

    @Test
    fun `onSiguiente en modo cita avanza solo 3 pasos y despues guarda`() = runTest {
        coEvery { guardarEjecucionLocalUseCase(any(), any()) } just Runs
        createViewModel()
        viewModel.cargarDesdeCita(
            programacionId = 100L, estacionId = estacion.id, actividadId = actividad.id,
            esInspeccion = true, vencida = false
        )
        viewModel.onTipoActividadChange("INSPECCION")

        viewModel.onSiguiente()
        assertEquals(2, viewModel.uiState.value.currentStep)

        viewModel.onResultadoChange("CONFORME")
        viewModel.onObservacionesChange("Todo en orden")
        viewModel.onSiguiente()
        assertEquals(3, viewModel.uiState.value.currentStep)

        // CONFORME no exige foto — el 3er "Siguiente" guarda en vez de avanzar a un paso 4 que no existe.
        viewModel.onSiguiente()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.currentStep)
        assertTrue(viewModel.uiState.value.saveCompleted)
    }

    // ---- Validación de pasos del wizard (modo libre — sin cambios respecto a como estaba) ----

    @Test
    fun `paso 1 requiere estacion seleccionada`() = runTest {
        createViewModel()
        assertFalse(viewModel.esPasoValido(1))

        viewModel.onEstacionSeleccionada(estacion)
        advanceUntilIdle()

        assertTrue(viewModel.esPasoValido(1))
    }

    @Test
    fun `paso 2 en modo catalogo requiere tipos y una actividad elegida`() = runTest {
        createViewModel()
        assertFalse(viewModel.esPasoValido(2))

        viewModel.onTipoActividadChange("MANTENIMIENTO")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        assertFalse(viewModel.esPasoValido(2))

        viewModel.onActividadSeleccionada(actividad)
        advanceUntilIdle()
        assertTrue(viewModel.esPasoValido(2))
    }

    @Test
    fun `paso 2 en modo libre requiere descripcion de mas de 3 caracteres`() = runTest {
        createViewModel()
        viewModel.onTipoActividadChange("MANTENIMIENTO")
        viewModel.onTipoMantenimientoChange("PREVENTIVO")
        viewModel.onToggleModoLibre()
        assertFalse(viewModel.esPasoValido(2))

        viewModel.onDescripcionLibreChange("ab")
        assertFalse(viewModel.esPasoValido(2))

        viewModel.onDescripcionLibreChange("Se revisó la compuerta")
        assertTrue(viewModel.esPasoValido(2))
    }

    @Test
    fun `paso 3 requiere resultado y observaciones`() = runTest {
        createViewModel()
        assertFalse(viewModel.esPasoValido(3))

        viewModel.onResultadoChange("CONFORME")
        assertFalse(viewModel.esPasoValido(3))

        viewModel.onObservacionesChange("Todo en orden")
        assertTrue(viewModel.esPasoValido(3))
    }

    @Test
    fun `paso 4 sin edicion exige foto solo si el resultado no es conforme`() = runTest {
        createViewModel()
        viewModel.onResultadoChange("CON_HALLAZGOS")
        assertFalse(viewModel.esPasoValido(4))

        viewModel.onResultadoChange("CONFORME")
        assertTrue(viewModel.esPasoValido(4))
    }

    @Test
    fun `paso 4 en edicion exige motivo de al menos 15 caracteres`() = runTest {
        val detalle = detalleFixture()
        coEvery { obtenerDetalleEjecucionUseCase(any()) } returns Result.success(detalle)
        createViewModel()
        viewModel.cargarParaEditar(1L)
        advanceUntilIdle()

        assertFalse(viewModel.esPasoValido(4))

        viewModel.onMotivoEdicionChange("corto")
        assertFalse(viewModel.esPasoValido(4))

        viewModel.onMotivoEdicionChange("Se corrigió el tipo de mantenimiento reportado")
        assertTrue(viewModel.esPasoValido(4))
    }

    // ---- Flujo de edición ----

    @Test
    fun `cargarParaEditar precarga los campos del registro`() = runTest {
        val detalle = detalleFixture()
        coEvery { obtenerDetalleEjecucionUseCase(any()) } returns Result.success(detalle)
        createViewModel()

        viewModel.cargarParaEditar(1L)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado.esEdicion)
        assertFalse(estado.cargandoEdicion)
        assertEquals(detalle.estacionId, estado.estacionId)
        assertEquals(detalle.tipoActividad, estado.tipoActividad)
        assertEquals(detalle.tipoMantenimiento, estado.tipoMantenimiento)
        assertEquals(detalle.actividadId, estado.actividadId)
        assertFalse(estado.modoLibre)
    }

    @Test
    fun `guardarEdicion envia el payload correcto y marca saveCompleted`() = runTest {
        val detalle = detalleFixture()
        var edicionEnviada: EjecucionEdicion? = null
        coEvery { obtenerDetalleEjecucionUseCase(any()) } returns Result.success(detalle)
        coEvery { editarEjecucionUseCase(any(), any()) } answers {
            edicionEnviada = secondArg()
            Result.success(detalle)
        }
        createViewModel()

        viewModel.cargarParaEditar(1L)
        advanceUntilIdle()
        viewModel.onMotivoEdicionChange("Se corrigió el resultado reportado en campo")

        // Avanza los 4 pasos — cargarParaEditar ya dejó 1-3 válidos.
        repeat(4) { viewModel.onSiguiente() }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveCompleted)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals("Se corrigió el resultado reportado en campo", edicionEnviada?.motivoEdicion)
        assertEquals(detalle.tipoActividad, edicionEnviada?.tipoActividad)
        assertEquals(detalle.actividadId, edicionEnviada?.actividadId)
    }

    private fun detalleFixture() = EjecucionDetalle(
        id = 1L,
        fecha = hoy.toString(),
        mesEjecucion = hoy.monthValue,
        semanaEjecucion = 1,
        estacionId = estacion.id,
        estacionNombre = estacion.nombre,
        disciplina = "CIVIL",
        tipoMantenimiento = "PREVENTIVO",
        tipoActividad = "INSPECCION",
        actividadId = actividad.id,
        actividadNombre = actividad.nombre,
        esProgramada = true,
        motivoNoCatalogado = null,
        resultado = "CONFORME",
        observaciones = "Sin novedades",
        descripcionLibre = null,
        responsable = "tecnico.test",
        uuidCliente = "uuid-1",
        evidencias = emptyList(),
        evidenciaPendiente = false,
        ediciones = emptyList()
    )
}
