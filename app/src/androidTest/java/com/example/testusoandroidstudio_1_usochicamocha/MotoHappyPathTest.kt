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
class MotoHappyPathTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    companion object {
        private const val SPLASH_TIMEOUT = 30000L
        private const val NAVIGATION_TIMEOUT = 25000L
        private const val UI_STABILIZATION_DELAY = 1000L
        
        private const val TEST_USERNAME = "admin"
        private const val TEST_PASSWORD = "admin123"
        private const val TEST_KILOMETRAJE = "1500"
        private const val TEST_OBSERVATIONS = "Prueba automatizada de Moto - Todo en orden"
    }

    @Test
    fun moto_complete_flow_test() {
        // 1. LOGIN
        performLogin()

        // 2. NAVIGATE TO MOTO INSPECTION
        composeTestRule.onNodeWithText("Inspección de Motos").performClick()
        
        // Wait for screen to load
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Inspección Motocicleta").fetchSemanticsNodes().isNotEmpty()
        }

        // 3. FILL FORM
        
        // Select Plate
        composeTestRule.onNodeWithText("Seleccione La PLACA de Su Motocicleta (*)").performClick()
        // Wait for dropdown and select first option (usually contains - or a plate)
        // Note: In DropdownField, we use ExposedDropdownMenu which might show items with text
        // For simplicity in E2E, we look for any text that looks like a plate or first item
        Thread.sleep(UI_STABILIZATION_DELAY)
        composeTestRule.onAllNodes(hasClickAction())[1].performClick() // Fallback to click something if list is shown

        // Select Unit
        composeTestRule.onNodeWithText("Seleccione la UNIDAD a la que Pertenece (*)").performClick()
        Thread.sleep(UI_STABILIZATION_DELAY)
        composeTestRule.onAllNodes(hasClickAction())[1].performClick()

        // Fill Kilometraje
        composeTestRule.onNodeWithText("Escriba el KILOMETRAJE Actual de la Moto (*)")
            .performTextInput(TEST_KILOMETRAJE)

        // Select General Status
        composeTestRule.onNodeWithText("Óptimo").performClick()

        // Fill Observations
        composeTestRule.onNodeWithText("Observaciones y/o Aspectos a Revisar (*)")
            .performTextInput(TEST_OBSERVATIONS)

        // 4. SAVE
        composeTestRule.onNodeWithText("Guardar").performClick()

        // 5. VERIFY SUCCESS
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Guardado Exitoso").fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Aceptar").performClick()
        
        // Should be back to main menu
        composeTestRule.onNodeWithText("Menú Principal").assertIsDisplayed()
    }

    private fun performLogin() {
        // Wait for splash
        composeTestRule.waitUntil(timeoutMillis = SPLASH_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Usuario").performTextInput(TEST_USERNAME)
        composeTestRule.onNodeWithText("Contraseña").performTextInput(TEST_PASSWORD)
        composeTestRule.onNodeWithText("Ingresar").performClick()

        // Wait for main screen
        composeTestRule.waitUntil(timeoutMillis = NAVIGATION_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Menú Principal").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
