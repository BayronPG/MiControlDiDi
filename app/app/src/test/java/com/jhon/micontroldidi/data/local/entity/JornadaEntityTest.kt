package com.jhon.micontroldidi.data.local.entity

import com.jhon.micontroldidi.domain.NivelCombustible
import com.jhon.micontroldidi.domain.PuntoRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JornadaEntityTest {

    private fun jornada(
        kilometrajeInicialMetros: Long = 12345000L,
        precioGalonExtra: Long = 17000L,
        metaBrutaDia: Long = 120000L,
        nivelEnergia: Int = 8
    ) = JornadaEntity(
        fechaHoraInicio = 1000L,
        kilometrajeInicialMetros = kilometrajeInicialMetros,
        precioGalonExtra = precioGalonExtra,
        plataforma = "inDrive",
        metaBrutaDia = metaBrutaDia,
        nivelEnergia = nivelEnergia
    )

    @Test
    fun `la revision empieza toda en buen estado`() {
        val j = jornada()

        PuntoRevision.entries.forEach { punto ->
            assertTrue("$punto debería estar en buen estado", j.estaEnBuenEstado(punto))
        }
        assertTrue(j.puntosEnMalEstado.isEmpty())
        assertTrue(j.puntosCriticosEnMalEstado.isEmpty())
    }

    @Test
    fun `los criticos son llantas frenos y luces`() {
        assertEquals(
            listOf(PuntoRevision.LLANTAS, PuntoRevision.FRENOS, PuntoRevision.LUCES),
            PuntoRevision.CRITICOS
        )
        assertTrue(PuntoRevision.LLANTAS.critico)
        assertFalse(PuntoRevision.CADENA.critico)
        assertFalse(PuntoRevision.AGUA.critico)
    }

    @Test
    fun `conRevision cambia solo el punto indicado`() {
        PuntoRevision.entries.forEach { punto ->
            val modificada = jornada().conRevision(punto, false)

            assertFalse(modificada.estaEnBuenEstado(punto))
            val otros = PuntoRevision.entries.filter { it != punto }
            otros.forEach { otro ->
                assertTrue(
                    "Al marcar $punto no debería cambiar $otro",
                    modificada.estaEnBuenEstado(otro)
                )
            }
            assertEquals(1, modificada.puntosEnMalEstado.size)
        }
    }

    @Test
    fun `conRevision puede volver a marcar en buen estado`() {
        val conFalla = jornada().conRevision(PuntoRevision.FRENOS, false)
        val recuperada = conFalla.conRevision(PuntoRevision.FRENOS, true)

        assertTrue(recuperada.estaEnBuenEstado(PuntoRevision.FRENOS))
        assertTrue(recuperada.puntosEnMalEstado.isEmpty())
    }

    @Test
    fun `los puntos criticos en mal estado se separan del resto`() {
        val j = jornada()
            .conRevision(PuntoRevision.FRENOS, false)
            .conRevision(PuntoRevision.AGUA, false)

        assertEquals(2, j.puntosEnMalEstado.size)
        assertEquals(listOf(PuntoRevision.FRENOS), j.puntosCriticosEnMalEstado)
    }

    @Test
    fun `todos los criticos en mal estado se reportan juntos`() {
        val j = jornada()
            .conRevision(PuntoRevision.LLANTAS, false)
            .conRevision(PuntoRevision.FRENOS, false)
            .conRevision(PuntoRevision.LUCES, false)

        assertEquals(3, j.puntosCriticosEnMalEstado.size)
    }

    @Test
    fun `el nivel de combustible se expone tipado`() {
        val j = JornadaEntity(
            fechaHoraInicio = 1000L,
            kilometrajeInicialMetros = 0L,
            nivelCombustible = "TRES_CUARTOS",
            precioGalonExtra = 17000L,
            plataforma = "inDrive",
            metaBrutaDia = 100000L,
            nivelEnergia = 5
        )

        assertEquals(NivelCombustible.TRES_CUARTOS, j.nivel)
    }

    @Test
    fun `un nivel de combustible desconocido degrada al valor por defecto`() {
        val j = JornadaEntity(
            fechaHoraInicio = 1000L,
            kilometrajeInicialMetros = 0L,
            nivelCombustible = "NO_EXISTE",
            precioGalonExtra = 17000L,
            plataforma = "inDrive",
            metaBrutaDia = 100000L,
            nivelEnergia = 5
        )

        assertEquals(NivelCombustible.POR_DEFECTO, j.nivel)
        assertEquals(NivelCombustible.MEDIO, j.nivel)
    }

    @Test
    fun `fromNombre ignora mayusculas y devuelve null si no existe`() {
        assertEquals(NivelCombustible.LLENO, NivelCombustible.fromNombre("lleno"))
        assertEquals(NivelCombustible.RESERVA, NivelCombustible.fromNombre("Reserva"))
        assertNull(NivelCombustible.fromNombre("MEDIO_LLENO"))
        assertNull(NivelCombustible.fromNombre(null))
    }

    @Test
    fun `el odometro se convierte entre metros y kilometros`() {
        val j = jornada(kilometrajeInicialMetros = 12345000L)

        assertEquals(12345L, j.kilometrajeInicialKm)
        assertEquals(12345000L, JornadaEntity.kmAMetros(12345L))
        assertEquals(0L, JornadaEntity.kmAMetros(0L))
    }
}
