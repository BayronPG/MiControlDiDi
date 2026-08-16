package com.jhon.micontroldidi.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class CalculadorRangoFiltroTest {

    private val bogota: ZoneId = ZoneId.of("America/Bogota") // UTC-5
    private val utc: ZoneId = ZoneOffset.UTC
    private val kolkata: ZoneId = ZoneId.of("Asia/Kolkata") // UTC+5:30

    /**
     * Convierte una fecha a la medianoche UTC del mismo día: es exactamente el formato
     * que expone `DatePickerState.selectedDateMillis` de Material 3.
     */
    private fun seleccionUtc(anio: Int, mes: Int, dia: Int): Long =
        LocalDate.of(anio, mes, dia)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    // ──────────────────────────────────────────────
    // rangoDelDiaSeleccionado
    // ──────────────────────────────────────────────

    @Test
    fun `dia seleccionado en Bogota inicia a las 0000 del dia local`() {
        // Selección 02/08/2026 en el DatePicker → medianoche UTC.
        val rango = CalculadorRangoFiltro.rangoDelDiaSeleccionado(seleccionUtc(2026, 8, 2), bogota)

        val inicio = Instant.ofEpochMilli(rango.inicioInclusivo).atZone(bogota).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 2), inicio)

        val inicioHora = Instant.ofEpochMilli(rango.inicioInclusivo).atZone(bogota).toLocalDateTime()
        assertEquals(0, inicioHora.hour)
        assertEquals(0, inicioHora.minute)
    }

    @Test
    fun `dia seleccionado en Bogota termina al inicio del dia siguiente`() {
        val rango = CalculadorRangoFiltro.rangoDelDiaSeleccionado(seleccionUtc(2026, 8, 2), bogota)

        val fin = Instant.ofEpochMilli(rango.finExclusivo).atZone(bogota).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 3), fin)
    }

    @Test
    fun `dia seleccionado en UTC no se desplaza`() {
        val rango = CalculadorRangoFiltro.rangoDelDiaSeleccionado(seleccionUtc(2026, 8, 2), utc)

        val inicio = Instant.ofEpochMilli(rango.inicioInclusivo).atZone(utc).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 2), inicio)
    }

    @Test
    fun `dia seleccionado en zona con offset positivo no se desplaza`() {
        // Kolkata UTC+5:30: la medianoche UTC del 02/08 cae el 02/08 a las 05:30 local.
        val rango = CalculadorRangoFiltro.rangoDelDiaSeleccionado(seleccionUtc(2026, 8, 2), kolkata)

        val inicio = Instant.ofEpochMilli(rango.inicioInclusivo).atZone(kolkata).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 2), inicio)
    }

    @Test
    fun `dia seleccionado en Bogota respeta hora local de inicio`() {
        val rango = CalculadorRangoFiltro.rangoDelDiaSeleccionado(seleccionUtc(2026, 8, 2), bogota)

        // 00:00 del 02/08 en Bogotá (UTC-5) = 05:00 UTC.
        val esperado = LocalDate.of(2026, 8, 2).atStartOfDay(bogota).toInstant().toEpochMilli()
        assertEquals(esperado, rango.inicioInclusivo)
    }

    // ──────────────────────────────────────────────
    // rangoFiltro
    // ──────────────────────────────────────────────

    @Test
    fun `mismo dia inicial y final genera rango de 24 horas`() {
        // Escenario del hallazgo H-8.7-01: inicial y final = 02/08/2026.
        val rango = CalculadorRangoFiltro.rangoFiltro(seleccionUtc(2026, 8, 2), seleccionUtc(2026, 8, 2), bogota)

        assertNotNull(rango)
        val inicio = Instant.ofEpochMilli(rango!!.inicioInclusivo).atZone(bogota).toLocalDate()
        val fin = Instant.ofEpochMilli(rango.finExclusivo).atZone(bogota).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 2), inicio)
        assertEquals(LocalDate.of(2026, 8, 3), fin)
    }

    @Test
    fun `gasto de las 0924 del dia seleccionado queda dentro del rango`() {
        // Reproducción exacta de H-8.7-01: gasto del 02/08/2026 09:24 Bogotá.
        val gastoMs = LocalDate.of(2026, 8, 2)
            .atTime(9, 24)
            .atZone(bogota)
            .toInstant()
            .toEpochMilli()

        val rango = CalculadorRangoFiltro.rangoFiltro(seleccionUtc(2026, 8, 2), seleccionUtc(2026, 8, 2), bogota)

        assertNotNull(rango)
        assertTrue(gastoMs >= rango!!.inicioInclusivo)
        assertTrue(gastoMs < rango.finExclusivo)
    }

    @Test
    fun `rango de varios dias incluye inicio y excluye fin`() {
        val rango = CalculadorRangoFiltro.rangoFiltro(seleccionUtc(2026, 8, 1), seleccionUtc(2026, 8, 5), bogota)

        assertNotNull(rango)
        val inicio = Instant.ofEpochMilli(rango!!.inicioInclusivo).atZone(bogota).toLocalDate()
        val fin = Instant.ofEpochMilli(rango.finExclusivo).atZone(bogota).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 1), inicio)
        assertEquals(LocalDate.of(2026, 8, 6), fin)
    }

    @Test
    fun `rango que cruza fin de mes abarca ambos meses`() {
        val rango = CalculadorRangoFiltro.rangoFiltro(seleccionUtc(2026, 1, 30), seleccionUtc(2026, 2, 1), bogota)

        assertNotNull(rango)
        val inicio = Instant.ofEpochMilli(rango!!.inicioInclusivo).atZone(bogota).toLocalDate()
        val fin = Instant.ofEpochMilli(rango.finExclusivo).atZone(bogota).toLocalDate()
        assertEquals(LocalDate.of(2026, 1, 30), inicio)
        assertEquals(LocalDate.of(2026, 2, 2), fin)
    }

    @Test
    fun `rango que cruza fin de anio abarca ambos anios`() {
        val rango = CalculadorRangoFiltro.rangoFiltro(seleccionUtc(2026, 12, 30), seleccionUtc(2027, 1, 1), bogota)

        assertNotNull(rango)
        val inicio = Instant.ofEpochMilli(rango!!.inicioInclusivo).atZone(bogota).toLocalDate()
        val fin = Instant.ofEpochMilli(rango.finExclusivo).atZone(bogota).toLocalDate()
        assertEquals(LocalDate.of(2026, 12, 30), inicio)
        assertEquals(LocalDate.of(2027, 1, 2), fin)
    }

    @Test
    fun `inversion de fechas devuelve null`() {
        // Inicial posterior a final: rango inválido (H-8.8-03).
        val rango = CalculadorRangoFiltro.rangoFiltro(seleccionUtc(2026, 8, 5), seleccionUtc(2026, 8, 2), bogota)

        assertNull(rango)
    }

    @Test
    fun `inversion de fechas devuelve null tambien en UTC`() {
        val rango = CalculadorRangoFiltro.rangoFiltro(seleccionUtc(2026, 8, 5), seleccionUtc(2026, 8, 2), utc)

        assertNull(rango)
    }

    // ──────────────────────────────────────────────
    // aUtcMedianoche
    // ──────────────────────────────────────────────

    @Test
    fun `aUtcMedianoche devuelve la medianoche UTC del dia local`() {
        // 02/08/2026 09:24 Bogotá → medianoche UTC del 02/08/2026.
        val timestamp = LocalDate.of(2026, 8, 2).atTime(9, 24).atZone(bogota).toInstant().toEpochMilli()

        val resultado = CalculadorRangoFiltro.aUtcMedianoche(timestamp, bogota)

        assertEquals(seleccionUtc(2026, 8, 2), resultado)
    }

    @Test
    fun `aUtcMedianoche de medianoche local conserva el dia`() {
        // 02/08/2026 00:00 Bogotá ya es medianoche local: el día no debe retroceder.
        val timestamp = LocalDate.of(2026, 8, 2).atStartOfDay(bogota).toInstant().toEpochMilli()

        val resultado = CalculadorRangoFiltro.aUtcMedianoche(timestamp, bogota)

        assertEquals(seleccionUtc(2026, 8, 2), resultado)
    }

    @Test
    fun `aUtcMedianoche de final de dia conserva el dia`() {
        // 02/08/2026 23:59 Bogotá → medianoche UTC del 02/08/2026 (mismo día local).
        val timestamp = LocalDate.of(2026, 8, 2).atTime(23, 59).atZone(bogota).toInstant().toEpochMilli()

        val resultado = CalculadorRangoFiltro.aUtcMedianoche(timestamp, bogota)

        assertEquals(seleccionUtc(2026, 8, 2), resultado)
    }
}
