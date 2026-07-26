package com.jhon.micontroldidi.ui.stats

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StatsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cargandoMuestraStatsCargando() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(cargando = true)
            )
        }

        composeTestRule.onNodeWithTag("stats_cargando").assertIsDisplayed()
    }

    @Test
    fun errorMuestraStatsErrorYMensaje() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = "Error de prueba"
                )
            )
        }

        composeTestRule.onNodeWithTag("stats_error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error de prueba").assertIsDisplayed()
    }

    @Test
    fun errorNoMuestraStatsContenido() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = "Error"
                )
            )
        }

        composeTestRule.onNodeWithTag("stats_error").assertIsDisplayed()
        org.junit.Assert.assertTrue(
            "stats_contenido no debe existir cuando hay error",
            composeTestRule.onAllNodesWithTag("stats_contenido").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun valoresVaciosMuestranStatsSinDatos() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = null,
                    ingresosActual = 0L,
                    gastosActual = 0L,
                    ingresosAnterior = 0L,
                    gastosAnterior = 0L
                )
            )
        }

        composeTestRule.onNodeWithTag("stats_sin_datos").assertIsDisplayed()
    }

    @Test
    fun vacioNoMuestraTarjetasComoDatosValidos() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = null,
                    ingresosActual = 0L,
                    gastosActual = 0L,
                    ingresosAnterior = 0L,
                    gastosAnterior = 0L
                )
            )
        }

        composeTestRule.onNodeWithTag("stats_sin_datos").assertIsDisplayed()
        org.junit.Assert.assertTrue(
            "stats_card_actual no debe existir en vacío",
            composeTestRule.onAllNodesWithTag("stats_card_actual").fetchSemanticsNodes().isEmpty()
        )
        org.junit.Assert.assertTrue(
            "stats_card_anterior no debe existir en vacío",
            composeTestRule.onAllNodesWithTag("stats_card_anterior").fetchSemanticsNodes().isEmpty()
        )
        org.junit.Assert.assertTrue(
            "stats_card_diferencia no debe existir en vacío",
            composeTestRule.onAllNodesWithTag("stats_card_diferencia").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun datosPeriodoActualSeMuestran() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = null,
                    ingresosActual = 150000,
                    gastosActual = 50000,
                    gananciaNetaActual = 100000,
                    cantidadViajesActual = 3,
                    ingresosAnterior = 0L,
                    gastosAnterior = 0L,
                    gananciaNetaAnterior = 0L
                )
            )
        }

        composeTestRule.onNodeWithTag("stats_card_actual").assertIsDisplayed()
        composeTestRule.onNodeWithText("$ 150.000").assertIsDisplayed()
    }

    @Test
    fun datosPeriodoAnteriorSeMuestran() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = null,
                    ingresosActual = 150000,
                    gastosActual = 50000,
                    gananciaNetaActual = 100000,
                    ingresosAnterior = 120000,
                    gastosAnterior = 40000,
                    gananciaNetaAnterior = 80000
                )
            )
        }

        composeTestRule.onNodeWithTag("stats_card_anterior").assertIsDisplayed()
    }

    @Test
    fun diferenciaSeMuestra() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = null,
                    ingresosActual = 150000,
                    gastosActual = 50000,
                    gananciaNetaActual = 100000,
                    ingresosAnterior = 100000,
                    gastosAnterior = 30000,
                    gananciaNetaAnterior = 70000
                )
            )
        }

        composeTestRule.onNodeWithTag("stats_card_diferencia").assertIsDisplayed()
    }

    @Test
    fun diferenciaCeroConDatosRealesNoSeInterpretaComoVacio() {
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = null,
                    ingresosActual = 100000,
                    gastosActual = 50000,
                    gananciaNetaActual = 50000,
                    cantidadViajesActual = 2,
                    ingresosAnterior = 100000,
                    gastosAnterior = 50000,
                    gananciaNetaAnterior = 50000
                )
            )
        }

        org.junit.Assert.assertTrue(
            "stats_sin_datos no debe existir con datos reales",
            composeTestRule.onAllNodesWithTag("stats_sin_datos").fetchSemanticsNodes().isEmpty()
        )
        composeTestRule.onNodeWithTag("stats_card_actual").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stats_card_diferencia").assertIsDisplayed()
    }

    @Test
    fun selectorPeriodoFunciona() {
        var periodoRecibido: StatsPeriodo? = null
        composeTestRule.setContent {
            StatsScreen(
                state = StatsUiState(
                    cargando = false,
                    mensajeError = null,
                    periodoSeleccionado = StatsPeriodo.HOY,
                    ingresosActual = 50000,
                    gastosActual = 20000,
                    gananciaNetaActual = 30000,
                    ingresosAnterior = 10000,
                    gastosAnterior = 5000,
                    gananciaNetaAnterior = 5000
                ),
                onPeriodoSeleccionado = { p -> periodoRecibido = p }
            )
        }

        composeTestRule.onNodeWithTag("chip_stats_SEMANA").performClick()
        assertEquals(StatsPeriodo.SEMANA, periodoRecibido)
    }
}
