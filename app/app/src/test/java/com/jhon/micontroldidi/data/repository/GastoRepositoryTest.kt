package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.GastoDao
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GastoRepositoryTest {

    private var insertarLlamadas = 0
    private var ultimoInicio = 0L
    private var ultimoFin = 0L

    private val daoFalso = object : GastoDao {
        override suspend fun insertar(gasto: GastoEntity): Long {
            insertarLlamadas++
            return 1L
        }
        override suspend fun actualizar(id: Long, fechaHora: Long, categoriaId: Long, valor: Long, descripcion: String): Int = 1
        override suspend fun eliminar(id: Long): Int = 1
        override suspend fun obtenerPorId(id: Long): GastoConCategoria? = null
        override fun obtenerTodos(): Flow<List<GastoConCategoria>> = flowOf(emptyList())
        override fun obtenerPorRango(
            inicioInclusivo: Long, finExclusivo: Long, categoriaId: Long?
        ): Flow<List<GastoConCategoria>> = flowOf(emptyList())

        override fun obtenerTotalGastosPorRango(
            inicioInclusivo: Long, finExclusivo: Long
        ): Flow<Long> {
            ultimoInicio = inicioInclusivo
            ultimoFin = finExclusivo
            return flowOf(7777L)
        }
    }

    private lateinit var repository: GastoRepository

    @Before
    fun setUp() {
        insertarLlamadas = 0
        repository = GastoRepository(daoFalso)
    }

    @Test
    fun `gasto con valor valido es aceptado`() = runTest {
        val g = GastoEntity(fechaHora = 1000L, categoriaId = 1, valor = 5000)
        val resultado = repository.insertar(g)
        assertTrue(resultado.isSuccess)
        assertEquals(1, insertarLlamadas)
    }

    @Test
    fun `gasto con valor cero es rechazado`() = runTest {
        val g = GastoEntity(fechaHora = 1000L, categoriaId = 1, valor = 0)
        val resultado = repository.insertar(g)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("mayor que cero") == true)
    }

    @Test
    fun `gasto con valor negativo es rechazado`() = runTest {
        val g = GastoEntity(fechaHora = 1000L, categoriaId = 1, valor = -1000)
        val resultado = repository.insertar(g)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("mayor que cero") == true)
    }

    @Test
    fun `gasto con descripcion vacia es valido`() = runTest {
        val g = GastoEntity(fechaHora = 1000L, categoriaId = 1, valor = 3000, descripcion = "")
        val resultado = repository.insertar(g)
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `gasto con categoriaId cero es rechazado`() = runTest {
        val g = GastoEntity(fechaHora = 1000L, categoriaId = 0, valor = 5000)
        val resultado = repository.insertar(g)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("categoría") == true)
    }

    @Test
    fun `actualizar gasto valido retorna exito`() = runTest {
        val g = GastoEntity(id = 1, fechaHora = 1000L, categoriaId = 1, valor = 5000)
        val resultado = repository.actualizar(g)
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `actualizar gasto con valor cero es rechazado`() = runTest {
        val g = GastoEntity(id = 1, fechaHora = 1000L, categoriaId = 1, valor = 0)
        val resultado = repository.actualizar(g)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("mayor que cero") == true)
    }

    @Test
    fun `actualizar gasto con categoriaId cero es rechazado`() = runTest {
        val g = GastoEntity(id = 1, fechaHora = 1000L, categoriaId = 0, valor = 5000)
        val resultado = repository.actualizar(g)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("categoría") == true)
    }

    @Test
    fun `eliminar gasto retorna exito`() = runTest {
        val resultado = repository.eliminar(1L)
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `obtenerTotalGastosPorRango delega limites al DAO`() = runTest {
        val flujo = repository.obtenerTotalGastosPorRango(2000L, 8888L)
        var resultado: Long? = null
        flujo.collect { resultado = it }
        assertEquals(7777L, resultado)
        assertEquals(2000L, ultimoInicio)
        assertEquals(8888L, ultimoFin)
    }

    @Test
    fun `obtenerTotalGastosPorRango expone el Flow sin transformar`() = runTest {
        val flujo = repository.obtenerTotalGastosPorRango(0L, 5000L)
        assertEquals(0L, ultimoInicio)
        assertEquals(5000L, ultimoFin)
        var resultado: Long? = null
        flujo.collect { resultado = it }
        assertEquals(7777L, resultado)
    }

    @Test
    fun `obtenerPorRango delega al DAO y expone el Flow`() = runTest {
        val flujo = repository.obtenerPorRango(1000L, 9999L)
        var resultado: List<GastoConCategoria>? = null
        flujo.collect { resultado = it }
        assertEquals(0, resultado?.size)
    }

    @Test
    fun `obtenerPorRango con categoriaId delega al DAO`() = runTest {
        val flujo = repository.obtenerPorRango(0L, 5000L, categoriaId = 1L)
        var resultado: List<GastoConCategoria>? = null
        flujo.collect { resultado = it }
        assertTrue(resultado != null)
    }
}
