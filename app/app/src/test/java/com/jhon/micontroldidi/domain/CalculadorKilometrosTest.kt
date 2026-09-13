package com.jhon.micontroldidi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadorKilometrosTest {

    @Test
    fun `jornada abierta devuelve metricas pendientes`() {
        val resultado = CalculadorKilometros.calcular(
            kilometrajeInicialMetros = 10_000_000L,
            kilometrajeFinalMetros = 0L,
            distanciaViajesMetros = 15_000L,
            maxPorcentajeKmVacios = 25,
            jornadaCerrada = false
        )

        assertTrue("En jornada abierta debe ser pendiente", resultado.esPendiente)
        assertEquals(EstadoSemaforo.PENDIENTE, resultado.semaforo)
        assertEquals(0L, resultado.kmTotalesMetros)
        assertEquals(15_000L, resultado.kmConPasajeroMetros)
        assertEquals(0L, resultado.kmVaciosMetros)
        assertEquals(0, resultado.porcentajeVacios)
        assertFalse(resultado.inconsistencia)
    }

    @Test
    fun `jornada cerrada calcula totales y vacios correctamente`() {
        // Inicial: 10.000 km, Final: 10.100 km -> 100 km totales (100.000 m).
        // Viajes con pasajero: 75 km (75.000 m).
        // Vacíos: 25 km (25.000 m) = 25%.
        val resultado = CalculadorKilometros.calcular(
            kilometrajeInicialMetros = 10_000_000L,
            kilometrajeFinalMetros = 10_100_000L,
            distanciaViajesMetros = 75_000L,
            maxPorcentajeKmVacios = 25,
            jornadaCerrada = true
        )

        assertFalse(resultado.esPendiente)
        assertEquals(100_000L, resultado.kmTotalesMetros)
        assertEquals(100L, resultado.kmTotalesKm)
        assertEquals(75_000L, resultado.kmConPasajeroMetros)
        assertEquals(75L, resultado.kmConPasajeroKm)
        assertEquals(25_000L, resultado.kmVaciosMetros)
        assertEquals(25L, resultado.kmVaciosKm)
        assertEquals(25, resultado.porcentajeVacios)
        assertEquals(EstadoSemaforo.VERDE, resultado.semaforo)
        assertFalse(resultado.inconsistencia)
    }

    @Test
    fun `jornada con 0 km totales devuelve estado neutro`() {
        val resultado = CalculadorKilometros.calcular(
            kilometrajeInicialMetros = 10_000_000L,
            kilometrajeFinalMetros = 10_000_000L,
            distanciaViajesMetros = 0L,
            maxPorcentajeKmVacios = 25,
            jornadaCerrada = true
        )

        assertEquals(0L, resultado.kmTotalesMetros)
        assertEquals(0L, resultado.kmVaciosMetros)
        assertEquals(0, resultado.porcentajeVacios)
        assertEquals(EstadoSemaforo.NEUTRO, resultado.semaforo)
        assertFalse(resultado.inconsistencia)
    }

    @Test
    fun `km con pasajero mayores que totales marca inconsistencia y acota vacios a cero`() {
        // Totales 50 km (50.000 m), pero viajes suman 60 km (60.000 m) por error de captura.
        val resultado = CalculadorKilometros.calcular(
            kilometrajeInicialMetros = 10_000_000L,
            kilometrajeFinalMetros = 10_050_000L,
            distanciaViajesMetros = 60_000L,
            maxPorcentajeKmVacios = 25,
            jornadaCerrada = true
        )

        assertTrue("Debe marcarse inconsistencia", resultado.inconsistencia)
        assertEquals(50_000L, resultado.kmTotalesMetros)
        assertEquals(60_000L, resultado.kmConPasajeroMetros)
        assertEquals("Los vacíos no deben ser negativos", 0L, resultado.kmVaciosMetros)
        assertEquals(0, resultado.porcentajeVacios)
    }

    @Test
    fun `semaforo en el umbral exacto`() {
        // Umbral 25%:
        // Exactamente 25% vacíos -> VERDE
        val enUmbral = CalculadorKilometros.calcular(
            kilometrajeInicialMetros = 0L,
            kilometrajeFinalMetros = 100_000L,
            distanciaViajesMetros = 75_000L, // vacíos = 25.000 (25%)
            maxPorcentajeKmVacios = 25,
            jornadaCerrada = true
        )
        assertEquals(25, enUmbral.porcentajeVacios)
        assertEquals(EstadoSemaforo.VERDE, enUmbral.semaforo)

        // 26% vacíos -> ROJO (supera umbral)
        val superaUmbral = CalculadorKilometros.calcular(
            kilometrajeInicialMetros = 0L,
            kilometrajeFinalMetros = 100_000L,
            distanciaViajesMetros = 74_000L, // vacíos = 26.000 (26%)
            maxPorcentajeKmVacios = 25,
            jornadaCerrada = true
        )
        assertEquals(26, superaUmbral.porcentajeVacios)
        assertEquals(EstadoSemaforo.ROJO, superaUmbral.semaforo)

        // 24% vacíos -> VERDE (debajo del umbral)
        val debajoUmbral = CalculadorKilometros.calcular(
            kilometrajeInicialMetros = 0L,
            kilometrajeFinalMetros = 100_000L,
            distanciaViajesMetros = 76_000L, // vacíos = 24.000 (24%)
            maxPorcentajeKmVacios = 25,
            jornadaCerrada = true
        )
        assertEquals(24, debajoUmbral.porcentajeVacios)
        assertEquals(EstadoSemaforo.VERDE, debajoUmbral.semaforo)
    }

    @Test
    fun `precision con distancias menores a kilometros completos`() {
        // Totales: 5.500 metros (5.5 km), con pasajero: 1.650 metros
        // Vacíos = 3.850 metros
        // % vacíos = (3850 * 100) / 5500 = 70%
        val resultado = CalculadorKilometros.calcular(
            kilometrajeInicialMetros = 10_000_000L,
            kilometrajeFinalMetros = 10_005_500L,
            distanciaViajesMetros = 1_650L,
            maxPorcentajeKmVacios = 25,
            jornadaCerrada = true
        )

        assertEquals(5_500L, resultado.kmTotalesMetros)
        assertEquals(5L, resultado.kmTotalesKm)
        assertEquals(1_650L, resultado.kmConPasajeroMetros)
        assertEquals(1L, resultado.kmConPasajeroKm)
        assertEquals(3_850L, resultado.kmVaciosMetros)
        assertEquals(3L, resultado.kmVaciosKm)
        assertEquals(70, resultado.porcentajeVacios)
        assertEquals(EstadoSemaforo.ROJO, resultado.semaforo)
    }
}
