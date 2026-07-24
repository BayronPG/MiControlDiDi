package com.jhon.micontroldidi.ui.viaje

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViajeComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun listaVacia_muestraBotonRegistrar() {
        composeTestRule.onNodeWithTag("boton_registrar_viaje").assertIsDisplayed()
    }

    @Test
    fun pulsarRegistrar_abreFormulario() {
        composeTestRule.onNodeWithTag("boton_registrar_viaje").performClick()
        composeTestRule.onNodeWithTag("campo_valor_viaje").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_guardar_viaje").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_cancelar_viaje").assertIsDisplayed()
    }

    @Test
    fun valorVacio_botonGuardarDeshabilitado() {
        composeTestRule.onNodeWithTag("boton_registrar_viaje").performClick()
        composeTestRule.onNodeWithTag("boton_guardar_viaje").assertIsNotEnabled()
    }

    @Test
    fun valorCero_botonGuardarDeshabilitado() {
        composeTestRule.onNodeWithTag("boton_registrar_viaje").performClick()
        composeTestRule.onNodeWithTag("campo_valor_viaje").performTextReplacement("0")
        composeTestRule.onNodeWithTag("boton_guardar_viaje").assertIsNotEnabled()
    }

    @Test
    fun propinaNegativa_muestraError() {
        composeTestRule.onNodeWithTag("boton_registrar_viaje").performClick()
        composeTestRule.onNodeWithTag("campo_valor_viaje").performTextReplacement("10000")
        composeTestRule.onNodeWithTag("campo_propina_viaje").performTextReplacement("-500")
        composeTestRule.onNodeWithText("La propina no puede ser negativa").assertIsDisplayed()
    }

    @Test
    fun guardarViajeValido_regresaAListaYMuestraViaje() {
        val observacion = "Viaje unico"
        composeTestRule.onNodeWithTag("boton_registrar_viaje").performClick()

        composeTestRule.onNodeWithTag("campo_valor_viaje").performTextReplacement("15000")
        composeTestRule.onNodeWithTag("campo_propina_viaje").performTextReplacement("3000")
        composeTestRule.onNodeWithTag("campo_observacion_viaje").performTextReplacement(observacion)
        composeTestRule.onNodeWithTag("boton_guardar_viaje").performClick()

        composeTestRule.onNodeWithText(observacion).assertIsDisplayed()
    }

    @Test
    fun cancelar_regresaAListaSinGuardar() {
        composeTestRule.onNodeWithTag("boton_registrar_viaje").performClick()
        composeTestRule.onNodeWithTag("campo_valor_viaje").performTextReplacement("99999")
        composeTestRule.onNodeWithTag("boton_cancelar_viaje").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_viaje").assertIsDisplayed()
    }

    @Test
    fun guardarViaje_muestraDetalles() {
        val observacion = "Detalle verificado"
        composeTestRule.onNodeWithTag("boton_registrar_viaje").performClick()

        composeTestRule.onNodeWithTag("campo_valor_viaje").performTextReplacement("25000")
        composeTestRule.onNodeWithTag("campo_propina_viaje").performTextReplacement("5000")
        composeTestRule.onNodeWithTag("campo_observacion_viaje").performTextReplacement(observacion)
        composeTestRule.onNodeWithTag("boton_guardar_viaje").performClick()

        composeTestRule.onNodeWithText(observacion).assertIsDisplayed()
    }
}
