package com.jhon.micontroldidi.ui.viaje

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ViajeListaScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cargando_muestraCargando() {
        composeTestRule.setContent {
            ListaViajesScreen(
                state = ViajeUiState(cargando = true)
            )
        }

        composeTestRule.onNodeWithText("Cargando\u2026").assertIsDisplayed()
    }

    @Test
    fun error_muestraEstadoErrorYmensaje() {
        composeTestRule.setContent {
            ListaViajesScreen(
                state = ViajeUiState(
                    cargando = false,
                    mensajeError = "Error de prueba"
                )
            )
        }

        composeTestRule.onNodeWithTag("estado_error_viajes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error de prueba").assertIsDisplayed()
    }

    @Test
    fun error_noMuestraLista() {
        composeTestRule.setContent {
            ListaViajesScreen(
                state = ViajeUiState(
                    cargando = false,
                    mensajeError = "Error"
                )
            )
        }

        assertTrue(
            "lista_viajes no debe existir cuando hay error",
            composeTestRule.onAllNodesWithTag("lista_viajes").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun error_noMuestraEstadoVacio() {
        composeTestRule.setContent {
            ListaViajesScreen(
                state = ViajeUiState(
                    cargando = false,
                    mensajeError = "Error",
                    viajes = emptyList()
                )
            )
        }

        assertTrue(
            "estado_vacio_viajes no debe existir cuando hay error",
            composeTestRule.onAllNodesWithTag("estado_vacio_viajes").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun listaVacia_sinFiltro_muestraEstadoVacio() {
        composeTestRule.setContent {
            ListaViajesScreen(
                state = ViajeUiState(
                    cargando = false,
                    mensajeError = null,
                    viajes = emptyList()
                )
            )
        }

        composeTestRule.onNodeWithTag("estado_vacio_viajes").assertIsDisplayed()
    }

    @Test
    fun filtroSinResultados_muestraEstadoFiltroVacio() {
        composeTestRule.setContent {
            ListaViajesScreen(
                state = ViajeUiState(
                    cargando = false,
                    mensajeError = null,
                    filtroActivo = true,
                    mensajeFiltroVacio = "No hay viajes en el rango seleccionado"
                )
            )
        }

        composeTestRule.onNodeWithTag("estado_vacio_filtro").assertIsDisplayed()
    }

    @Test
    fun conDatos_muestraLista() {
        composeTestRule.setContent {
            ListaViajesScreen(
                state = ViajeUiState(
                    cargando = false,
                    mensajeError = null,
                    viajes = listOf(
                        ViajeEntity(
                            id = 1,
                            fechaHora = 0L,
                            valor = 15000,
                            propina = 2000
                        )
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag("lista_viajes").assertIsDisplayed()
    }
}
