package com.jhon.micontroldidi.ui.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jhon.micontroldidi.domain.PeriodoDashboard
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun muestraIngresosFormateadosEnCOP() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    ingresos = 150000,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithTag("dashboard_tarjeta_ingresos").assertIsDisplayed()
        composeTestRule.onNodeWithText("$ 150.000").assertIsDisplayed()
    }

    @Test
    fun muestraGastosFormateadosEnCOP() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    gastos = 75000,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithTag("dashboard_tarjeta_gastos").assertIsDisplayed()
        composeTestRule.onNodeWithText("$ 75.000").assertIsDisplayed()
    }

    @Test
    fun muestraGananciaNetaFormateadaEnCOP() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    ingresos = 200000,
                    gastos = 50000,
                    gananciaNeta = 150000,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithTag("dashboard_tarjeta_ganancia").assertIsDisplayed()
        composeTestRule.onNodeWithText("$ 150.000").assertIsDisplayed()
    }

    @Test
    fun gananciaNegativaMuestraSignoNegativo() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    ingresos = 30000,
                    gastos = 80000,
                    gananciaNeta = -50000,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        // CurrencyFormatter produce "$ -50.000" para valores negativos
        composeTestRule.onNodeWithText("$ -50.000").assertIsDisplayed()
    }

    @Test
    fun estadoCargandoMuestraTexto() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(cargando = true),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithText("Cargando\u2026").assertIsDisplayed()
    }

    @Test
    fun estadoErrorMuestraMensaje() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    cargando = false,
                    mensajeError = "Error simulado de prueba"
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithTag("dashboard_error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error simulado de prueba").assertIsDisplayed()
    }

    @Test
    fun estadoSinMovimientosMuestraMensaje() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    ingresos = 0,
                    gastos = 0,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithTag("dashboard_sin_datos").assertIsDisplayed()
    }

    @Test
    fun losTresChipsDePeriodoSonVisibles() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithTag("chip_periodo_DIA").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chip_periodo_SEMANA").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chip_periodo_MES").assertIsDisplayed()
    }

    @Test
    fun chipDelPeriodoActivoEstaSeleccionado() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.SEMANA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithTag("chip_periodo_SEMANA").assertIsSelected()
        composeTestRule.onNodeWithTag("chip_periodo_DIA").assertIsNotSelected()
        composeTestRule.onNodeWithTag("chip_periodo_MES").assertIsNotSelected()
    }

    @Test
    fun alPulsarChipSEMANAseInvocanCallbackConSEMANA() {
        var periodoRecibido: PeriodoDashboard? = null
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = { periodoRecibido = it }
            )
        }

        composeTestRule.onNodeWithTag("chip_periodo_SEMANA").performClick()
        assertEquals(PeriodoDashboard.SEMANA, periodoRecibido)
    }

    @Test
    fun alPulsarChipMESseInvocaCallbackConMES() {
        var periodoRecibido: PeriodoDashboard? = null
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = { periodoRecibido = it }
            )
        }

        composeTestRule.onNodeWithTag("chip_periodo_MES").performClick()
        assertEquals(PeriodoDashboard.MES, periodoRecibido)
    }

    @Test
    fun estadoMixtoMuestraValoresCorrectos() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    ingresos = 100000,
                    gastos = 40000,
                    gananciaNeta = 60000,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithText("$ 100.000").assertIsDisplayed()
        composeTestRule.onNodeWithText("$ 40.000").assertIsDisplayed()
        composeTestRule.onNodeWithText("$ 60.000").assertIsDisplayed()
    }

    @Test
    fun fabsDeAccesoRapidoEstanVisibles() {
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    ingresos = 0,
                    gastos = 0,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {}
            )
        }

        composeTestRule.onNodeWithTag("fab_registrar_viaje_dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fab_registrar_gasto_dashboard").assertIsDisplayed()
    }

    @Test
    fun fabViajeInvocaCallbackAlPulsar() {
        var pulsado = false
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    ingresos = 0,
                    gastos = 0,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {},
                onNavegarARegistrarViaje = { pulsado = true }
            )
        }

        composeTestRule.onNodeWithTag("fab_registrar_viaje_dashboard").performClick()
        Assert.assertTrue(pulsado)
    }

    @Test
    fun fabGastoInvocaCallbackAlPulsar() {
        var pulsado = false
        composeTestRule.setContent {
            DashboardScreen(
                uiState = DashboardUiState(
                    ingresos = 0,
                    gastos = 0,
                    cargando = false,
                    periodoSeleccionado = PeriodoDashboard.DIA
                ),
                onPeriodoSeleccionado = {},
                onNavegarARegistrarGasto = { pulsado = true }
            )
        }

        composeTestRule.onNodeWithTag("fab_registrar_gasto_dashboard").performClick()
        Assert.assertTrue(pulsado)
    }
}
