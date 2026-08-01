package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.MetaDao
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import com.jhon.micontroldidi.domain.PeriodoMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MetaRepositoryTest {

    private var metaGuardada: MetaEntity? = null
    private var metaActivaDevuelta: MetaEntity? = null
    private var desactivarLlamadas = 0
    private var errorSimulado: Exception? = null

    private val daoFalso = object : MetaDao {
        override suspend fun insertar(meta: MetaEntity): Long {
            errorSimulado?.let { throw it }
            metaGuardada = meta
            return 1L
        }

        override fun obtenerActiva(): Flow<MetaEntity?> = flowOf(metaActivaDevuelta)

        override suspend fun obtenerUltima(): MetaEntity? = metaActivaDevuelta

        override suspend fun desactivarTodas() {
            desactivarLlamadas++
        }

        override suspend fun actualizar(id: Long, tipoPeriodo: String, valorObjetivo: Long): Int {
            errorSimulado?.let { throw it }
            metaActivaDevuelta = metaActivaDevuelta?.copy(tipoPeriodo = tipoPeriodo, valorObjetivo = valorObjetivo)
            return 1
        }

        override suspend fun eliminar(id: Long): Int {
            errorSimulado?.let { throw it }
            metaActivaDevuelta = null
            return 1
        }
    }

    private lateinit var repository: MetaRepository

    @Before
    fun setUp() {
        metaGuardada = null
        metaActivaDevuelta = null
        desactivarLlamadas = 0
        errorSimulado = null
        repository = MetaRepository(daoFalso)
    }

    @Test
    fun `guardar meta valida retorna exito`() = runTest {
        val resultado = repository.guardar(PeriodoMeta.DIA, 50000)
        assertTrue(resultado.isSuccess)
        assertEquals(1L, resultado.getOrNull())
        assertNotNull(metaGuardada)
        assertEquals("DIA", metaGuardada!!.tipoPeriodo)
        assertEquals(50000L, metaGuardada!!.valorObjetivo)
    }

    @Test
    fun `guardar meta manda desactivar las anteriores`() = runTest {
        repository.guardar(PeriodoMeta.DIA, 50000)
        assertEquals(1, desactivarLlamadas)
    }

    @Test
    fun `guardar meta mensual`() = runTest {
        val resultado = repository.guardar(PeriodoMeta.MES, 1500000)
        assertTrue(resultado.isSuccess)
        assertEquals("MES", metaGuardada!!.tipoPeriodo)
        assertEquals(1500000L, metaGuardada!!.valorObjetivo)
    }

    @Test
    fun `guardar meta con valor cero es rechazado`() = runTest {
        val resultado = repository.guardar(PeriodoMeta.DIA, 0)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("mayor que cero") == true)
    }

    @Test
    fun `guardar meta con valor negativo es rechazado`() = runTest {
        val resultado = repository.guardar(PeriodoMeta.DIA, -1000)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("mayor que cero") == true)
    }

    @Test
    fun `fromNombre devuelve null para periodo desconocido`() {
        assertEquals(null, PeriodoMeta.fromNombre("SEMANA"))
        assertEquals(PeriodoMeta.DIA, PeriodoMeta.fromNombre("DIA"))
        assertEquals(PeriodoMeta.MES, PeriodoMeta.fromNombre("MES"))
    }

    @Test
    fun `obtenerActiva devuelve null cuando no hay meta`() = runTest {
        metaActivaDevuelta = null
        var resultado: MetaEntity? = null
        repository.obtenerActiva().collect { resultado = it }
        assertEquals(null, resultado)
    }

    @Test
    fun `obtenerActiva devuelve la meta activa`() = runTest {
        metaActivaDevuelta = MetaEntity(id = 1, tipoPeriodo = "DIA", valorObjetivo = 60000, activa = true)
        var resultado: MetaEntity? = null
        repository.obtenerActiva().collect { resultado = it }
        assertNotNull(resultado)
        assertEquals(60000L, resultado!!.valorObjetivo)
    }

    @Test
    fun `actualizar meta existente retorna exito`() = runTest {
        metaActivaDevuelta = MetaEntity(id = 1, tipoPeriodo = "DIA", valorObjetivo = 50000)
        val resultado = repository.actualizar(1, PeriodoMeta.MES, 2000000)
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `eliminar meta existente retorna exito`() = runTest {
        metaActivaDevuelta = MetaEntity(id = 1, tipoPeriodo = "DIA", valorObjetivo = 50000)
        val resultado = repository.eliminar(1)
        assertTrue(resultado.isSuccess)
    }
}
