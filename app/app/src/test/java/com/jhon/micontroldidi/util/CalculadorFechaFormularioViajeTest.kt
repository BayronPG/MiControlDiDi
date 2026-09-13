package com.jhon.micontroldidi.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculadorFechaFormularioViajeTest {

    private val ahora = 1_800_000_000_000L
    private val fechaOriginal = 1_700_000_000_000L

    @Test
    fun `en creacion muestra la fecha actual`() {
        val resultado = CalculadorFechaFormularioViaje.fechaVisible(
            esEdicion = false,
            editando = false,
            fechaHoraOriginal = 0L,
            ahora = ahora
        )

        assertEquals(ahora, resultado)
    }

    @Test
    fun `en edicion cargada muestra la fecha original`() {
        val resultado = CalculadorFechaFormularioViaje.fechaVisible(
            esEdicion = true,
            editando = true,
            fechaHoraOriginal = fechaOriginal,
            ahora = ahora
        )

        assertEquals(fechaOriginal, resultado)
    }

    @Test
    fun `en edicion sin cargar no muestra fecha`() {
        val resultado = CalculadorFechaFormularioViaje.fechaVisible(
            esEdicion = true,
            editando = false,
            fechaHoraOriginal = 0L,
            ahora = ahora
        )

        assertNull(resultado)
    }

    @Test
    fun `en edicion con fecha original cero no muestra fecha`() {
        val resultado = CalculadorFechaFormularioViaje.fechaVisible(
            esEdicion = true,
            editando = true,
            fechaHoraOriginal = 0L,
            ahora = ahora
        )

        assertNull(resultado)
    }
}
