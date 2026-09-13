package com.jhon.micontroldidi.domain

/**
 * Calculador puro de métricas de kilómetros para una jornada de trabajo.
 *
 * Implementa las reglas del ADR-001 (D-12, D-14):
 * - Totales = odómetro final - inicial.
 * - Con pasajero = suma de distancias de viajes.
 * - Vacíos = max(0, totales - con pasajero).
 * - Inconsistencia si con pasajero > totales.
 * - Porcentaje de vacíos y semáforo según el umbral del perfil.
 * - Jornada abierta marca métricas como pendientes.
 */
object CalculadorKilometros {

    fun calcular(
        kilometrajeInicialMetros: Long,
        kilometrajeFinalMetros: Long,
        distanciaViajesMetros: Long,
        maxPorcentajeKmVacios: Int,
        jornadaCerrada: Boolean
    ): MetricasKilometros {
        if (!jornadaCerrada) {
            return MetricasKilometros(
                kmTotalesMetros = 0L,
                kmConPasajeroMetros = maxOf(0L, distanciaViajesMetros),
                kmVaciosMetros = 0L,
                porcentajeVacios = 0,
                semaforo = EstadoSemaforo.PENDIENTE,
                inconsistencia = false,
                esPendiente = true
            )
        }

        val kmTotalesMetros = maxOf(0L, kilometrajeFinalMetros - kilometrajeInicialMetros)
        val kmConPasajeroMetros = maxOf(0L, distanciaViajesMetros)
        val inconsistencia = kmConPasajeroMetros > kmTotalesMetros
        val kmVaciosMetros = if (inconsistencia) 0L else maxOf(0L, kmTotalesMetros - kmConPasajeroMetros)

        val porcentajeVacios = if (kmTotalesMetros == 0L) {
            0
        } else {
            ((kmVaciosMetros * 100L) / kmTotalesMetros).toInt()
        }

        val semaforo = when {
            kmTotalesMetros == 0L -> EstadoSemaforo.NEUTRO
            porcentajeVacios <= maxPorcentajeKmVacios -> EstadoSemaforo.VERDE
            else -> EstadoSemaforo.ROJO
        }

        return MetricasKilometros(
            kmTotalesMetros = kmTotalesMetros,
            kmConPasajeroMetros = kmConPasajeroMetros,
            kmVaciosMetros = kmVaciosMetros,
            porcentajeVacios = porcentajeVacios,
            semaforo = semaforo,
            inconsistencia = inconsistencia,
            esPendiente = false
        )
    }
}
