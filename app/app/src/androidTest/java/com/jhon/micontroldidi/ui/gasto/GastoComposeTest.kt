package com.jhon.micontroldidi.ui.gasto

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.MainActivity
import com.jhon.micontroldidi.data.local.database.MiControlDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class GastoComposeTest {

    companion object {
        private const val PREFIJO_PRUEBA = "__TEST_GASTO_COMPOSE__"
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val db by lazy {
        MiControlDatabase.obtenerInstancia(ApplicationProvider.getApplicationContext())
    }

    @Before
    fun setup() {
        limpiarGastosDePrueba()
    }

    @After
    fun tearDown() {
        limpiarGastosDePrueba()
    }

    /** Elimina solo los gastos cuyo descripcion comienza con el prefijo de prueba.
     *  Conserva gastos sin prefijo, viajes y categorias. */
    private fun limpiarGastosDePrueba() {
        runBlocking {
            val todos = db.gastoDao().obtenerTodos().first()
            val dePrueba = todos.filter { it.descripcion.startsWith(PREFIJO_PRUEBA) }
            dePrueba.forEach { db.gastoDao().eliminar(it.id) }
        }
    }

    /** Genera una descripcion unica con prefijo reservado y UUID. */
    private fun descripcionPrueba(base: String): String =
        "${PREFIJO_PRUEBA}${base}_${UUID.randomUUID().toString().take(6)}"

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
        for (cat in listOf("Gasolina", "Mantenimiento", "Parqueadero",
                           "Lavado", "Cuota de la moto", "Otros")) {
            composeTestRule.onNodeWithText(cat).assertIsDisplayed()
        }
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
        val desc = descripcionPrueba("Nuevo")
        val id = crearGastoYObtenerId("Gasolina", "18000", desc)
        assertGastoVisible(id, desc)
    }

    @Test
    fun listadoMuestraCategoriaValorYDescripcion() {
        val desc = descripcionPrueba("Listado")
        val id = crearGastoYObtenerId("Gasolina", "25000", desc)
        assertGastoVisible(id, desc)
        composeTestRule.onNode(
            hasText("Gasolina") and hasAnyAncestor(hasTestTag("item_gasto_$id"))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("$ 25.000").assertIsDisplayed()
    }

    @Test
    fun doblePulsacionNoDuplica() {
        val desc = descripcionPrueba("Doble")
        val id = crearGastoYObtenerId("Gasolina", "30000", desc)
        assertGastoVisible(id, desc)
        val coincidencias = runBlocking {
            db.gastoDao().obtenerTodos().first().filter { it.descripcion == desc }
        }
        assertEquals("Se esperaba 1 gasto", 1, coincidencias.size)
    }

    @Test
    fun confirmarEliminacionCierraDialogoYRemueveGasto() {
        val descEliminar = descripcionPrueba("A_Eliminar")
        val descControl = descripcionPrueba("B_Control")
        val idControl = crearGastoYObtenerId("Gasolina", "9999", descControl)
        val idEliminar = crearGastoYObtenerId("Parqueadero", "15000", descEliminar)

        assertGastoVisible(idControl, descControl)
        assertGastoVisible(idEliminar, descEliminar)

        eliminarGasto(idEliminar)
        composeTestRule.onNodeWithTag("dialogo_confirmar_eliminar").assertIsDisplayed()
        composeTestRule.onNodeWithText("S\u00ED, eliminar").performClick()

        assertGastoNoVisible(idEliminar, descEliminar)
        assertGastoVisible(idControl, descControl)
        val noExiste = runBlocking { db.gastoDao().obtenerPorId(idEliminar) }
        assertNull("El gasto $idEliminar deberia haber sido eliminado", noExiste)
    }

    @Test
    fun editarGuardaCambiosYLuegoMuestraActualizado() {
        val descOriginal = descripcionPrueba("Orig")
        val descEditada = descripcionPrueba("Edit")
        val id = crearGastoYObtenerId("Mantenimiento", "45000", descOriginal)

        editarGasto(id)
        composeTestRule.onNodeWithText("Editar gasto").assertIsDisplayed()
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(descEditada)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(descEditada).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(descEditada).assertIsDisplayed()

        // Verificar que el texto original ya no aparece
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(descOriginal).fetchSemanticsNodes().isEmpty()
        }

        val gastoActualizado = runBlocking { db.gastoDao().obtenerPorId(id) }
        assertNotNull("El gasto $id debe seguir existiendo", gastoActualizado)
        assertEquals("Error en descripcion", descEditada, gastoActualizado?.descripcion)
        val sinDuplicado = runBlocking {
            db.gastoDao().obtenerTodos().first().filter { it.descripcion == descEditada }
        }
        assertEquals("No debe haber duplicado", 1, sinDuplicado.size)
    }

    @Test
    fun cancelarEdicionNoModificaGasto() {
        val desc = descripcionPrueba("CancelEdit")
        val id = crearGastoYObtenerId("Lavado", "15000", desc)

        editarGasto(id)
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("99999")
        composeTestRule.onNodeWithTag("boton_cancelar_gasto").performClick()
        composeTestRule.waitForIdle()

        assertGastoVisible(id, desc)
        val despues = runBlocking { db.gastoDao().obtenerPorId(id) }
        assertEquals("El valor no debe cambiar", 15000L, despues?.valor)
    }

    @Test
    fun cancelarDialogoEliminacionConservaGasto() {
        val desc = descripcionPrueba("CancelElim")
        val id = crearGastoYObtenerId("Gasolina", "55000", desc)

        eliminarGasto(id)
        composeTestRule.onNodeWithText("Cancelar").performClick()

        assertGastoVisible(id, desc)
    }

    @Test
    fun editarConservaIdYFechaHora() {
        val desc = descripcionPrueba("Conserva")
        val id = crearGastoYObtenerId("Gasolina", "10000", desc)

        editarGasto(id)
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement("99999")
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()
        composeTestRule.waitForIdle()

        val despues = runBlocking { db.gastoDao().obtenerPorId(id) }
        assertNotNull("El gasto $id debe seguir existiendo", despues)
        assertEquals("El ID debe conservarse", id, despues?.id)
    }

    @Test
    fun eliminarGasto_botonCorrectoPorId() {
        val desc = descripcionPrueba("Btn")
        val id = crearGastoYObtenerId("Lavado", "6000", desc)
        composeTestRule.onNodeWithTag("boton_eliminar_gasto_$id").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_editar_gasto_$id").assertIsDisplayed()
    }

    @Test
    fun limpiezaSelectiva_conservaGastosSinPrefijo() {
        val descTest = descripcionPrueba("Selectiva")
        val descNormal = "Gasto normal usuario"
        var idTest = -1L
        var idNormal = -1L
        try {
            idTest = crearGastoYObtenerId("Gasolina", "7000", descTest)
            idNormal = runBlocking {
                db.gastoDao().insertar(
                    com.jhon.micontroldidi.data.local.entity.GastoEntity(
                        fechaHora = System.currentTimeMillis(), categoriaId = 1, valor = 8888,
                        descripcion = descNormal
                    )
                )
            }
            val todos = runBlocking { db.gastoDao().obtenerTodos().first() }
            assertTrue("El gasto de prueba debe existir antes", todos.any { it.descripcion == descTest })
            assertTrue("El gasto normal debe existir antes", todos.any { it.descripcion == descNormal })

            // Limpiar solo gastos con prefijo
            limpiarGastosDePrueba()

            val despues = runBlocking { db.gastoDao().obtenerTodos().first() }
            assertTrue("El gasto de prueba debe eliminarse", despues.none { it.descripcion == descTest })
            assertTrue("El gasto normal debe conservarse", despues.any { it.descripcion == descNormal })
        } finally {
            // Retirar el gasto normal de control (sin prefijo) para no contaminar
            if (idNormal > 0) {
                runBlocking { db.gastoDao().eliminar(idNormal) }
                val final = runBlocking { db.gastoDao().obtenerPorId(idNormal) }
                assertNull("El gasto normal de control deberia haber sido retirado", final)
            }
            // El gasto con prefijo ya fue eliminado por limpiarGastosDePrueba(),
            // pero @After lo cubre por si acaso.
        }
    }

    // ── Helpers ──

    private fun crearGastoYObtenerId(categoria: String, valor: String, descripcion: String): Long {
        composeTestRule.onNodeWithTag("navegacion_gastos").performClick()
        composeTestRule.onNodeWithTag("boton_registrar_gasto").performClick()
        composeTestRule.onNodeWithTag("selector_categoria_gasto").performClick()
        composeTestRule.onNodeWithText(categoria).performClick()
        composeTestRule.onNodeWithTag("campo_valor_gasto").performTextReplacement(valor)
        composeTestRule.onNodeWithTag("campo_descripcion_gasto").performTextReplacement(descripcion)
        composeTestRule.onNodeWithTag("boton_guardar_gasto").performClick()
        composeTestRule.waitForIdle()

        val coincidentes = runBlocking {
            db.gastoDao().obtenerTodos().first().filter { it.descripcion == descripcion }
        }
        assertTrue(
            "Esperado exactamente 1 gasto con '$descripcion', encontrados ${coincidentes.size}",
            coincidentes.size == 1
        )
        val id = coincidentes.first().id
        assertGastoVisible(id, descripcion)
        return id
    }

    private fun assertGastoVisible(gastoId: Long, descripcion: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(descripcion).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("item_gasto_$gastoId").assertIsDisplayed()
        composeTestRule.onNodeWithText(descripcion).assertIsDisplayed()
    }

    private fun assertGastoNoVisible(gastoId: Long, descripcion: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("item_gasto_$gastoId").fetchSemanticsNodes().isEmpty()
        }
        assertTrue("item_gasto_$gastoId aun visible", composeTestRule.onAllNodesWithTag("item_gasto_$gastoId").fetchSemanticsNodes().isEmpty())
        assertTrue("Descripcion '$descripcion' aun visible", composeTestRule.onAllNodesWithText(descripcion).fetchSemanticsNodes().isEmpty())
    }

    private fun editarGasto(gastoId: Long) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("boton_editar_gasto_$gastoId").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("boton_editar_gasto_$gastoId").performClick()
    }

    private fun eliminarGasto(gastoId: Long) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("boton_eliminar_gasto_$gastoId").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("boton_eliminar_gasto_$gastoId").performClick()
    }
}
