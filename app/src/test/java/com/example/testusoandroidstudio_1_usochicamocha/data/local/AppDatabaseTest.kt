package com.example.testusoandroidstudio_1_usochicamocha.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun database_creation_should_succeed() {
        assertNotNull(db)
        assertNotNull(db.formDao())
        assertNotNull(db.machineDao())
        assertNotNull(db.logDao())
        assertNotNull(db.machineOilChangeDao())
        assertNotNull(db.oilDao())
        assertNotNull(db.imageDao())
    }

    @Test
    @Ignore("Template for future migration tests")
    fun migration_1_to_2_should_work() {
        // Create database at version 1
        val db = migrationTestHelper.createDatabase("test_db", 1)
        // You can insert data here to test with
        db.close()

        // Run migration to version 2 and validate
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            "test_db",
            2,
            true,
            // AppDatabase.MIGRATION_1_2 // Uncomment when you have the migration
        )

        // Verify the schema changes here
        // For example, check if a new table or column was added
        migratedDb.close()
    }
}
