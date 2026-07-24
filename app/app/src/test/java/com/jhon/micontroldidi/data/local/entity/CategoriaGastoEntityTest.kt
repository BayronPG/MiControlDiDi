package com.jhon.micontroldidi.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoriaGastoEntityTest {

    @Test
    fun `categoria con nombre valido`() {
        val c = CategoriaGastoEntity(nombre = "Gasolina")
        assertEquals("Gasolina", c.nombre)
        assertTrue(c.activa)
    }

    @Test
    fun `categoria puede crearse inactiva`() {
        val c = CategoriaGastoEntity(nombre = "Otros", activa = false)
        assertFalse(c.activa)
    }

    @Test
    fun `id por defecto es cero`() {
        val c = CategoriaGastoEntity(nombre = "Prueba")
        assertEquals(0L, c.id)
    }
}
