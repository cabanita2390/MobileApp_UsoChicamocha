package com.example.testusoandroidstudio_1_usochicamocha

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MotoHappyPathTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    companion object {
        private const val SPLASH_TIMEOUT = 30000L
        private const val NAVIGATION_TIMEOUT = 45000L
        private const val UI_STABILIZATION_DELAY = 1500L
        
        private const val TEST_USERNAME = "admin"
        private const val TEST_PASSWORD = "admin123"
        private const val TEST_KILOMETRAJE = "15500"
        private const val TEST_OBSERVATIONS = "Prueba automatizada de Moto - Todo en orden"
    }

    @Test
    fun moto_complete_flow_test() {
        // 1. LOGIN
        performLogin()

        // 2. NAVIGATE TO MOTO HUB
        composeTestRule.onNodeWithText("Inspección de Motos").performClick()
        
        // Wait for Hub screen
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Menú Motocicletas").fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate to actual Form
        composeTestRule.onNodeWithText("Formulario de Motos").performClick()
        
        // Wait for Form screen to load
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Inspección Motocicleta").fetchSemanticsNodes().isNotEmpty()
        }        // 3. FILL FORM
        
        // --- 1. PLATE ---
        // Scroll to and Select Plate
        composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("section_placa"))
        composeTestRule.onNodeWithTag("plate_option_dropdown").performClick()
        
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithTag("plate_option").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("plate_option")[0].performClick()
        
        // --- 2. UNIT ---
        composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("section_ubicacion"))
        composeTestRule.onNodeWithTag("unit_option_dropdown").performClick()
        
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithTag("unit_option").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("unit_option")[0].performClick()

        // --- 3. KILOMETRAJE Dinámico ---
        composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("section_km"))
        
        // Esperamos a que la UI se actualice con el último KM de la placa
        Thread.sleep(UI_STABILIZATION_DELAY * 2)
        val dynamicKm = getDynamicKilometraje()
        
        composeTestRule.onNodeWithTag("moto_km_field").performTextReplacement(dynamicKm)
        
        // Trigger blur and handle alert as fallback
        composeTestRule.onNodeWithTag("moto_km_field").performImeAction()
        composeTestRule.onNodeWithTag("moto_form_scroll").performClick()
        Thread.sleep(UI_STABILIZATION_DELAY)
        handleKmAlerts()
        
        // Ensure keyboard is closed
        composeTestRule.onNodeWithTag("moto_km_field").performImeAction()

        // --- 4. DOCUMENTS ---
        composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("section_vigencia"))
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithText("SOAT (Seguro Obligatorio)", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        // --- 5. MECHANICAL INSPECTION ---
        composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("section_mecanica"))
        composeTestRule.onNodeWithTag("status_aceite_Bueno").performClick()
        composeTestRule.onNodeWithTag("status_llantas_Bueno").performClick()
        composeTestRule.onNodeWithTag("status_luces_Bueno").performClick()

        // --- 6. GENERAL STATUS ---
        composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("section_estado"))
        composeTestRule.onNodeWithTag("status_general_Óptimo").performClick()

        // --- 7. OBSERVATIONS ---
        val obsLabel = "Observaciones y/o Aspectos a Revisar (*)"
        composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("section_observaciones"))
        composeTestRule.onNodeWithText(obsLabel, substring = true).performTextInput(TEST_OBSERVATIONS)
        
        // CERRAR TECLADO EXPLÍCITAMENTE
        composeTestRule.onNodeWithText(obsLabel, substring = true).performImeAction()

        // FINAL CHECK FOR LATE ALERTS
        handleKmAlerts()

        // 4. WAIT FOR SAVE ENABLED AND CLICK
        // Scroll to the actual button tag
        composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("btn_guardar"))
        
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            try {
                // Check if enabled using TAG for reliability
                composeTestRule.onNodeWithTag("btn_guardar").assertIsEnabled()
                true
            } catch (e: Exception) {
                // If not found or not enabled, scroll again just in case
                try {
                    composeTestRule.onNodeWithTag("moto_form_scroll").performScrollToNode(hasTestTag("btn_guardar"))
                } catch (sc: Exception) {}
                false
            }
        }

        // 5. SAVE
        composeTestRule.onNodeWithTag("btn_guardar").performClick()

        // 6. VERIFY SUCCESS AND GO BACK
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithTag("btn_done_audit").fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithTag("btn_done_audit").performClick()
        
        // Final Menu Verification (Wait for navigation to complete)
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Menú", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Menú", substring = true).assertIsDisplayed()
    }

    private fun performLogin() {
        // 1. Wait for EITHER login screen OR home screen (if already logged in)
        composeTestRule.waitUntil(timeoutMillis = SPLASH_TIMEOUT) {
            try {
                composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithContentDescription("Cerrar Sesión").fetchSemanticsNodes().isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

        // 2. If already logged in, perform logout to "start from login" as requested
        val logoutButtons = composeTestRule.onAllNodesWithContentDescription("Cerrar Sesión").fetchSemanticsNodes()
        if (logoutButtons.isNotEmpty()) {
            composeTestRule.onNodeWithContentDescription("Cerrar Sesión").performClick()
            Thread.sleep(UI_STABILIZATION_DELAY)
            composeTestRule.onNodeWithText("Salir").performClick()
            
            // Wait for login screen to appear after logout
            composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
                composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty()
            }
        }

        // 3. Perform Login
        composeTestRule.onNodeWithText("Usuario").performTextInput(TEST_USERNAME)
        composeTestRule.onNodeWithText("Contraseña").performTextInput(TEST_PASSWORD)
        composeTestRule.onNodeWithText("Ingresar").performClick()

        // Wait for main screen
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Menú Principal").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun getDynamicKilometraje(): String {
        // Wait a little bit for the API to load the plate's last KM
        Thread.sleep(UI_STABILIZATION_DELAY * 2)
        
        return try {
            val nodes = composeTestRule.onAllNodesWithText("Último kilometraje registrado", substring = true).fetchSemanticsNodes()
            if (nodes.isNotEmpty()) {
                val text = nodes[0].config[SemanticsProperties.Text].map { it.text }.joinToString("")
                val lastKm = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                (lastKm + 289).toString()
            } else {
                "15500" // Fallback
            }
        } catch (e: Exception) {
            "15500"
        }
    }

    private fun handleKmAlerts() {
        var attempts = 0
        val maxAttempts = 3
        
        while (attempts < maxAttempts) {
            attempts++
            
            // Wait a bit for the UI to stabilize and dialog to appear
            Thread.sleep(UI_STABILIZATION_DELAY)

            // Look for common texts in KM alerts (Red or Yellow)
            val redAlertTitle = composeTestRule.onAllNodesWithText("Kilometraje Incorrecto", substring = true, useUnmergedTree = true).fetchSemanticsNodes()
            val yellowAlertTitle = composeTestRule.onAllNodesWithText("Verificación de Kilometraje", substring = true, useUnmergedTree = true).fetchSemanticsNodes()
            val confirmBtn = composeTestRule.onAllNodesWithText("Confirmar Excepción", substring = true, useUnmergedTree = true).fetchSemanticsNodes()
            val correctBtn = composeTestRule.onAllNodesWithText("Corregir", substring = true, useUnmergedTree = true).fetchSemanticsNodes()

            if (redAlertTitle.isEmpty() && yellowAlertTitle.isEmpty() && confirmBtn.isEmpty() && correctBtn.isEmpty()) {
                // No alert elements found
                break
            }

            // 1. Handle Yellow (Warning)
            if (confirmBtn.isNotEmpty()) {
                composeTestRule.onNodeWithText("Confirmar Excepción", substring = true, useUnmergedTree = true).performClick()
                Thread.sleep(UI_STABILIZATION_DELAY)
                break // Success
            }

            // 2. Handle Red (Error) or fallback to Correcting
            if (correctBtn.isNotEmpty()) {
                composeTestRule.onNodeWithText("Corregir", substring = true, useUnmergedTree = true).performClick()
                Thread.sleep(UI_STABILIZATION_DELAY)
                
                // Add an extra digit to ensure it's higher than minimum
                composeTestRule.onNodeWithTag("moto_km_field")
                    .performTextReplacement(TEST_KILOMETRAJE + "0")
                
                // Trigger blur again
                composeTestRule.onNodeWithTag("moto_km_field").performImeAction()
                composeTestRule.onNodeWithTag("moto_form_scroll").performClick()
                continue
            }
        }
    }
}
