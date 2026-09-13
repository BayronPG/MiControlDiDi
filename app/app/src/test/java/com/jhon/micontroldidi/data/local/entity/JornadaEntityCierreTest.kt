package com.jhon.micontroldidi.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JornadaEntityCierreTest {

    private fun jornada(
        kilometrajeInicialMetros: Long = 10_000_000L,
        fechaHoraFin: Long = 0L,
        kilometrajeFinalMetros: Long = 0L
    ) = JornadaEntity(
        fechaHoraInicio = 1000L,
        kilometrajeInicialMetros = kilometrajeInicialMetros,
        precioGalonExtra = 17000L,
        plataforma = "inDrive",
        metaBrutaDia = 120000L,
        nivelEnergia = 8,
        fechaHoraFin = fechaHoraFin,
        kilometrajeFinalMetros = kilometrajeFinalMetros
    )

    @Test
    fun `una jornada nueva esta abierta`() {
        val entidad = jornada()

        assertEquals(0L, entidad.fechaHoraFin)
        assertEquals(0L, entidad.kilometrajeFinalMetros)
        assertFalse(entidad.cerrada)
        assertEquals(0L, entidad.kilometrosTotales)
    }

    @Test
    fun `una jornada con hora de fin esta cerrada`() {
        val entidad = jornada(fechaHoraFin = 2000L, kilometrajeFinalMetros = 10_045_000L)

        assertTrue(entidad.cerrada)
        assertEquals(45L, entidad.kilometrosTotales)
        assertEquals(10045L, entidad.kilometrajeFinalKm)
    }

    @Test
    fun `el cierre se refleja en los kilometros totales en metros`() {
        val entidad = jornada(fechaHoraFin = 2000L, kilometrajeFinalMetros = 10_000_500L)

        assertEquals(0L, entidad.kilometrosTotales)
        assertEquals(10000L, entidad.kilometrajeFinalKm)
    }
}
