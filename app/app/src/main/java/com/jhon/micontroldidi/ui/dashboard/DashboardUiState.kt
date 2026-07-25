package com.jhon.micontroldidi.ui.dashboard

import com.jhon.micontroldidi.domain.PeriodoDashboard

/**
 * Estado de presentación del Dashboard.
 *
 * Contiene valores de dominio sin formato monetario.
 * El formateo se realiza en la capa de UI (Compose).
 */
data class DashboardUiState(
    val periodoSeleccionado: PeriodoDashboard = PeriodoDashboard.DIA,
    val ingresos: Long = 0L,
    val gastos: Long = 0L,
    val gananciaNeta: Long = 0L,
    val cargando: Boolean = true,
    val mensajeError: String? = null
)
