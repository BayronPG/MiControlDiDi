package com.jhon.micontroldidi.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TanqueoEntityTest {

    @Test
    fun `litros y precio por litro se calculan desde el importe`() {
        val tanqueo = TanqueoEntity(
            fechaHora = 1000L,
            odometroMetros = 12500000L,
            litrosMililitros = 4000L,
            importePagado = 60000L,
            tipoCombustible = "Extra"
        )

        assertEquals(4L, tanqueo.litros)
        assertEquals(15000L, tanqueo.precioLitro)
    }

    @Test
    fun `precio por litro es cero cuando no hay mililitros`() {
        val tanqueo = TanqueoEntity(
            fechaHora = 1000L,
            odometroMetros = 0L,
            litrosMililitros = 0L,
            importePagado = 0L
        )

        assertEquals(0L, tanqueo.precioLitro)
    }

    @Test
    fun `el importe pagado no se reparte entre litros al persistir`() {
        val tanqueo = TanqueoEntity(
            fechaHora = 1000L,
            odometroMetros = 0L,
            litrosMililitros = 3000L,
            importePagado = 47000L
        )

        // 47000 * 1000 / 3000 = 15666 (entero, sin decimales)
        assertEquals(15666L, tanqueo.precioLitro)
    }

    @Test
    fun `valores por defecto del tanqueo`() {
        val tanqueo = TanqueoEntity(
            fechaHora = 1000L,
            odometroMetros = 0L,
            litrosMililitros = 1000L,
            importePagado = 16000L
        )

        assertTrue("Un tanqueo nuevo es de tanque lleno por defecto", tanqueo.esLleno)
        assertEquals("", tanqueo.tipoCombustible)
        assertEquals("", tanqueo.observacion)
        assertEquals(0L, tanqueo.gastoId)
    }

    @Test
    fun `no se persisten valores decimales`() {
        val tipos = TanqueoEntity::class.java.declaredFields.map { it.type.simpleName }.toSet()

        assertFalse("No debe haber Double", tipos.contains("Double"))
        assertFalse("No debe haber Float", tipos.contains("Float"))
    }
}
