package com.jhon.micontroldidi.ui.gasto

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
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
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("18000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement("Nuevo gasto unico")
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText("Nuevo gasto unico").assertIsDisplayed()
    }

    @Test
    fun listadoMuestraCategoriaValorYDescripcion() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("25000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement("Descripcion lista")
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        // Verificar que aparece al menos una vez en la lista
        composeTestRule.onAllNodesWithText("Descripcion lista")[0].assertIsDisplayed()
        composeTestRule.onNodeWithText("Gasolina").assertIsDisplayed()
        composeTestRule.onNodeWithText("$ 25.000").assertIsDisplayed()
    }

    @Test
    fun doblePulsacionNoDuplica() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("30000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement("Gasto unico doble")
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText("Gasto unico doble").assertIsDisplayed()
    }

    @Test
    fun botonEditar_abreFormularioConDatos() {
        val desc = "Para editar boton"
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("50000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(desc)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(desc).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("boton_editar_gasto")[0].performClick()

        composeTestRule.onNodeWithText("Editar gasto").assertIsDisplayed()
        composeTestRule.onNodeWithTag("campo_valor_gasto").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_cancelar_gasto").performClick()
    }

    @Test
    fun eliminarGasto_conConfirmacion_remueveDeLista() {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Parqueadero").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("15000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement("Para eliminar test")
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText("Para eliminar test").assertIsDisplayed()

        // Usar el primer botón de eliminar disponible
        composeTestRule.onAllNodesWithTag("boton_eliminar_gasto")[0].performClick()

        composeTestRule.onNodeWithTag("dialogo_confirmar_eliminar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sí, eliminar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar", substring = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("Sí, eliminar").performClick()
    }

    @Test
    fun editarGuardaCambiosYLuegoMuestraActualizado() {
        val original = "Gasto original edicion"
        val editado = "Gasto editado compose"
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Mantenimiento").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("45000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(original)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(original).assertIsDisplayed()

        composeTestRule.onAllNodesWithTag("boton_editar_gasto")[0].performClick()
        composeTestRule.onNodeWithText("Editar gasto").assertIsDisplayed()

        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(editado)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(editado).assertIsDisplayed()
    }

    @Test
    fun cancelarEdicionNoModificaGasto() {
        val desc = "No modificar cancel"
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Lavado").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("15000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(desc)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(desc).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("boton_editar_gasto")[0].performClick()

        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("99999")
        composeTestRule.onNodeWithTag("boton_cancelar_gasto").performClick()

        // El valor original debe seguir visible
        composeTestRule.onNodeWithText(desc).assertIsDisplayed()
    }

    @Test
    fun cancelarDialogoEliminacionConservaGasto() {
        val desc = "Conservar gasto cancel"
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("55000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(desc)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(desc).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("boton_eliminar_gasto")[0].performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()

        composeTestRule.onNodeWithText(desc).assertIsDisplayed()
    }

    @Test
    fun dobleConfirmacionEliminacionNoDuplica() {
        val desc = "Doble eliminar test"
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()

        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText("Gasolina").performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("13000")
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(desc)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()

        composeTestRule.onNodeWithText(desc).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("boton_eliminar_gasto")[0].performClick()
        composeTestRule.onNodeWithText("Sí, eliminar").performClick()
    }
}
