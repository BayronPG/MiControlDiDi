package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ViajeRepositoryTest {

    private lateinit var repository: ViajeRepository
    private var ultimoViajeInsertado: ViajeEntity? = null

    private val daoFalso = object : ViajeDao {
        override suspend fun insertar(viaje: ViajeEntity): Long {
            ultimoViajeInsertado = viaje
            return 1L
        }

        override fun obtenerTodos(): Flow<List<ViajeEntity>> {
            return flowOf(emptyList())
        }
    }

    @Before
    fun setUp() {
        repository = ViajeRepository(daoFalso)
        ultimoViajeInsertado = null
    }

    @Test
    fun `insertar viaje valido retorna exito con id`() = runTest {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = 15000, propina = 2000)
        val resultado = repository.insertar(viaje)
        assertTrue("Viaje válido debe retornar success", resultado.isSuccess)
        assertEquals(1L, resultado.getOrNull())
    }

    @Test
    fun `insertar viaje con propina cero es valido`() = runTest {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = 12000, propina = 0)
        val resultado = repository.insertar(viaje)
        assertTrue("Propina cero debe ser válida", resultado.isSuccess)
    }

    @Test
    fun `insertar viaje con valor cero es rechazado`() = runTest {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = 0, propina = 0)
        val resultado = repository.insertar(viaje)
        assertFalse("Valor cero debe ser rechazado", resultado.isSuccess)
        assertTrue(
            resultado.exceptionOrNull()?.message?.contains("mayor que cero") ?: false
        )
    }

    @Test
    fun `insertar viaje con valor negativo es rechazado`() = runTest {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = -5000, propina = 0)
        val resultado = repository.insertar(viaje)
        assertFalse("Valor negativo debe ser rechazado", resultado.isSuccess)
        assertTrue(
            resultado.exceptionOrNull()?.message?.contains("mayor que cero") ?: false
        )
    }

    @Test
    fun `insertar viaje con propina negativa es rechazado`() = runTest {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = 10000, propina = -1000)
        val resultado = repository.insertar(viaje)
        assertFalse("Propina negativa debe ser rechazada", resultado.isSuccess)
        assertTrue(
            resultado.exceptionOrNull()?.message?.contains("no puede ser negativa") ?: false
        )
    }

    @Test
    fun `obtenerTodos retorna los viajes del DAO sin modificar`() = runTest {
        val viajesEsperados = listOf(
            ViajeEntity(2, 2000L, 10000, 0, ""),
            ViajeEntity(1, 1000L, 15000, 0, "Segundo")
        )
        val dao = object : ViajeDao {
            override suspend fun insertar(viaje: ViajeEntity): Long = 1L
            override fun obtenerTodos(): Flow<List<ViajeEntity>> = flowOf(viajesEsperados)
        }
        val repo = ViajeRepository(dao)
        var listaRecibida: List<ViajeEntity>? = null
        repo.obtenerTodos().collect { lista ->
            listaRecibida = lista
        }
        assertEquals(2, listaRecibida?.size)
        assertEquals(2000L, listaRecibida?.get(0)?.fechaHora)
        assertEquals("Segundo", listaRecibida?.get(1)?.observacion)
    }
}
