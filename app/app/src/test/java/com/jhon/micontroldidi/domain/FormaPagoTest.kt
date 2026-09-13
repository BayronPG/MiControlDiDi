package com.jhon.micontroldidi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormaPagoTest {

    @Test
    fun `el catalogo tiene las cuatro formas previstas`() {
        assertEquals(
            listOf("EFECTIVO", "TRANSFERENCIA", "TARJETA", "OTRO"),
            FormaPago.entries.map { it.nombre }
        )
    }

    @Test
    fun `fromNombre reconoce el valor persistido`() {
        FormaPago.entries.forEach { forma ->
            assertEquals(forma, FormaPago.fromNombre(forma.nombre))
        }
    }

    @Test
    fun `fromNombre no distingue mayusculas`() {
        assertEquals(FormaPago.EFECTIVO, FormaPago.fromNombre("efectivo"))
        assertEquals(FormaPago.TRANSFERENCIA, FormaPago.fromNombre("Transferencia"))
    }

    @Test
    fun `fromNombre devuelve null con cadena vacia`() {
        assertNull(FormaPago.fromNombre(""))
    }

    @Test
    fun `fromNombre devuelve null con nulo`() {
        assertNull(FormaPago.fromNombre(null))
    }

    @Test
    fun `fromNombre devuelve null con un valor desconocido`() {
        assertNull(FormaPago.fromNombre("BITCOIN"))
    }
}
