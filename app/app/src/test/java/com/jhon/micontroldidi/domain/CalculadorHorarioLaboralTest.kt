package com.jhon.micontroldidi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class CalculadorHorarioLaboralTest {

    private val inicioPorDefecto = LocalTime.of(6, 0)
    private val finPorDefecto = LocalTime.of(15, 0)

    private fun calcular(ahora: LocalTime): EstadoHorarioLaboral =
        CalculadorHorarioLaboral.calcular(ahora, inicioPorDefecto, finPorDefecto)

    // --- Horario permitido ---

    @Test
    fun `el horario completo dura nueve horas`() {
        assertEquals(540, BloqueJornada.DURACION_TOTAL_MINUTOS)
        assertEquals(480, BloqueJornada.MINUTOS_TRABAJO)
        assertEquals(60, BloqueJornada.MINUTOS_PAUSA)
    }

    @Test
    fun `los bloques suman la duracion total en orden`() {
        var acumulado = 0
        BloqueJornada.entries.forEach { bloque ->
            acumulado += bloque.duracionMinutos
        }
        assertEquals(540, acumulado)
    }

    // --- Antes de la jornada ---

    @Test
    fun `antes del inicio no hay bloque activo`() {
        val estado = calcular(LocalTime.of(5, 30))

        assertFalse(estado.enJornada)
        assertNull(estado.bloqueActual)
        assertNull(estado.finBloque)
        assertEquals(0, estado.progresoPorcentaje)
        assertEquals(0, estado.minutosConectado)
        assertEquals(0, estado.minutosEnPausa)
        assertEquals(540, estado.minutosRestantes)
        assertFalse(estado.modoRegreso)
        assertFalse(estado.advertenciaFinalizacion)
    }

    @Test
    fun `antes del inicio la siguiente pausa es la primera del dia`() {
        val estado = calcular(LocalTime.of(5, 30))

        assertEquals(LocalTime.of(8, 30), estado.proximaPausa)
    }

    @Test
    fun `a medianoche sigue fuera de jornada`() {
        val estado = calcular(LocalTime.of(0, 30))

        assertFalse(estado.enJornada)
        assertNull(estado.bloqueActual)
        assertEquals(540, estado.minutosRestantes)
    }

    // --- Bloque principal ---

    @Test
    fun `al iniciar empieza el bloque principal`() {
        val estado = calcular(LocalTime.of(6, 0))

        assertEquals(BloqueJornada.PRINCIPAL, estado.bloqueActual)
        assertEquals(LocalTime.of(8, 30), estado.finBloque)
        assertEquals(0, estado.progresoPorcentaje)
        assertEquals(0, estado.minutosConectado)
        assertEquals(540, estado.minutosRestantes)
    }

    @Test
    fun `a las 7 el bloque principal lleva una hora conectada`() {
        val estado = calcular(LocalTime.of(7, 0))

        assertEquals(BloqueJornada.PRINCIPAL, estado.bloqueActual)
        assertEquals(60, estado.minutosConectado)
        assertEquals(480, estado.minutosRestantes)
    }

    @Test
    fun `a las 8 y 29 sigue el bloque principal`() {
        val estado = calcular(LocalTime.of(8, 29))

        assertEquals(BloqueJornada.PRINCIPAL, estado.bloqueActual)
        assertEquals(149, estado.minutosConectado)
    }

    // --- Pausas ---

    @Test
    fun `a las 8 y 30 empieza la primera pausa`() {
        val estado = calcular(LocalTime.of(8, 30))

        assertEquals(BloqueJornada.PRIMERA_PAUSA, estado.bloqueActual)
        assertEquals(LocalTime.of(8, 45), estado.finBloque)
        assertEquals(150, estado.minutosConectado)
        assertEquals(0, estado.minutosEnPausa)
    }

    @Test
    fun `durante la primera pausa la siguiente pausa es el snack`() {
        val estado = calcular(LocalTime.of(8, 40))

        assertEquals(BloqueJornada.PRIMERA_PAUSA, estado.bloqueActual)
        assertEquals(10, estado.minutosEnPausa)
        assertEquals(LocalTime.of(11, 0), estado.proximaPausa)
    }

    @Test
    fun `a las 8 y 45 empieza el segundo bloque`() {
        val estado = calcular(LocalTime.of(8, 45))

        assertEquals(BloqueJornada.SEGUNDO_BLOQUE, estado.bloqueActual)
        assertEquals(150, estado.minutosConectado)
        assertEquals(15, estado.minutosEnPausa)
        assertEquals(LocalTime.of(11, 0), estado.proximaPausa)
    }

    @Test
    fun `a las 11 empieza el snack y la revision`() {
        val estado = calcular(LocalTime.of(11, 0))

        assertEquals(BloqueJornada.SNACK_REVISION, estado.bloqueActual)
        assertEquals(LocalTime.of(11, 15), estado.finBloque)
        assertEquals(285, estado.minutosConectado)
        assertEquals(LocalTime.of(12, 30), estado.proximaPausa)
    }

    @Test
    fun `a las 11 y 15 empieza el bloque selectivo`() {
        val estado = calcular(LocalTime.of(11, 15))

        assertEquals(BloqueJornada.BLOQUE_SELECTIVO, estado.bloqueActual)
        // 150 del bloque principal + 135 del segundo bloque
        assertEquals(285, estado.minutosConectado)
        assertEquals(30, estado.minutosEnPausa)
    }

    @Test
    fun `a las 12 y 30 empieza el almuerzo`() {
        val estado = calcular(LocalTime.of(12, 30))

        assertEquals(BloqueJornada.ALMUERZO, estado.bloqueActual)
        assertEquals(LocalTime.of(13, 0), estado.finBloque)
        // 150 + 135 + 75: el snack y la revisión son pausa, no trabajo
        assertEquals(360, estado.minutosConectado)
        assertEquals(30, estado.minutosEnPausa)
    }

    @Test
    fun `en el almuerzo ya no queda otra pausa`() {
        val estado = calcular(LocalTime.of(12, 45))

        assertEquals(BloqueJornada.ALMUERZO, estado.bloqueActual)
        assertNull(estado.proximaPausa)
    }

    @Test
    fun `a las 13 empieza el bloque final`() {
        val estado = calcular(LocalTime.of(13, 0))

        assertEquals(BloqueJornada.BLOQUE_FINAL, estado.bloqueActual)
        assertEquals(LocalTime.of(14, 30), estado.finBloque)
        // El bloque final acaba de empezar: el trabajo acumulado no cambia.
        assertEquals(360, estado.minutosConectado)
        assertEquals(60, estado.minutosEnPausa)
    }

    // --- Modo regreso ---

    @Test
    fun `a las 14 y 30 se activa el modo regreso`() {
        val estado = calcular(LocalTime.of(14, 30))

        assertEquals(BloqueJornada.REGRESO_PRODUCTIVO, estado.bloqueActual)
        assertTrue(estado.modoRegreso)
        assertEquals(LocalTime.of(15, 0), estado.finBloque)
        // 150 + 135 + 75 + 90 del bloque final
        assertEquals(450, estado.minutosConectado)
    }

    @Test
    fun `el modo regreso sigue activo hasta el final de la jornada`() {
        val estado = calcular(LocalTime.of(14, 59))

        assertTrue(estado.modoRegreso)
        assertEquals(539 * 100 / 540, estado.progresoPorcentaje)
    }

    @Test
    fun `el modo regreso no esta activo en el bloque final`() {
        val estado = calcular(LocalTime.of(14, 0))

        assertEquals(BloqueJornada.BLOQUE_FINAL, estado.bloqueActual)
        assertFalse(estado.modoRegreso)
    }

    // --- Final de la jornada ---

    @Test
    fun `a las 15 termina la jornada y se advierte`() {
        val estado = calcular(LocalTime.of(15, 0))

        assertFalse(estado.enJornada)
        assertNull(estado.bloqueActual)
        assertTrue(estado.advertenciaFinalizacion)
        assertFalse(estado.modoRegreso)
        assertEquals(100, estado.progresoPorcentaje)
        assertEquals(0, estado.minutosRestantes)
        assertEquals(480, estado.minutosConectado)
        assertEquals(60, estado.minutosEnPausa)
    }

    @Test
    fun `despues del fin se mantiene la advertencia`() {
        val estado = calcular(LocalTime.of(16, 0))

        assertTrue(estado.advertenciaFinalizacion)
        assertEquals(100, estado.progresoPorcentaje)
        assertEquals(480, estado.minutosConectado)
    }

    @Test
    fun `la jornada no se cierra automaticamente`() {
        val estado = calcular(LocalTime.of(23, 30))

        // Sigue siendo solo un aviso: el estado no tiene forma de cerrar nada.
        assertTrue(estado.advertenciaFinalizacion)
        assertFalse(estado.enJornada)
        assertEquals(0, estado.minutosRestantes)
    }

    // --- Progreso ---

    @Test
    fun `a mitad del horario el progreso es del 50 por ciento`() {
        val estado = calcular(LocalTime.of(10, 30))

        assertEquals(50, estado.progresoPorcentaje)
        assertEquals(270, estado.minutosRestantes)
    }

    @Test
    fun `el progreso nunca sale de cero a cien`() {
        assertEquals(0, calcular(LocalTime.of(3, 0)).progresoPorcentaje)
        assertEquals(100, calcular(LocalTime.of(22, 0)).progresoPorcentaje)
    }

    // --- Horarios distintos al de por defecto ---

    @Test
    fun `el horario se desplaza con la hora de inicio`() {
        val estado = CalculadorHorarioLaboral.calcular(
            ahora = LocalTime.of(15, 0),
            horaInicio = LocalTime.of(7, 0),
            horaFin = LocalTime.of(16, 0)
        )

        // Con inicio a las 07:00 el bloque final va de 14:00 a 15:30.
        assertEquals(BloqueJornada.BLOQUE_FINAL, estado.bloqueActual)
        assertFalse(estado.modoRegreso)
        assertFalse(estado.advertenciaFinalizacion)
    }

    @Test
    fun `con inicio desplazado el modo regreso empieza a las 15 y 30`() {
        val estado = CalculadorHorarioLaboral.calcular(
            ahora = LocalTime.of(15, 30),
            horaInicio = LocalTime.of(7, 0),
            horaFin = LocalTime.of(16, 0)
        )

        assertEquals(BloqueJornada.REGRESO_PRODUCTIVO, estado.bloqueActual)
        assertTrue(estado.modoRegreso)
    }

    @Test
    fun `una jornada mas corta recorta los ultimos bloques`() {
        val estado = CalculadorHorarioLaboral.calcular(
            ahora = LocalTime.of(8, 0),
            horaInicio = LocalTime.of(6, 0),
            horaFin = LocalTime.of(8, 0)
        )

        assertFalse(estado.enJornada)
        assertNull(estado.bloqueActual)
        assertEquals(120, estado.minutosConectado)
        assertEquals(0, estado.minutosEnPausa)
        assertEquals(100, estado.progresoPorcentaje)
        assertEquals(0, estado.minutosRestantes)
    }

    @Test
    fun `una jornada de duracion cero no divide por cero`() {
        val estado = CalculadorHorarioLaboral.calcular(
            ahora = LocalTime.of(6, 0),
            horaInicio = LocalTime.of(6, 0),
            horaFin = LocalTime.of(6, 0)
        )

        assertEquals(0, estado.progresoPorcentaje)
        assertEquals(0, estado.minutosRestantes)
        assertEquals(0, estado.minutosConectado)
        assertEquals(0, estado.minutosEnPausa)
        assertNull(estado.bloqueActual)
    }

    @Test
    fun `los limites del bloque son semiabiertos`() {
        // 08:29:59 pertenece al bloque principal; 08:30 ya es la pausa.
        assertEquals(BloqueJornada.PRINCIPAL, calcular(LocalTime.of(8, 29)).bloqueActual)
        assertEquals(BloqueJornada.PRIMERA_PAUSA, calcular(LocalTime.of(8, 30)).bloqueActual)
        // 14:59 sigue en regreso; 15:00 ya cerró el horario.
        assertEquals(BloqueJornada.REGRESO_PRODUCTIVO, calcular(LocalTime.of(14, 59)).bloqueActual)
        assertNull(calcular(LocalTime.of(15, 0)).bloqueActual)
    }
}
