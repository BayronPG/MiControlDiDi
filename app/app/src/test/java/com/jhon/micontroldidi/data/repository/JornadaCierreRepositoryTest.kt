package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.JornadaDao
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JornadaCierreRepositoryTest {

    private val jornada = JornadaEntity(
        id = 1L,
        fechaHoraInicio = 1000L,
        kilometrajeInicialMetros = 10_000_000L,
        precioGalonExtra = 17000L,
        plataforma = "inDrive",
        metaBrutaDia = 120000L,
        nivelEnergia = 8
    )

    private fun daoCon(
        existente: JornadaEntity? = jornada,
        filasCierre: Int = 1
    ) = object : JornadaDao {
        override suspend fun insertar(jornada: JornadaEntity): Long = 1L
        override fun observarTodas(): Flow<List<JornadaEntity>> = flowOf(emptyList())
        override fun observarUltima(): Flow<JornadaEntity?> = flowOf(existente)
        override suspend fun obtenerPorId(id: Long): JornadaEntity? = existente
        override suspend fun cerrar(
            id: Long,
            fechaHoraFin: Long,
            kilometrajeFinalMetros: Long
        ): Int = filasCierre
    }

    @Test
    fun `cerrar con datos validos retorna exito`() = runTest {
        val resultado = JornadaRepository(daoCon()).cerrar(1L, 2000L, 10_045_000L)

        assertTrue("El cierre válido debe tener éxito", resultado.isSuccess)
    }

    @Test
    fun `cerrar con hora anterior al inicio falla`() = runTest {
        val resultado = JornadaRepository(daoCon()).cerrar(1L, 900L, 10_045_000L)

        assertTrue("Una hora de fin anterior al inicio debe rechazarse", resultado.isFailure)
    }

    @Test
    fun `cerrar con odometro final menor que el inicial falla`() = runTest {
        val resultado = JornadaRepository(daoCon()).cerrar(1L, 2000L, 9_999_000L)

        assertTrue("Un odómetro final menor debe rechazarse", resultado.isFailure)
    }

    @Test
    fun `cerrar una jornada ya cerrada falla`() = runTest {
        val resultado = JornadaRepository(daoCon(filasCierre = 0)).cerrar(1L, 2000L, 10_045_000L)

        assertTrue("No se debe poder cerrar dos veces", resultado.isFailure)
    }

    @Test
    fun `cerrar una jornada inexistente falla`() = runTest {
        val resultado = JornadaRepository(daoCon(existente = null)).cerrar(99L, 2000L, 10_045_000L)

        assertTrue("Una jornada inexistente debe rechazarse", resultado.isFailure)
    }

    @Test
    fun `el odometro final igual al inicial es valido`() = runTest {
        val resultado = JornadaRepository(daoCon()).cerrar(1L, 1000L, 10_000_000L)

        assertEquals(true, resultado.isSuccess)
    }
}
