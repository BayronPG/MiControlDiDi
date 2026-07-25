package com.jhon.micontroldidi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.Month

class CalculadorRangoPeriodoTest {

    // ──────────────────────────────────────────────
    // DÍA
    // ──────────────────────────────────────────────

    @Test
    fun `dia normal calcula desde 0000 hasta 0000 del dia siguiente`() {
        // 2026-07-25 10:30:00 en Bogotá (UTC-5)
        val referencia = zonedEpochMillis(2026, Month.JULY, 25, 10, 30, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.DIA, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 25, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.JULY, 26, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    @Test
    fun `dia cambio de dia incluye referencia justo antes de medianoche`() {
        // 2026-07-25 23:59:59.999 en Bogotá → último ms del día 25
        val referencia = zonedEpochMillis(2026, Month.JULY, 25, 23, 59, 59, ZoneOffset.ofHours(-5)) + 999

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.DIA, referencia, ZoneOffset.ofHours(-5)
        )

        // inicio = 2026-07-25 00:00:00.000
        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 25, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        // fin = 2026-07-26 00:00:00.000 → cualquier ms de este valor está excluido
    }

    @Test
    fun `dia cambio de dia referencia en 0000 del dia siguiente da periodo diferente`() {
        // 1 ms después del fin del día 25 = ya es día 26
        val inicioDia26 = zonedEpochMillis(2026, Month.JULY, 26, 0, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.DIA, inicioDia26, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 26, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.JULY, 27, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    // ──────────────────────────────────────────────
    // SEMANA
    // ──────────────────────────────────────────────

    @Test
    fun `semana con referencia en lunes empieza ese lunes`() {
        // 2026-07-27 es lunes
        val referencia = zonedEpochMillis(2026, Month.JULY, 27, 8, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.SEMANA, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 27, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.AUGUST, 3, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    @Test
    fun `semana con referencia en domingo retrocede al lunes anterior`() {
        // 2026-08-02 es domingo → semana del lunes 27-jul al lunes 03-ago
        val referencia = zonedEpochMillis(2026, Month.AUGUST, 2, 15, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.SEMANA, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 27, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.AUGUST, 3, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    @Test
    fun `semana que cruza de diciembre a enero`() {
        // 2026-12-31 es jueves → lunes anterior = 28-dic, lunes siguiente = 04-ene-2027
        val referencia = zonedEpochMillis(2026, Month.DECEMBER, 31, 12, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.SEMANA, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.DECEMBER, 28, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2027, Month.JANUARY, 4, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    @Test
    fun `semana con referencia el 01-ene retrocede al lunes de la semana anterior`() {
        // 2026-01-01 es jueves → lunes anterior = 29-dic-2025
        val referencia = zonedEpochMillis(2026, Month.JANUARY, 1, 6, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.SEMANA, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2025, Month.DECEMBER, 29, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.JANUARY, 5, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    // ──────────────────────────────────────────────
    // MES
    // ──────────────────────────────────────────────

    @Test
    fun `mes de 31 dias`() {
        // Julio tiene 31 días
        val referencia = zonedEpochMillis(2026, Month.JULY, 15, 10, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.MES, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.AUGUST, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    @Test
    fun `mes de 30 dias`() {
        // Abril tiene 30 días
        val referencia = zonedEpochMillis(2026, Month.APRIL, 10, 10, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.MES, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.APRIL, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.MAY, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)

        // Verificar que abril tiene 30 días (30 * 86400000 ms entre inicio y fin)
        val duracionMs = rango.finExclusivo - rango.inicioInclusivo
        assertEquals(30 * 86_400_000L, duracionMs)
    }

    @Test
    fun `mes de 28 dias febrero no bisiesto`() {
        // 2026 no es bisiesto → febrero tiene 28 días
        val referencia = zonedEpochMillis(2026, Month.FEBRUARY, 14, 10, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.MES, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.FEBRUARY, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.MARCH, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)

        val duracionMs = rango.finExclusivo - rango.inicioInclusivo
        assertEquals(28 * 86_400_000L, duracionMs)
    }

    @Test
    fun `mes de 29 dias febrero bisiesto`() {
        // 2028 es bisiesto → febrero tiene 29 días
        val referencia = zonedEpochMillis(2028, Month.FEBRUARY, 14, 10, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.MES, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2028, Month.FEBRUARY, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2028, Month.MARCH, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)

        val duracionMs = rango.finExclusivo - rango.inicioInclusivo
        assertEquals(29 * 86_400_000L, duracionMs)
    }

    @Test
    fun `mes cambio de ano`() {
        // Diciembre 2026 → enero 2027
        val referencia = zonedEpochMillis(2026, Month.DECEMBER, 25, 10, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.MES, referencia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.DECEMBER, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2027, Month.JANUARY, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    // ──────────────────────────────────────────────
    // ZONA HORARIA
    // ──────────────────────────────────────────────

    @Test
    fun `zona horaria distinta de UTC afecta los limites del dia`() {
        // UTC+9 (Tokio). 2026-07-25 22:00 UTC → 2026-07-26 07:00 JST
        val referencia = zonedEpochMillis(2026, Month.JULY, 25, 22, 0, 0, ZoneOffset.UTC)
        val zoneId = ZoneOffset.ofHours(9)

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.DIA, referencia, zoneId
        )

        // En JST, la referencia es 2026-07-26 07:00, así que el día es 26 de julio
        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 26, 0, 0, 0, zoneId)
        val finEsperado = zonedEpochMillis(2026, Month.JULY, 27, 0, 0, 0, zoneId)
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    @Test
    fun `zona America Bogota da resultados coherentes`() {
        val zoneBogota = ZoneId.of("America/Bogota") // UTC-5
        // 2026-07-25 10:00 Bogotá
        val referencia = zonedEpochMillis(2026, Month.JULY, 25, 10, 0, 0, ZoneOffset.ofHours(-5))

        val rangoDia = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.DIA, referencia, zoneBogota
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 25, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rangoDia.inicioInclusivo)
    }

    // ──────────────────────────────────────────────
    // INICIO INCLUIDO / FIN EXCLUSIVO
    // ──────────────────────────────────────────────

    @Test
    fun `inicio incluido el timestamp de inicio esta dentro del rango`() {
        val referencia = zonedEpochMillis(2026, Month.JULY, 25, 10, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.DIA, referencia, ZoneOffset.ofHours(-5)
        )

        val justoAlInicio = rango.inicioInclusivo
        assertTrue("inicio debe estar dentro del rango",
            justoAlInicio >= rango.inicioInclusivo && justoAlInicio < rango.finExclusivo
        )
    }

    @Test
    fun `fin exclusivo el timestamp exacto del fin esta fuera del rango`() {
        val referencia = zonedEpochMillis(2026, Month.JULY, 25, 10, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.DIA, referencia, ZoneOffset.ofHours(-5)
        )

        // El timestamp exacto de fin NO está en el rango
        assertTrue("finExclusivo debe estar fuera del rango",
            rango.finExclusivo !in rango.inicioInclusivo until rango.finExclusivo
        )
    }

    @Test
    fun `inicio incluido mes un ms despues del inicio esta dentro`() {
        val referencia = zonedEpochMillis(2026, Month.JULY, 15, 0, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.MES, referencia, ZoneOffset.ofHours(-5)
        )

        val unMsDespuesDelInicio = rango.inicioInclusivo + 1
        assertTrue("un ms después del inicio debe estar dentro",
            unMsDespuesDelInicio in rango.inicioInclusivo until rango.finExclusivo
        )
    }

    // ──────────────────────────────────────────────
    // RANGO CON REFERENCIA EN LOS LÍMITES
    // ──────────────────────────────────────────────

    @Test
    fun `dia referencia exactamente al inicio 0000`() {
        val inicioDia = zonedEpochMillis(2026, Month.JULY, 25, 0, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.DIA, inicioDia, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 25, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.JULY, 26, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    @Test
    fun `mes referencia el primer dia del mes`() {
        val primero = zonedEpochMillis(2026, Month.JULY, 1, 0, 0, 0, ZoneOffset.ofHours(-5))

        val rango = CalculadorRangoPeriodo.calcular(
            PeriodoDashboard.MES, primero, ZoneOffset.ofHours(-5)
        )

        val inicioEsperado = zonedEpochMillis(2026, Month.JULY, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        val finEsperado = zonedEpochMillis(2026, Month.AUGUST, 1, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(inicioEsperado, rango.inicioInclusivo)
        assertEquals(finEsperado, rango.finExclusivo)
    }

    // ──────────────────────────────────────────────
    // VALIDACIÓN DE RangoPeriodo
    // ──────────────────────────────────────────────

    @Test
    fun `RangoPeriodo acepta inicio negativo cuando fin es mayor`() {
        val rango = RangoPeriodo(inicioInclusivo = -86_400_000, finExclusivo = 0)
        assertEquals(-86_400_000, rango.inicioInclusivo)
        assertEquals(0, rango.finExclusivo)
    }

    @Test
    fun `RangoPeriodo rechaza fin menor o igual que inicio`() {
        assertThrows(IllegalArgumentException::class.java) {
            RangoPeriodo(inicioInclusivo = 1000, finExclusivo = 1000)
        }
        assertThrows(IllegalArgumentException::class.java) {
            RangoPeriodo(inicioInclusivo = 1000, finExclusivo = 500)
        }
    }

    @Test
    fun `RangoPeriodo acepta valores validos`() {
        val rango = RangoPeriodo(inicioInclusivo = 0, finExclusivo = 86_400_000)
        assertEquals(0, rango.inicioInclusivo)
        assertEquals(86_400_000, rango.finExclusivo)
    }

    // ──────────────────────────────────────────────
    // HELPER
    // ──────────────────────────────────────────────

    /**
     * Convierte una fecha en año/mes/día/hora/minuto/segundo a epoch milliseconds
     * usando la zona horaria especificada.
     */
    private fun zonedEpochMillis(
        year: Int,
        month: Month,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
        zoneId: ZoneId
    ): Long {
        return ZonedDateTime.of(year, month.value, day, hour, minute, second, 0, zoneId)
            .toInstant()
            .toEpochMilli()
    }
}
