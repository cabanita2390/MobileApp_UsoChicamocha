package com.example.testusoandroidstudio_1_usochicamocha

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FullAuditTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    companion object {
        private const val TIMEOUT = 30000L
        private const val STABILIZATION_DELAY = 3000L
        
        private const val USERNAME = "admin"
        private const val PASSWORD = "admin123"
        
        private const val MOTO_KILO = "13500"
        private const val MACHINE_HORO = "5000"
        private const val TEST_OBS = "Auditoría Completa - Test Automático"
    }

    @Test
    fun executeMotoAuditFlow() {
        // 1. ENSURE START FROM LOGIN
        resetAppState()

        // 2. LOGIN
        performLogin()

        // 3. MOTO INSPECTION
        performMotoInspection()
    }

    private fun resetAppState() {
        // Wait for Splash to end
        composeTestRule.waitUntil(timeoutMillis = TIMEOUT) {
            try {
                composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("Menú Principal").fetchSemanticsNodes().isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        Thread.sleep(1000)

        // If we see the Main Menu, log out
        val onMainMenu = try {
            composeTestRule.onAllNodesWithText("Menú Principal").fetchSemanticsNodes().isNotEmpty()
        } catch (e: Exception) {
            false
        }

        if (onMainMenu) {
            println("App is logged in, performing logout...")
            composeTestRule.onNodeWithContentDescription("Cerrar Sesión").performClick()
            Thread.sleep(STABILIZATION_DELAY)
            // Confirm logout if dialog appears
            try {
                composeTestRule.onNodeWithText("Salir").performClick()
                Thread.sleep(STABILIZATION_DELAY)
            } catch (e: Exception) { /* No dialog or already clicked */ }
        }

        // Final check: Wait for Login Screen
        composeTestRule.waitUntil(timeoutMillis = TIMEOUT) {
            composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(1000)
    }

    private fun performLogin() {
        composeTestRule.onNodeWithText("Usuario").performTextInput(USERNAME)
        Thread.sleep(1500)
        composeTestRule.onNodeWithText("Contraseña").performTextInput(PASSWORD)
        Thread.sleep(1500)
        composeTestRule.onNodeWithText("Ingresar").performClick()
        Thread.sleep(STABILIZATION_DELAY)

        composeTestRule.waitUntil(timeoutMillis = TIMEOUT) {
            composeTestRule.onAllNodesWithText("Menú Principal").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun performMotoInspection() {
        // Go to Moto Hub
        composeTestRule.onNodeWithText("Inspección de Motos").performClick()
        Thread.sleep(STABILIZATION_DELAY)
        
        composeTestRule.waitUntil(timeoutMillis = TIMEOUT) {
            composeTestRule.onAllNodesWithText("Menú Motocicletas").fetchSemanticsNodes().isNotEmpty()
        }

        // Go to Moto Form
        composeTestRule.onNodeWithText("Formulario de Motos").performClick()
        Thread.sleep(STABILIZATION_DELAY)

        composeTestRule.waitUntil(timeoutMillis = TIMEOUT) {
            composeTestRule.onAllNodesWithText("Inspección Motocicleta").fetchSemanticsNodes().isNotEmpty()
        }

        // Select Plate
        // Select Plate
        val plateLabel = "Seleccione La PLACA de Su Motocicleta (*)"
        composeTestRule.onNodeWithText(plateLabel).performScrollTo().performClick()
        Thread.sleep(2000)
        
        // Pick any plate using the new unique tag
        composeTestRule.onAllNodes(hasTestTag("plate_option")).onFirst().performClick()
        Thread.sleep(2000)

        // Select Unit
        val unitLabel = "Seleccione la UNIDAD a la que Pertenece (*)"
        composeTestRule.onNodeWithText(unitLabel).performScrollTo().performClick()
        
        // Wait for items to appear (up to 5 seconds)
        Thread.sleep(2000)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("unit_option")).fetchSemanticsNodes().isNotEmpty()
        }
        
        // Pick any unit using the new unique tag
        composeTestRule.onAllNodes(hasTestTag("unit_option")).onFirst().performClick()
        Thread.sleep(2000)

        // Fill Kilometraje
        val kiloLabel = "Escriba el KILOMETRAJE Actual de la Moto (*)"
        composeTestRule.onNodeWithText(kiloLabel).performScrollTo().performClick()
        composeTestRule.onNodeWithText(kiloLabel).performTextClearance()
        composeTestRule.onNodeWithText(kiloLabel).performTextInput(MOTO_KILO)
        // Dismiss keyboard definitively
        composeTestRule.onNodeWithText(kiloLabel).performImeAction()
        Thread.sleep(2000)

        // Select General Status
        val scrollTag = "moto_form_scroll"
        
        // Use scroll action of LazyColumn to reach sections
        composeTestRule.onNode(hasTestTag(scrollTag)).performScrollToNode(hasTestTag("section_vigencia"))
        Thread.sleep(1000)
        
        composeTestRule.onNode(hasTestTag(scrollTag)).performScrollToNode(hasTestTag("section_estado"))
        Thread.sleep(1000)
        
        composeTestRule.onNode(hasText("ptimo", substring = true)).performClick()
        Thread.sleep(2000)

        // Fill Observations
        composeTestRule.onNode(hasTestTag(scrollTag)).performScrollToNode(hasTestTag("section_observaciones"))
        Thread.sleep(1000)
        
        val obsLabel = "Observaciones y/o Aspectos a Revisar (*)"
        composeTestRule.onNodeWithText(obsLabel).performClick()
        composeTestRule.onNodeWithText(obsLabel).performTextClearance()
        composeTestRule.onNodeWithText(obsLabel).performTextInput(TEST_OBS)
        composeTestRule.onNodeWithText(obsLabel).performImeAction()
        Thread.sleep(2000)

        // Final Scroll to Save
        composeTestRule.onNode(hasTestTag(scrollTag)).performScrollToNode(hasTestTag("btn_guardar"))
        Thread.sleep(1000)
        
        composeTestRule.onNode(hasTestTag("btn_guardar")).performClick()
        
        // Wait for Success Dialog and accept using testTag
        composeTestRule.waitUntil(timeoutMillis = TIMEOUT) {
            composeTestRule.onAllNodes(hasTestTag("btn_done_audit")).fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(2000)
        composeTestRule.onNode(hasTestTag("btn_done_audit")).performClick()
        Thread.sleep(STABILIZATION_DELAY)

        // Verify we are back in Moto Hub or Menu
        composeTestRule.waitUntil(timeoutMillis = TIMEOUT) {
            composeTestRule.onAllNodesWithText("Menú Principal").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Menú Motocicletas").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
