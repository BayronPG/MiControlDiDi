package com.jhon.micontroldidi.domain

/**
 * Métricas de distancias recorridas en una jornada.
 *
 * - Todas las distancias base se manejan en metros con [Long] (ADR-001 D-14).
 * - [inconsistencia] es true si los kilómetros con pasajero superan a los totales.
 * - [esPendiente] es true cuando la jornada permanece abierta.
 */
data class MetricasKilometros(
    val kmTotalesMetros: Long,
    val kmConPasajeroMetros: Long,
    val kmVaciosMetros: Long,
    val porcentajeVacios: Int,
    val semaforo: EstadoSemaforo,
    val inconsistencia: Boolean,
    val esPendiente: Boolean
) {
    /** Kilómetros totales completos para visualización. */
    val kmTotalesKm: Long get() = kmTotalesMetros / METROS_POR_KM

    /** Kilómetros con pasajero completos para visualización. */
    val kmConPasajeroKm: Long get() = kmConPasajeroMetros / METROS_POR_KM

    /** Kilómetros vacíos completos para visualización. */
    val kmVaciosKm: Long get() = kmVaciosMetros / METROS_POR_KM

    companion object {
        const val METROS_POR_KM = 1000L
    }
}
