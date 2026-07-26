package com.jhon.micontroldidi.ui.stats

import com.jhon.micontroldidi.domain.PeriodoDashboard

/**
 * Estado de la pantalla de estadísticas.
 * Muestra comparación entre el periodo actual y el anterior.
 */
data class StatsUiState(
    val periodoSeleccionado: StatsPeriodo = StatsPeriodo.HOY,
    val cargando: Boolean = true,

    // Periodo actual
    val ingresosActual: Long = 0L,
    val gastosActual: Long = 0L,
    val gananciaNetaActual: Long = 0L,
    val cantidadViajesActual: Int = 0,
    val cantidadGastosActual: Int = 0,

    // Periodo anterior
    val ingresosAnterior: Long = 0L,
    val gastosAnterior: Long = 0L,
    val gananciaNetaAnterior: Long = 0L,

    val mensajeError: String? = null
) {
    val diferenciaIngresos: Long get() = ingresosActual - ingresosAnterior
    val diferenciaGastos: Long get() = gastosActual - gastosAnterior
    val diferenciaGanancia: Long get() = gananciaNetaActual - gananciaNetaAnterior

    val porcIngresos: Float get() = if (ingresosAnterior > 0) ingresosActual.toFloat() / ingresosAnterior.toFloat() else 1f
    val porcGastos: Float get() = if (gastosAnterior > 0) gastosActual.toFloat() / gastosAnterior.toFloat() else 1f
    val porcGanancia: Float get() = if (gananciaNetaAnterior > 0) gananciaNetaActual.toFloat() / gananciaNetaAnterior.toFloat() else 1f

    companion object {
        fun statsPeriodoToDashboard(periodo: StatsPeriodo): PeriodoDashboard = when (periodo) {
            StatsPeriodo.HOY -> PeriodoDashboard.DIA
            StatsPeriodo.SEMANA -> PeriodoDashboard.SEMANA
            StatsPeriodo.MES -> PeriodoDashboard.MES
        }
    }
}
