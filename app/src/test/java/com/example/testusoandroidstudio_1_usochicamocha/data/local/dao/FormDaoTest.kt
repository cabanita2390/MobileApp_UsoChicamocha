package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.testusoandroidstudio_1_usochicamocha.data.local.AppDatabase
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FormEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.PendingFormWithImageCount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FormDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var formDao: FormDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        formDao = db.formDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertForm_should_insert_form_successfully() = runBlocking {
        val form = FormEntity(
            UUID = "test-uuid",
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

        formDao.insertForm(form)

        val pendingForms = formDao.getPendingFormsFlow().first()
        assertEquals(1, pendingForms.size)
        assertEquals("test-uuid", pendingForms[0].UUID)
    }

    @Test
    fun markAsSynced_should_update_form_status() = runBlocking {
        val form = FormEntity(
            UUID = "test-uuid",
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

        formDao.insertForm(form)
        formDao.markAsSynced("test-uuid", 1001L)

        val pendingForms = formDao.getPendingFormsFlow().first()
        assertTrue(pendingForms.isEmpty()) // Should be empty since isSynced = 1 now
    }

    @Test
    fun markAsSyncing_should_update_syncing_status() = runBlocking {
        val form = FormEntity(
            UUID = "test-uuid",
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

        formDao.insertForm(form)
        formDao.acquireFormLock("test-uuid")

        val pendingForms = formDao.getPendingFormsFlow().first()
        assertTrue(pendingForms.isEmpty()) // Should be empty since isSyncing = 1 now
    }

    @Test
    fun markAsNotSyncing_should_reset_syncing_status() = runBlocking {
        val form = FormEntity(
            UUID = "test-uuid",
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
            isSyncing = true // Start as syncing
        )

        formDao.insertForm(form)
        formDao.markAsNotSyncing("test-uuid")

        val pendingForms = formDao.getPendingFormsFlow().first()
        assertEquals(1, pendingForms.size) // Should be back in pending list
        assertEquals(false, pendingForms[0].isSyncing)
    }

    @Test
    fun getPendingFormsWithImageCount_should_return_correct_counts() = runBlocking {
        // Insert form
        val form = FormEntity(
            UUID = "test-uuid",
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
        formDao.insertForm(form)

        // Insert images
        val images = listOf(
            ImageEntity(formUUID = "test-uuid", localUri = "uri1.jpg", isSynced = false),
            ImageEntity(formUUID = "test-uuid", localUri = "uri2.jpg", isSynced = true),
            ImageEntity(formUUID = "test-uuid", localUri = "uri3.jpg", isSynced = false)
        )
        db.imageDao().insertImages(images)

        val result = formDao.getPendingFormsWithImageCount().first()
        assertEquals(1, result.size)
        assertEquals("test-uuid", result[0].formEntity.UUID)
        assertEquals(3, result[0].totalImageCount)
        assertEquals(1, result[0].syncedImageCount)
    }

    @Test
    fun getPendingFormsFlow_should_only_return_unsynced_forms() = runBlocking {
        val unsyncedForm = FormEntity(
            UUID = "unsynced-uuid",
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

        val syncedForm = FormEntity(
            UUID = "synced-uuid",
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
            isSynced = true,
            isSyncing = false
        )

        formDao.insertForm(unsyncedForm)
        formDao.insertForm(syncedForm)

        val pendingForms = formDao.getPendingFormsFlow().first()
        assertEquals(1, pendingForms.size)
        assertEquals("unsynced-uuid", pendingForms[0].UUID)
    }
}