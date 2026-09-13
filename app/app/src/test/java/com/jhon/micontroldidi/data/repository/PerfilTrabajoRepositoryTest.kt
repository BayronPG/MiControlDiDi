package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PerfilTrabajoRepositoryTest {

    private var perfilGuardado: PerfilTrabajoEntity? = null
    private var errorSimulado: Exception? = null
    private var observaciones = 0
    private var consultas = 0

    private val daoFalso = object : PerfilTrabajoDao {
        override fun observar(id: Int): Flow<PerfilTrabajoEntity?> {
            observaciones++
            return flowOf(perfilGuardado)
        }

        override suspend fun obtener(id: Int): PerfilTrabajoEntity? {
            consultas++
            return perfilGuardado
        }

        override suspend fun guardar(perfil: PerfilTrabajoEntity) {
            errorSimulado?.let { throw it }
            perfilGuardado = perfil
        }
    }

    private lateinit var repository: PerfilTrabajoRepository

    @Before
    fun setUp() {
        perfilGuardado = null
        errorSimulado = null
        observaciones = 0
        consultas = 0
        repository = PerfilTrabajoRepository(daoFalso)
    }

    @Test
    fun `guardar perfil valido retorna exito`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity())

        assertTrue(resultado.isSuccess)
        assertNotNull(perfilGuardado)
    }

    @Test
    fun `guardar fuerza el id unico del perfil`() = runTest {
        repository.guardar(PerfilTrabajoEntity(id = 99))

        assertEquals(PerfilTrabajoEntity.ID_UNICO, perfilGuardado!!.id)
    }

    @Test
    fun `observar delega con el id unico`() = runTest {
        repository.observar().collect { }

        assertEquals(1, observaciones)
    }

    @Test
    fun `obtener delega con el id unico`() = runTest {
        repository.obtener()

        assertEquals(1, consultas)
    }

    @Test
    fun `plataforma en blanco es rechazada`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity(plataforma = "   "))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("plataforma") == true)
        assertEquals(null, perfilGuardado)
    }

    @Test
    fun `vehiculo en blanco es rechazado`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity(vehiculo = ""))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("vehículo") == true)
    }

    @Test
    fun `ciudad en blanco es rechazada`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity(ciudad = ""))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("ciudad") == true)
    }

    @Test
    fun `perfil sin dias laborales es rechazado`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity(diasLaborales = ""))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("día") == true)
    }

    @Test
    fun `hora de inicio posterior a la de fin es rechazada`() = runTest {
        val resultado = repository.guardar(
            PerfilTrabajoEntity(horaInicioMinutos = 900, horaFinMinutos = 360)
        )

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("anterior") == true)
    }

    @Test
    fun `hora fuera del dia es rechazada`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity(horaInicioMinutos = 1440))

        assertFalse(resultado.isSuccess)
    }

    @Test
    fun `porcentaje de kilometros vacios fuera de rango es rechazado`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity(maxPorcentajeKmVacios = 101))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("100") == true)
    }

    @Test
    fun `porcentaje negativo es rechazado`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity(maxPorcentajeKmVacios = -1))

        assertFalse(resultado.isSuccess)
    }

    @Test
    fun `costo negativo es rechazado`() = runTest {
        val resultado = repository.guardar(
            PerfilTrabajoEntity(costoAceite = -1, intervaloAceiteKm = 3000)
        )

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("negativo") == true)
    }

    @Test
    fun `intervalo negativo es rechazado`() = runTest {
        val resultado = repository.guardar(PerfilTrabajoEntity(intervaloLlantasKm = -100))

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("negativo") == true)
    }

    @Test
    fun `costo con intervalo cero es rechazado`() = runTest {
        val resultado = repository.guardar(
            PerfilTrabajoEntity(costoFrenos = 80000, intervaloFrenosKm = 0)
        )

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("mayor que cero") == true)
    }

    @Test
    fun `intervalo cero sin costo es aceptado`() = runTest {
        val resultado = repository.guardar(
            PerfilTrabajoEntity(costoFrenos = 0, intervaloFrenosKm = 0)
        )

        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `reservas completas son aceptadas`() = runTest {
        val resultado = repository.guardar(
            PerfilTrabajoEntity(
                costoAceite = 60000, intervaloAceiteKm = 3000,
                costoLlantas = 180000, intervaloLlantasKm = 12000,
                costoFrenos = 80000, intervaloFrenosKm = 8000,
                costoKitArrastre = 90000, intervaloKitArrastreKm = 9000,
                costoMantenimiento = 40000, intervaloMantenimientoKm = 4000,
                costoDepreciacion = 240000, intervaloDepreciacionKm = 24000
            )
        )

        assertTrue(resultado.isSuccess)
        assertEquals(75L, perfilGuardado!!.reservaTotalPorKm)
    }

    @Test
    fun `fallo del dao retorna failure`() = runTest {
        errorSimulado = IllegalStateException("base de datos caída")

        val resultado = repository.guardar(PerfilTrabajoEntity())

        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull() is IllegalStateException)
    }
}
