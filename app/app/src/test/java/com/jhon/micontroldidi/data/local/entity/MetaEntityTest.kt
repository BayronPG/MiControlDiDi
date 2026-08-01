package com.jhon.micontroldidi.data.local.entity

import com.jhon.micontroldidi.domain.PeriodoMeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetaEntityTest {

    @Test
    fun `crear meta diaria con valores minimos`() {
        val meta = MetaEntity(tipoPeriodo = "DIA", valorObjetivo = 50000)
        assertEquals("DIA", meta.tipoPeriodo)
        assertEquals(50000L, meta.valorObjetivo)
        assertTrue(meta.activa)
        assertTrue(meta.createdAt > 0)
    }

    @Test
    fun `crear meta mensual`() {
        val meta = MetaEntity(tipoPeriodo = "MES", valorObjetivo = 1500000)
        assertEquals("MES", meta.tipoPeriodo)
        assertEquals(1500000L, meta.valorObjetivo)
        assertTrue(meta.activa)
    }

    @Test
    fun `meta puede estar inactiva`() {
        val meta = MetaEntity(tipoPeriodo = "DIA", valorObjetivo = 50000, activa = false)
        assertFalse(meta.activa)
    }

    @Test
    fun `meta con id explicito`() {
        val meta = MetaEntity(id = 5, tipoPeriodo = "DIA", valorObjetivo = 100000)
        assertEquals(5L, meta.id)
    }

    @Test
    fun `id por defecto es cero`() {
        val meta = MetaEntity(tipoPeriodo = "DIA", valorObjetivo = 50000)
        assertEquals(0L, meta.id)
    }

    @Test
    fun `propiedad periodo mapea desde tipoPeriodo`() {
        assertEquals(PeriodoMeta.DIA, MetaEntity(tipoPeriodo = "DIA", valorObjetivo = 50000).periodo)
        assertEquals(PeriodoMeta.MES, MetaEntity(tipoPeriodo = "MES", valorObjetivo = 50000).periodo)
    }

    @Test
    fun `propiedad periodo degrada a DIA con dato desconocido`() {
        assertEquals(PeriodoMeta.DIA, MetaEntity(tipoPeriodo = "SEMANA", valorObjetivo = 50000).periodo)
    }
}
