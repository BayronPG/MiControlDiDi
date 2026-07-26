package com.jhon.micontroldidi.ui.dashboard

import com.jhon.micontroldidi.data.local.entity.MetaEntity
import com.jhon.micontroldidi.domain.PeriodoDashboard

/**
 * Estado de presentación del Dashboard.
 *
 * [progresoMeta] es un valor entre 0.0 y +inf que representa
 * el avance hacia la meta activa (gananciaNeta / valorObjetivo).
 * Es null cuando no hay meta activa.
 */
data class DashboardUiState(
    val periodoSeleccionado: PeriodoDashboard = PeriodoDashboard.DIA,
    val ingresos: Long = 0L,
    val gastos: Long = 0L,
    val gananciaNeta: Long = 0L,
    val cargando: Boolean = true,
    val mensajeError: String? = null,
    val metaActiva: MetaEntity? = null,
    val progresoMeta: Float? = null
)
