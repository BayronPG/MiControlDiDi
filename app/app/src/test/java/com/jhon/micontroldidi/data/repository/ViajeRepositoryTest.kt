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
    private var ultimoInicio = 0L
    private var ultimoFin = 0L

    private val daoFalso = object : ViajeDao {
        override suspend fun insertar(viaje: ViajeEntity): Long {
            ultimoViajeInsertado = viaje
            return 1L
        }

        override fun obtenerTodos(): Flow<List<ViajeEntity>> {
            return flowOf(emptyList())
        }

        override fun obtenerIngresosPorRango(
            inicioInclusivo: Long, finExclusivo: Long
        ): Flow<Long> {
            ultimoInicio = inicioInclusivo
            ultimoFin = finExclusivo
            return flowOf(9999L)
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
            override fun obtenerIngresosPorRango(
                inicioInclusivo: Long,
                finExclusivo: Long
            ): Flow<Long> = kotlinx.coroutines.flow.flowOf(0L)
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

    @Test
    fun `obtenerIngresosPorRango delega limites al DAO`() = runTest {
        val flujo = repository.obtenerIngresosPorRango(1000L, 9999L)
        var resultado: Long? = null
        flujo.collect { resultado = it }
        assertEquals(9999L, resultado)
        assertEquals(1000L, ultimoInicio)
        assertEquals(9999L, ultimoFin)
    }

    @Test
    fun `obtenerIngresosPorRango expone el Flow sin transformar`() = runTest {
        val flujo = repository.obtenerIngresosPorRango(0L, 1000L)
        assertEquals(0L, ultimoInicio)
        assertEquals(1000L, ultimoFin)
        var resultado: Long? = null
        flujo.collect { resultado = it }
        assertEquals(9999L, resultado)
    }
}
