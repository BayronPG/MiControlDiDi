package com.jhon.micontroldidi.ui.gasto

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GastoListaScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cargando_muestraCargando() {
        composeTestRule.setContent {
            ListaGastosScreen(
                state = GastoUiState(cargando = true)
            )
        }

        composeTestRule.onNodeWithText("Cargando\u2026").assertIsDisplayed()
    }

    @Test
    fun errorGlobal_muestraEstadoErrorYMensaje() {
        composeTestRule.setContent {
            ListaGastosScreen(
                state = GastoUiState(
                    cargando = false,
                    mensajeErrorCarga = "Error de prueba"
                )
            )
        }

        composeTestRule.onNodeWithTag("estado_error_gastos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error de prueba").assertIsDisplayed()
    }

    @Test
    fun errorGlobal_noMuestraLista() {
        composeTestRule.setContent {
            ListaGastosScreen(
                state = GastoUiState(
                    cargando = false,
                    mensajeErrorCarga = "Error"
                )
            )
        }

        assertTrue(
            "lista_gastos no debe existir cuando hay error global",
            composeTestRule.onAllNodesWithTag("lista_gastos").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun errorGlobal_noMuestraEstadoVacio() {
        composeTestRule.setContent {
            ListaGastosScreen(
                state = GastoUiState(
                    cargando = false,
                    mensajeErrorCarga = "Error",
                    gastos = emptyList()
                )
            )
        }

        assertTrue(
            "estado_vacio_gastos no debe existir cuando hay error global",
            composeTestRule.onAllNodesWithTag("estado_vacio_gastos").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun errorGlobal_noMuestraFiltroVacio() {
        composeTestRule.setContent {
            ListaGastosScreen(
                state = GastoUiState(
                    cargando = false,
                    mensajeErrorCarga = "Error",
                    filtroActivo = true,
                    mensajeFiltroVacio = "Sin resultados"
                )
            )
        }

        assertTrue(
            "estado_vacio_filtro_gastos no debe existir cuando hay error global",
            composeTestRule.onAllNodesWithTag("estado_vacio_filtro_gastos").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun listaVacia_sinFiltro_muestraEstadoVacio() {
        composeTestRule.setContent {
            ListaGastosScreen(
                state = GastoUiState(
                    cargando = false,
                    mensajeErrorCarga = null,
                    gastos = emptyList(),
                    categorias = listOf(CategoriaGastoEntity(id = 1, nombre = "Gasolina"))
                )
            )
        }

        composeTestRule.onNodeWithTag("estado_vacio_gastos").assertIsDisplayed()
    }

    @Test
    fun filtroSinResultados_muestraEstadoFiltroVacio() {
        composeTestRule.setContent {
            ListaGastosScreen(
                state = GastoUiState(
                    cargando = false,
                    mensajeErrorCarga = null,
                    filtroActivo = true,
                    mensajeFiltroVacio = "No hay gastos en el rango seleccionado"
                )
            )
        }

        composeTestRule.onNodeWithTag("estado_vacio_filtro_gastos").assertIsDisplayed()
    }

    @Test
    fun conDatos_muestraLista() {
        composeTestRule.setContent {
            ListaGastosScreen(
                state = GastoUiState(
                    cargando = false,
                    mensajeErrorCarga = null,
                    gastos = listOf(
                        GastoConCategoria(
                            id = 1,
                            fechaHora = 0L,
                            categoriaId = 1,
                            nombreCategoria = "Gasolina",
                            valor = 15000,
                            descripcion = ""
                        )
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag("lista_gastos").assertIsDisplayed()
    }
}
