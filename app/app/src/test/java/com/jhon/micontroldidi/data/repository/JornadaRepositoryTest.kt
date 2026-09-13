package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.JornadaDao
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JornadaRepositoryTest {

    private var jornadaGuardada: JornadaEntity? = null
    private var errorSimulado: Exception? = null
    private var observacionesTodas = 0
    private var observacionesUltima = 0

    private val daoFalso = object : JornadaDao {
        override suspend fun obtenerPorId(id: Long): JornadaEntity? = null

        override suspend fun cerrar(id: Long, fechaHoraFin: Long, kilometrajeFinalMetros: Long): Int = 0

        override suspend fun insertar(jornada: JornadaEntity): Long {
            errorSimulado?.let { throw it }
            jornadaGuardada = jornada
            return 7L
        }

        override fun observarTodas(): Flow<List<JornadaEntity>> {
            observacionesTodas++
            return flowOf(emptyList())
        }

        override fun observarUltima(): Flow<JornadaEntity?> {
            observacionesUltima++
            return flowOf(jornadaGuardada)
        }
    }

    private lateinit var repository: JornadaRepository

    private fun jornada(
        kilometrajeInicialMetros: Long = 12345000L,
        precioGalonExtra: Long = 17000L,
        metaBrutaDia: Long = 120000L,
        nivelEnergia: Int = 8,
        plataforma: String = "inDrive"
    ) = JornadaEntity(
        fechaHoraInicio = 1000L,
        kilometrajeInicialMetros = kilometrajeInicialMetros,
        precioGalonExtra = precioGalonExtra,
        plataforma = plataforma,
        metaBrutaDia = metaBrutaDia,
        nivelEnergia = nivelEnergia
    )

    @Before
    fun setUp() {
        jornadaGuardada = null
        errorSimulado = null
        observacionesTodas = 0
        observacionesUltima = 0
        repository = JornadaRepository(daoFalso)
    }

    @Test
    fun `insertar jornada valida retorna el id`() = runTest {
        val resultado = repository.insertar(jornada())

        assertTrue(resultado.isSuccess)
        assertEquals(7L, resultado.getOrNull())
        assertTrue(jornadaGuardada != null)
    }

    @Test
    fun `kilometraje negativo es rechazado`() = runTest {
        val resultado = repository.insertar(jornada(kilometrajeInicialMetros = -1L))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("kilometraje") == true)
        assertEquals(null, jornadaGuardada)
    }

    @Test
    fun `kilometraje cero es aceptado`() = runTest {
        val resultado = repository.insertar(jornada(kilometrajeInicialMetros = 0L))

        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `precio del galon en cero es rechazado`() = runTest {
        val resultado = repository.insertar(jornada(precioGalonExtra = 0L))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("galón") == true)
    }

    @Test
    fun `meta bruta en cero es rechazada`() = runTest {
        val resultado = repository.insertar(jornada(metaBrutaDia = 0L))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("meta") == true)
    }

    @Test
    fun `energia mayor que diez es rechazada`() = runTest {
        val resultado = repository.insertar(jornada(nivelEnergia = 11))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("energía") == true)
    }

    @Test
    fun `energia negativa es rechazada`() = runTest {
        val resultado = repository.insertar(jornada(nivelEnergia = -1))

        assertFalse(resultado.isSuccess)
    }

    @Test
    fun `plataforma en blanco es rechazada`() = runTest {
        val resultado = repository.insertar(jornada(plataforma = "   "))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("plataforma") == true)
    }

    @Test
    fun `fallo del dao retorna failure`() = runTest {
        errorSimulado = IllegalStateException("base de datos caída")

        val resultado = repository.insertar(jornada())

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `observarTodas delega en el dao`() = runTest {
        repository.observarTodas().collect { }

        assertEquals(1, observacionesTodas)
    }

    @Test
    fun `observarUltima delega en el dao`() = runTest {
        repository.observarUltima().collect { }

        assertEquals(1, observacionesUltima)
    }
}
