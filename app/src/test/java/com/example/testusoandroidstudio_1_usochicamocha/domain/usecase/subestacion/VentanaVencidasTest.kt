package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.CitaProgramada
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Cambio de año: en enero y febrero las vencidas del año anterior siguen saliendo. */
class VentanaVencidasTest {

    private val repo = mockk<SubestacionRepository>(relaxed = true)

    private fun cita(id: Long, anio: Int, mes: Int, cumple: Boolean = false) = CitaProgramada(
        programacionId = id, anio = anio, mes = mes, estacionId = 1, estacionNombre = "Ayalas", estacionTipo = "BOMBEO",
        actividadId = 10, actividadNombre = "Pintura", ejecutado = if (cumple) 1 else 0, cumple = cumple
    )

    @Test
    fun `enero - pendientes suma las no ejecutadas del anio anterior`() = runTest {
        coEvery { repo.getPendientesDelAnio(2027, 1) } returns Result.success(listOf(cita(3, 2027, 1)))
        coEvery { repo.getPendientesDelAnio(2026, 12) } returns
            Result.success(listOf(cita(1, 2026, 12), cita(2, 2026, 11, cumple = true)))
        val uc = ObtenerPendientesUseCase(repo).apply { hoy = { LocalDate.of(2027, 1, 15) } }

        val r = uc(2027, 1).getOrThrow()

        assertEquals(listOf(1L, 3L), r.map { it.programacionId }) // la cumplida del año anterior no
    }

    @Test
    fun `marzo - ya no se suman las del anio anterior`() = runTest {
        coEvery { repo.getPendientesDelAnio(2027, 3) } returns Result.success(listOf(cita(3, 2027, 3)))
        val uc = ObtenerPendientesUseCase(repo).apply { hoy = { LocalDate.of(2027, 3, 1) } }

        assertEquals(listOf(3L), uc(2027, 3).getOrThrow().map { it.programacionId })
        coVerify(exactly = 0) { repo.getPendientesDelAnio(2026, any()) }
    }

    @Test
    fun `enero - si el anio anterior no se puede traer, sigue con el actual`() = runTest {
        coEvery { repo.getPendientesDelAnio(2027, 1) } returns Result.success(listOf(cita(3, 2027, 1)))
        coEvery { repo.getPendientesDelAnio(2026, 12) } returns Result.failure(Exception("sin red"))
        val uc = ObtenerPendientesUseCase(repo).apply { hoy = { LocalDate.of(2027, 1, 2) } }

        assertEquals(listOf(3L), uc(2027, 1).getOrThrow().map { it.programacionId })
    }

    @Test
    fun `febrero - el cache local suma las no ejecutadas del anio anterior y sincroniza los dos anios`() = runTest {
        every { repo.getCumplimientoLocalDelAnioFlow(2027) } returns flowOf(listOf(cita(3, 2027, 2)))
        every { repo.getCumplimientoLocalDelAnioFlow(2026) } returns
            flowOf(listOf(cita(1, 2026, 12), cita(2, 2026, 6, cumple = true)))
        val febrero = { LocalDate.of(2027, 2, 28) }

        val local = ObtenerCumplimientoAnioLocalUseCase(repo).apply { hoy = febrero }(2027).first()
        assertEquals(listOf(1L, 3L), local.map { it.programacionId })

        SincronizarCumplimientoAnioUseCase(repo).apply { hoy = febrero }(2027)
        coVerify { repo.sincronizarCumplimientoDelAnio(2026) }
        coVerify { repo.sincronizarCumplimientoDelAnio(2027) }
    }
}
