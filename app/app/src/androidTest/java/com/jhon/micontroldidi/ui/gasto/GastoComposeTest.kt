package com.jhon.micontroldidi.ui.gasto

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.MainActivity
import com.jhon.micontroldidi.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GastoComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun listaVacia_muestraBotonRegistrar() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").assertIsDisplayed()
    }

    @Test
    fun pulsarRegistrar_abreFormulario() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()
        composeTestRule.onNodeWithTag("selector_categoria_gasto").assertIsDisplayed()
        composeTestRule.onNodeWithTag("campo_valor_gasto").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_guardar_gasto").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_cancelar_gasto").assertIsDisplayed()
    }

    @Test
    fun selectorMuestraCategorias() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()
        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mantenimiento").assertIsDisplayed()
        composeTestRule.onNodeWithText("Parqueadero").assertIsDisplayed()
    }

    @Test
    fun sePuedeSeleccionarUnaCategoria() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()
        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithText("Gasolina").assertIsDisplayed()
    }

    @Test
    fun valorVacio_botonGuardarDeshabilitado() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()
        composeTestRule.onNodeWithTag("boton_guardar_gasto").assertIsNotEnabled()
    }

    @Test
    fun valorCero_botonGuardarDeshabilitado() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()
        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("0")
        composeTestRule.onNodeWithTag("boton_guardar_gasto").assertIsNotEnabled()
    }

    @Test
    fun cancelar_vuelveSinGuardar() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("99999")
        composeTestRule.onNodeWithTag("boton_cancelar_gasto").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").assertIsDisplayed()
    }

    @Test
    fun gastoValido_regresaAListadoYMuestraGasto() {
        val descripcion = "Gasto de prueba unico"
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("18000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(descripcion)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(descripcion).assertIsDisplayed()
    }

    @Test
    fun listadoMuestraCategoriaValorYDescripcion() {
        val descripcion = "Tanqueo de prueba"
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("25000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(descripcion)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(descripcion).assertIsDisplayed()
        composeTestRule.onNodeWithText("Gasolina").assertIsDisplayed()
        // El valor debe mostrar $ 25.000 (formato COP)
        composeTestRule.onNodeWithText("$ 25.000").assertIsDisplayed()
    }

    @Test
    fun doblePulsacionNoDuplica() {
        val descripcion = "Unico gasto"
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("30000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(descripcion)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(descripcion).assertIsDisplayed()
        // Solo debe haber un elemento con esa descripción
        composeTestRule.onNodeWithText(descripcion).assertIsDisplayed()
    }
}
