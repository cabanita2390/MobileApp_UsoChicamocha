package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.MotoOilChangeDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.VehiculoOilChangeDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MotoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.VehiculoOilChangeEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Cambios de aceite guardados sin cantidad (versiones anteriores de la app) no se envían:
 * el backend los rechaza siempre y quedaban reintentando sin fin.
 */
class OilChangeSinCantidadRepositoryTest {

    private val api = mockk<ApiService>()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() = unmockkAll()

    private fun vehiculo(id: Int, cantidad: Double?) = VehiculoOilChangeEntity(
        localId = id, placa = "ABC123", timestamp = 0L, oilType = "motor", oilBrandId = 1,
        oilBrandName = "Mobil", quantity = cantidad, kmAtChange = 5000, intervalKm = 5000
    )

    @Test
    fun `vehiculo - el registro sin cantidad no se envia y el resto si`() = runTest {
        val dao = mockk<VehiculoOilChangeDao>(relaxed = true)
        coEvery { dao.getPending() } returns listOf(vehiculo(1, null), vehiculo(2, 4.0))
        coEvery { dao.acquireLock(any()) } returns 1
        coEvery { api.registerVehicleOilChange(any()) } returns Response.success(Unit)

        val result = VehiculoOilChangeRepositoryImpl(dao, api).syncPending()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { api.registerVehicleOilChange(match { it.quantity == 4.0 }) }
        coVerify(exactly = 0) { dao.acquireLock(1) }
        coVerify(exactly = 1) { dao.markAsSynced(2) }
    }

    @Test
    fun `vehiculo - deletePending borra solo pendientes`() = runTest {
        val dao = mockk<VehiculoOilChangeDao>(relaxed = true)

        VehiculoOilChangeRepositoryImpl(dao, api).deletePending(7)

        coVerify(exactly = 1) { dao.deletePending(7) }
    }

    @Test
    fun `moto - el registro sin cantidad no se envia`() = runTest {
        val dao = mockk<MotoOilChangeDao>(relaxed = true)
        coEvery { dao.getPendingOilChanges() } returns listOf(
            MotoOilChangeEntity(
                localId = 3, placa = "MTO12A", timestamp = 0L, oilType = "motor", oilBrandId = 1,
                oilBrandName = "Mobil", quantity = null, kmAtChange = 9000, intervalKm = 3000
            )
        )

        val result = MotoOilChangeRepositoryImpl(dao, api).syncPending()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { api.registerMotoOilChange(any(), any()) }
        coVerify(exactly = 0) { dao.acquireOilChangeLock(any()) }
    }
}
