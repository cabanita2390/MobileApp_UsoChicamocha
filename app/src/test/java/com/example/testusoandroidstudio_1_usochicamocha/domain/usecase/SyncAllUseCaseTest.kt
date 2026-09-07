package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncFormUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.SyncPendingImagesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form.TriggerImageSyncUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machineoilchange.SyncMachineOilChangeFormsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.SyncMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.oil.SyncOilsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.MachineOilChangeForm
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FormRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MachineOilChangeRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MachineRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.OilRepository
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.ImageForSync
import com.example.testusoandroidstudio_1_usochicamocha.util.AppLogger
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SyncAllUseCaseTest {

    private lateinit var formRepository: FormRepository

    private lateinit var machineOilChangeRepository: MachineOilChangeRepository

    private lateinit var machineRepository: MachineRepository

    private lateinit var oilRepository: OilRepository

    private lateinit var logger: AppLogger

    private lateinit var context: Context

    private lateinit var workManager: WorkManager

    private lateinit var syncFormUseCase: SyncFormUseCase
    private lateinit var syncMachineOilChangeFormsUseCase: SyncMachineOilChangeFormsUseCase
    private lateinit var syncMachinesUseCase: SyncMachinesUseCase
    private lateinit var syncOilsUseCase: SyncOilsUseCase
    private lateinit var syncPendingImagesUseCase: SyncPendingImagesUseCase
    private lateinit var triggerImageSyncUseCase: TriggerImageSyncUseCase

    @Before
    fun setUp() {
        // Initialize mocks
        formRepository = mockk(relaxed = true)
        machineOilChangeRepository = mockk(relaxed = true)
        machineRepository = mockk(relaxed = true)
        oilRepository = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        context = mockk(relaxed = true)
        workManager = mockk(relaxed = true)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        syncFormUseCase = SyncFormUseCase(formRepository)
        syncMachineOilChangeFormsUseCase = SyncMachineOilChangeFormsUseCase(machineOilChangeRepository)
        syncMachinesUseCase = SyncMachinesUseCase(machineRepository, logger)
        syncOilsUseCase = SyncOilsUseCase(oilRepository, logger)
        syncPendingImagesUseCase = SyncPendingImagesUseCase(formRepository)
        triggerImageSyncUseCase = TriggerImageSyncUseCase(context)

        // Mock WorkManager
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager
    }

    @Test
    fun `test image sync operations with pending images`() = runTest {
        // Mock image sync operations
        val sampleImages = listOf(
            ImageForSync(localId = 1, serverId = 1001L, localUri = "/storage/image1.jpg"),
            ImageForSync(localId = 2, serverId = 1002L, localUri = "/storage/image2.jpg"),
            ImageForSync(localId = 3, serverId = 1003L, localUri = "/storage/image3.jpg")
        )
        
        // Mock repository responses for image sync
        coEvery { formRepository.getPendingImagesForSync() } returns flowOf(sampleImages)
        coEvery { formRepository.syncImage(any(), any()) } returns Result.success(Unit)
        coEvery { formRepository.markImageAsSynced(any()) } returns Unit
        
        // Execute image sync
        val result = syncPendingImagesUseCase()
        
        // Verify success
        assertTrue("Image sync should succeed", result.isSuccess)
        
        // Verify that each image was processed
        coVerify(exactly = 1) { formRepository.getPendingImagesForSync() }
        coVerify(exactly = 3) { formRepository.syncImage(any(), any()) }
        coVerify(exactly = 3) { formRepository.markImageAsSynced(any()) }
    }

    @Test
    fun `test image sync operations with no pending images`() = runTest {
        // Mock empty image list
        coEvery { formRepository.getPendingImagesForSync() } returns flowOf(emptyList())
        
        // Execute image sync
        val result = syncPendingImagesUseCase()
        
        // Verify success (no images to sync is considered success)
        assertTrue("Image sync with no images should succeed", result.isSuccess)
        
        // Verify that no sync operations were called
        coVerify(exactly = 1) { formRepository.getPendingImagesForSync() }
        coVerify(exactly = 0) { formRepository.syncImage(any(), any()) }
        coVerify(exactly = 0) { formRepository.markImageAsSynced(any()) }
    }

    @Test
    fun `test image sync operations with partial failures`() = runTest {
        // Mock image sync operations with mixed results
        val sampleImages = listOf(
            ImageForSync(localId = 1, serverId = 1001L, localUri = "/storage/image1.jpg"),
            ImageForSync(localId = 2, serverId = 1002L, localUri = "/storage/image2.jpg"),
            ImageForSync(localId = 3, serverId = 1003L, localUri = "/storage/image3.jpg")
        )
        
        coEvery { formRepository.getPendingImagesForSync() } returns flowOf(sampleImages)
        
        // Mock mixed results: first succeeds, second fails, third succeeds
        coEvery { formRepository.syncImage(1001L, "/storage/image1.jpg") } returns Result.success(Unit)
        coEvery { formRepository.syncImage(1002L, "/storage/image2.jpg") } returns Result.failure(Exception("Network error"))
        coEvery { formRepository.syncImage(1003L, "/storage/image3.jpg") } returns Result.success(Unit)
        
        coEvery { formRepository.markImageAsSynced(any()) } returns Unit
        
        // Execute image sync
        val result = syncPendingImagesUseCase()
        
        // Should still succeed overall (partial failures are handled)
        assertTrue("Image sync should succeed even with partial failures", result.isSuccess)
        
        // Verify that successful images were marked as synced
        coVerify(exactly = 1) { formRepository.markImageAsSynced(1) }
        coVerify(exactly = 0) { formRepository.markImageAsSynced(2) } // Failed, so not marked
        coVerify(exactly = 1) { formRepository.markImageAsSynced(3) }
    }

    @Test
    fun `test trigger image sync use case`() = runTest {
        // Mock work manager to return empty work infos (no running jobs)
        val mockWorkManager = mockk<WorkManager>()
        val mockWorkInfo = mockk<androidx.work.WorkInfo> {
            every { state } returns androidx.work.WorkInfo.State.SUCCEEDED
        }
        val mockOperation = mockk<androidx.work.Operation>()

        every { mockWorkManager.getWorkInfosForUniqueWork("manual_image_sync") } returns mockk {
            every { get() } returns listOf(mockWorkInfo)
        }
        every { mockWorkManager.getWorkInfosForUniqueWork("chained_image_sync") } returns mockk {
            every { get() } returns listOf(mockWorkInfo)
        }
        every { mockWorkManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) } returns mockOperation

        // Mock static WorkManager
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns mockWorkManager

        // Create new instance with mocked work manager
        val testTriggerUseCase = TriggerImageSyncUseCase(context)

        // Execute trigger
        testTriggerUseCase()

        // Verify WorkManager was called to enqueue work
        verify { mockWorkManager.enqueueUniqueWork("manual_image_sync", androidx.work.ExistingWorkPolicy.KEEP, any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun `test random sync operations with success and failure scenarios`() = runTest {
        // Mock successful sync for all repositories including images
        coEvery { formRepository.syncForm(any()) } returns Result.success(Unit)
        coEvery { machineOilChangeRepository.syncMachineOilChangeForm(any()) } returns Result.success(Unit)
        coEvery { machineOilChangeRepository.deleteMachineOilChangeForm(any()) } returns Result.success(Unit)
        coEvery { machineRepository.syncMachines() } returns Result.success(Unit)
        coEvery { oilRepository.syncOils() } returns Result.success(Unit)
        coEvery { formRepository.getPendingImagesForSync() } returns flowOf(emptyList())

        // Create sample data
        val sampleForm = Form(
            localId = 1,
            serverId = 1001L,
            UUID = "uuid-form-1",
            timestamp = System.currentTimeMillis(),
            machineId = 1L,
            userId = 1L,
            hourmeter = "100",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Test observations",
            greasingAction = "applied",
            greasingObservations = "Test greasing",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )

        val sampleMachineOilChange = MachineOilChangeForm(
            id = 1,
            machineId = 1,
            dateTime = System.currentTimeMillis(),
            brand = "Test Brand",
            brandId = 1,
            quantity = 5.0,
            currentHourMeter = 100,
            averageHoursChange = 50,
            type = "preventive",
            isSynced = false,
            isSyncing = false
        )

        // Test all sync operations sequentially (not randomly)
        val syncOperations = listOf(
            suspend { syncFormUseCase(sampleForm) },
            suspend { syncMachineOilChangeFormsUseCase(sampleMachineOilChange) },
            suspend { syncMachinesUseCase() },
            suspend { syncOilsUseCase() },
            suspend { syncPendingImagesUseCase() }
        )

        // Run all sync operations sequentially
        syncOperations.forEachIndexed { index, operation ->
            val result = operation()
            assertTrue("Sync operation $index should succeed", result.isSuccess)
        }

        // Verify that all operations were called exactly once
        coVerify(exactly = 1) { formRepository.syncForm(any()) }
        coVerify(exactly = 1) { machineOilChangeRepository.syncMachineOilChangeForm(any()) }
        coVerify(exactly = 1) { machineRepository.syncMachines() }
        coVerify(exactly = 1) { oilRepository.syncOils() }
        coVerify(exactly = 1) { formRepository.getPendingImagesForSync() }
    }

    @Test
    fun `test random sync operations with mixed success and failure`() = runTest {
        // Mock mixed results - some succeed, some fail
        coEvery { formRepository.syncForm(any()) } returns Result.success(Unit)
        coEvery { machineOilChangeRepository.syncMachineOilChangeForm(any()) } returns Result.failure(Exception("Sync failed"))
        coEvery { machineRepository.syncMachines() } returns Result.success(Unit)
        coEvery { oilRepository.syncOils() } returns Result.failure(Exception("Network error"))
        coEvery { formRepository.getPendingImagesForSync() } returns flowOf(emptyList())

        val sampleForm = Form(
            localId = 2,
            serverId = 1002L,
            UUID = "uuid-form-2",
            timestamp = System.currentTimeMillis(),
            machineId = 2L,
            userId = 2L,
            hourmeter = "200",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Test observations 2",
            greasingAction = "applied",
            greasingObservations = "Test greasing 2",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )

        val sampleMachineOilChange = MachineOilChangeForm(
            id = 2,
            machineId = 2,
            dateTime = System.currentTimeMillis(),
            brand = "Test Brand 2",
            brandId = 2,
            quantity = 10.0,
            currentHourMeter = 200,
            averageHoursChange = 100,
            type = "corrective",
            isSynced = false,
            isSyncing = false
        )

        // Test all sync operations sequentially with expected results
        val syncOperations = listOf(
            suspend { syncFormUseCase(sampleForm) } to true,  // Should succeed
            suspend { syncMachineOilChangeFormsUseCase(sampleMachineOilChange) } to false, // Should fail
            suspend { syncMachinesUseCase() } to true,  // Should succeed
            suspend { syncOilsUseCase() } to false,  // Should fail
            suspend { syncPendingImagesUseCase() } to true  // Should succeed
        )

        // Run all sync operations sequentially and check expected results
        syncOperations.forEachIndexed { index, (operation, shouldSucceed) ->
            val result = operation()
            if (shouldSucceed) {
                assertTrue("Sync operation $index should succeed", result.isSuccess)
            } else {
                assertTrue("Sync operation $index should fail", result.isFailure)
            }
        }

        // Verify that all operations were called exactly once
        coVerify(exactly = 1) { formRepository.syncForm(any()) }
        coVerify(exactly = 1) { machineOilChangeRepository.syncMachineOilChangeForm(any()) }
        coVerify(exactly = 1) { machineRepository.syncMachines() }
        coVerify(exactly = 1) { oilRepository.syncOils() }
        coVerify(exactly = 1) { formRepository.getPendingImagesForSync() }
    }

    @Test
    fun `test concurrent random sync operations`() = runTest {
        // Mock all operations to succeed including images
        coEvery { formRepository.syncForm(any()) } returns Result.success(Unit)
        coEvery { machineOilChangeRepository.syncMachineOilChangeForm(any()) } returns Result.success(Unit)
        coEvery { machineOilChangeRepository.deleteMachineOilChangeForm(any()) } returns Result.success(Unit)
        coEvery { machineRepository.syncMachines() } returns Result.success(Unit)
        coEvery { oilRepository.syncOils() } returns Result.success(Unit)
        coEvery { formRepository.getPendingImagesForSync() } returns flowOf(emptyList())

        val sampleForm = Form(
            localId = 3,
            serverId = 1003L,
            UUID = "uuid-form-3",
            timestamp = System.currentTimeMillis(),
            machineId = 3L,
            userId = 3L,
            hourmeter = "300",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Concurrent test",
            greasingAction = "applied",
            greasingObservations = "Concurrent greasing",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )

        val sampleMachineOilChange = MachineOilChangeForm(
            id = 3,
            machineId = 3,
            dateTime = System.currentTimeMillis(),
            brand = "Concurrent Brand",
            brandId = 3,
            quantity = 15.0,
            currentHourMeter = 300,
            averageHoursChange = 150,
            type = "preventive",
            isSynced = false,
            isSyncing = false
        )

        // Run all sync operations sequentially (not concurrently for deterministic testing)
        val syncOperations = listOf(
            suspend { syncFormUseCase(sampleForm) },
            suspend { syncMachineOilChangeFormsUseCase(sampleMachineOilChange) },
            suspend { syncMachinesUseCase() },
            suspend { syncOilsUseCase() },
            suspend { syncPendingImagesUseCase() }
        )

        // Run all operations multiple times sequentially
        val results = mutableListOf<Result<Unit>>()
        repeat(15) {
            syncOperations.forEach { operation ->
                results.add(operation())
            }
        }

        // Verify they all succeed
        results.forEach { result ->
            assertTrue("Sequential sync operation should succeed", result.isSuccess)
        }

        // Verify that operations were called the expected number of times
        coVerify(exactly = 15) { formRepository.syncForm(any()) }
        coVerify(exactly = 15) { machineOilChangeRepository.syncMachineOilChangeForm(any()) }
        coVerify(exactly = 15) { machineRepository.syncMachines() }
        coVerify(exactly = 15) { oilRepository.syncOils() }
        coVerify(exactly = 15) { formRepository.getPendingImagesForSync() }
    }

    @Test
    fun `test complete random sync workflow including images`() = runTest {
        // Mock all sync operations
        coEvery { formRepository.syncForm(any()) } returns Result.success(Unit)
        coEvery { machineOilChangeRepository.syncMachineOilChangeForm(any()) } returns Result.success(Unit)
        coEvery { machineOilChangeRepository.deleteMachineOilChangeForm(any()) } returns Result.success(Unit)
        coEvery { machineRepository.syncMachines() } returns Result.success(Unit)
        coEvery { oilRepository.syncOils() } returns Result.success(Unit)

        // Mock image sync with some pending images
        val pendingImages = listOf(
            ImageForSync(localId = 1, serverId = 1001L, localUri = "/storage/workflow_image1.jpg"),
            ImageForSync(localId = 2, serverId = 1002L, localUri = "/storage/workflow_image2.jpg")
        )
        coEvery { formRepository.getPendingImagesForSync() } returns flowOf(pendingImages)
        coEvery { formRepository.syncImage(any(), any()) } returns Result.success(Unit)
        coEvery { formRepository.markImageAsSynced(any()) } returns Unit

        val sampleForm = Form(
            localId = 4,
            serverId = 1004L,
            UUID = "uuid-workflow-form",
            timestamp = System.currentTimeMillis(),
            machineId = 4L,
            userId = 4L,
            hourmeter = "400",
            leakStatus = "good",
            brakeStatus = "good",
            beltsPulleysStatus = "good",
            tireLanesStatus = "good",
            carIgnitionStatus = "good",
            electricalStatus = "good",
            mechanicalStatus = "good",
            temperatureStatus = "good",
            oilStatus = "good",
            hydraulicStatus = "good",
            coolantStatus = "good",
            structuralStatus = "good",
            expirationDateFireExtinguisher = "2024-12-31",
            observations = "Workflow test",
            greasingAction = "applied",
            greasingObservations = "Workflow greasing",
            isUnexpected = false,
            isSynced = false,
            isSyncing = false
        )

        val sampleMachineOilChange = MachineOilChangeForm(
            id = 4,
            machineId = 4,
            dateTime = System.currentTimeMillis(),
            brand = "Workflow Brand",
            brandId = 4,
            quantity = 20.0,
            currentHourMeter = 400,
            averageHoursChange = 200,
            type = "workflow",
            isSynced = false,
            isSyncing = false
        )

        // All sync operations including images - run sequentially for deterministic testing
        val allSyncOperations = listOf(
            suspend { syncFormUseCase(sampleForm) },
            suspend { syncMachineOilChangeFormsUseCase(sampleMachineOilChange) },
            suspend { syncMachinesUseCase() },
            suspend { syncOilsUseCase() },
            suspend { syncPendingImagesUseCase() }
        )

        // Execute all sync operations multiple times sequentially
        repeat(30) {
            allSyncOperations.forEachIndexed { index, operation ->
                val result = operation()
                assertTrue("Complete workflow sync operation $index should succeed", result.isSuccess)
            }
        }

        // Verify all types of sync operations were called the expected number of times
        coVerify(exactly = 30) { formRepository.syncForm(any()) }
        coVerify(exactly = 30) { machineOilChangeRepository.syncMachineOilChangeForm(any()) }
        coVerify(exactly = 30) { machineRepository.syncMachines() }
        coVerify(exactly = 30) { oilRepository.syncOils() }
        coVerify(exactly = 30) { formRepository.getPendingImagesForSync() }
        coVerify(exactly = 60) { formRepository.syncImage(any(), any()) } // 2 images * 30 times
        coVerify(exactly = 60) { formRepository.markImageAsSynced(any()) } // 2 images * 30 times
    }
}