package com.jhon.micontroldidi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadorNetosTest {

    @Test
    fun `jornada abierta devuelve ingresos y gastos parciales pero reserva costo por km y neto economico pendientes`() {
        // Ingresos: $120.000, Gastos: $30.000
        val resultado = CalculadorNetos.calcular(
            ingresos = 120_000L,
            gastosReales = 30_000L,
            gastoGasolina = 20_000L,
            kmTotalesMetros = 50_000L,
            reservaTotalPorKm = 100L,
            jornadaCerrada = false
        )

        assertTrue("En jornada abierta debe ser pendiente", resultado.esPendiente)
        assertEquals(120_000L, resultado.ingresos)
        assertEquals(30_000L, resultado.gastos)
        assertEquals(90_000L, resultado.netoOperativo)
        assertNull("Reserva debe ser null (pendiente)", resultado.reservaMantenimientoTotal)
        assertNull("Costo combustible por km debe ser null (pendiente)", resultado.costoCombustiblePorKm)
        assertNull("Neto económico debe ser null (pendiente)", resultado.netoEconomico)
    }

    @Test
    fun `jornada cerrada calcula neto operativo y economico correctamente`() {
        // Ingresos: $200.000
        // Gastos reales: $50.000 (ya incluye gasolina de $35.000)
        // Recorrido: 100 km (100.000 m)
        // Reserva por km: $80 / km
        // Reserva total = 100 * 80 = $8.000
        // Neto operativo = 200.000 - 50.000 = $150.000
        // Neto económico = 150.000 - 8.000 = $142.000
        // Costo combustible por km = (35.000 * 1000) / 100.000 = $350 / km
        val resultado = CalculadorNetos.calcular(
            ingresos = 200_000L,
            gastosReales = 50_000L,
            gastoGasolina = 35_000L,
            kmTotalesMetros = 100_000L,
            reservaTotalPorKm = 80L,
            jornadaCerrada = true
        )

        assertFalse(resultado.esPendiente)
        assertEquals(200_000L, resultado.ingresos)
        assertEquals(50_000L, resultado.gastos)
        assertEquals(150_000L, resultado.netoOperativo)
        assertEquals(8_000L, resultado.reservaMantenimientoTotal)
        assertEquals(350L, resultado.costoCombustiblePorKm)
        assertEquals(142_000L, resultado.netoEconomico)
    }

    @Test
    fun `neto operativo no descuenta gasolina dos veces`() {
        // Gastos reales = $40.000 (incluye $30.000 de gasolina + $10.000 de almuerzo)
        // Ingresos = $100.000
        // netoOperativo = 100.000 - 40.000 = 60.000 (no resta 30.000 de nuevo)
        val resultado = CalculadorNetos.calcular(
            ingresos = 100_000L,
            gastosReales = 40_000L,
            gastoGasolina = 30_000L,
            kmTotalesMetros = 50_000L,
            reservaTotalPorKm = 0L,
            jornadaCerrada = true
        )

        assertEquals(60_000L, resultado.netoOperativo)
        assertEquals(60_000L, resultado.netoEconomico)
    }

    @Test
    fun `peajes informativos no se descuentan automaticamente del neto`() {
        // Si el conductor tuvo peajes informativos, estos no deben restarse de netoOperativo ni netoEconomico
        // a menos que hayan sido registrados como un gasto real (en cuyo caso ya vienen dentro de gastosReales).
        val resultado = CalculadorNetos.calcular(
            ingresos = 150_000L,
            gastosReales = 20_000L,
            gastoGasolina = 15_000L,
            kmTotalesMetros = 80_000L,
            reservaTotalPorKm = 50L,
            jornadaCerrada = true
        )

        assertEquals(130_000L, resultado.netoOperativo)
        val reservaEsperada = (80_000L * 50L) / 1000L // 4.000
        assertEquals(reservaEsperada, resultado.reservaMantenimientoTotal)
        assertEquals(126_000L, resultado.netoEconomico)
    }

    @Test
    fun `precision con distancias menores a kilometros completos sin perder precision`() {
        // Recorrido: 3.500 metros (3.5 km)
        // Reserva por km: $100 / km
        // reservaTotal = (3.500 * 100) / 1000 = 350 pesos
        // Si se dividiera 3500 / 1000 primero en enteros, daría 3 * 100 = 300 pesos (pérdida de 50 pesos).
        // Costo combustible = $7.000
        // costoCombustiblePorKm = (7.000 * 1000) / 3.500 = 2.000 pesos/km
        val resultado = CalculadorNetos.calcular(
            ingresos = 50_000L,
            gastosReales = 10_000L,
            gastoGasolina = 7_000L,
            kmTotalesMetros = 3_500L,
            reservaTotalPorKm = 100L,
            jornadaCerrada = true
        )

        assertEquals("No debe haber truncamiento previo a la multiplicación", 350L, resultado.reservaMantenimientoTotal)
        assertEquals(2_000L, resultado.costoCombustiblePorKm)
        assertEquals(39_650L, resultado.netoEconomico)
    }

    @Test
    fun `jornada cerrada con 0 km totales no causa division por cero`() {
        val resultado = CalculadorNetos.calcular(
            ingresos = 80_000L,
            gastosReales = 10_000L,
            gastoGasolina = 10_000L,
            kmTotalesMetros = 0L,
            reservaTotalPorKm = 100L,
            jornadaCerrada = true
        )

        assertEquals(70_000L, resultado.netoOperativo)
        assertEquals(0L, resultado.reservaMantenimientoTotal)
        assertEquals(0L, resultado.costoCombustiblePorKm)
        assertEquals(70_000L, resultado.netoEconomico)
    }
}
