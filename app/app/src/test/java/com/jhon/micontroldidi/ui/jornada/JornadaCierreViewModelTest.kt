package com.jhon.micontroldidi.ui.jornada

import com.jhon.micontroldidi.data.local.dao.JornadaDao
import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.repository.JornadaRepository
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.util.FakeResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class JornadaCierreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val perfil = MutableStateFlow<PerfilTrabajoEntity?>(PerfilTrabajoEntity())
    private var cerrarLlamadas = 0
    private var finRecibido = 0L
    private var kmRecibido = 0L

    private val clockFijo: Clock = Clock.fixed(
        Instant.parse("2026-09-13T20:00:00Z"),
        ZoneId.of("America/Bogota")
    )

    private fun jornadaDeHoy(cerrada: Boolean = false) = JornadaEntity(
        id = 1L,
        fechaHoraInicio = clockFijo.millis() - 3600_000L,
        kilometrajeInicialMetros = 10_000_000L,
        precioGalonExtra = 17000L,
        plataforma = "inDrive",
        metaBrutaDia = 120000L,
        nivelEnergia = 8,
        fechaHoraFin = if (cerrada) clockFijo.millis() else 0L,
        kilometrajeFinalMetros = if (cerrada) 10_045_000L else 0L
    )

    private val ultimaJornada = MutableStateFlow<JornadaEntity?>(null)

    private val jornadaDaoFalso = object : JornadaDao {
        override suspend fun insertar(jornada: JornadaEntity): Long = 1L
        override fun observarTodas(): Flow<List<JornadaEntity>> =
            MutableStateFlow(listOfNotNull(ultimaJornada.value))
        override fun observarUltima(): Flow<JornadaEntity?> = ultimaJornada
        override suspend fun obtenerPorId(id: Long): JornadaEntity? = ultimaJornada.value
        override suspend fun cerrar(
            id: Long,
            fechaHoraFin: Long,
            kilometrajeFinalMetros: Long
        ): Int {
            cerrarLlamadas++
            finRecibido = fechaHoraFin
            kmRecibido = kilometrajeFinalMetros
            return 1
        }
    }

    private val perfilDaoFalso = object : PerfilTrabajoDao {
        override fun observar(id: Int): Flow<PerfilTrabajoEntity?> = perfil
        override suspend fun obtener(id: Int): PerfilTrabajoEntity? = perfil.value
        override suspend fun guardar(perfilNuevo: PerfilTrabajoEntity) {
            perfil.value = perfilNuevo
        }
    }

    private lateinit var viewModel: JornadaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        perfil.value = PerfilTrabajoEntity()
        cerrarLlamadas = 0
        finRecibido = 0L
        kmRecibido = 0L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel() = JornadaViewModel(
        JornadaRepository(jornadaDaoFalso),
        PerfilTrabajoRepository(perfilDaoFalso),
        FakeResourceProvider(),
        clockFijo
    )

    @Test
    fun `cerrar la jornada envia la hora del reloj y los metros`() = runTest {
        ultimaJornada.value = jornadaDeHoy()
        viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.actualizarOdometroFinal("10045")
        viewModel.cerrarJornada()
        advanceUntilIdle()

        assertEquals(1, cerrarLlamadas)
        assertEquals(clockFijo.millis(), finRecibido)
        assertEquals(10_045_000L, kmRecibido)
        assertEquals("", viewModel.uiState.value.odometroFinalText)
    }

    @Test
    fun `cerrar sin odometro final produce error`() = runTest {
        ultimaJornada.value = jornadaDeHoy()
        viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.cerrarJornada()
        advanceUntilIdle()

        assertEquals(0, cerrarLlamadas)
        assertNotNull(viewModel.uiState.value.errorOdometroFinal)
    }

    @Test
    fun `un odometro final menor que el inicial produce error`() = runTest {
        ultimaJornada.value = jornadaDeHoy()
        viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.actualizarOdometroFinal("9999")
        viewModel.cerrarJornada()
        advanceUntilIdle()

        assertEquals(0, cerrarLlamadas)
        assertEquals(
            "El odómetro final no puede ser menor que el inicial",
            viewModel.uiState.value.errorOdometroFinal
        )
    }

    @Test
    fun `una jornada ya cerrada no se puede cerrar otra vez`() = runTest {
        ultimaJornada.value = jornadaDeHoy(cerrada = true)
        viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.actualizarOdometroFinal("10050")
        viewModel.cerrarJornada()
        advanceUntilIdle()

        assertEquals("No debe llamarse al cierre", 0, cerrarLlamadas)
        assertEquals(
            "Esta jornada ya está cerrada",
            viewModel.uiState.value.errorCierre
        )
    }

    @Test
    fun `sin jornada de hoy no se cierra nada`() = runTest {
        ultimaJornada.value = null
        viewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.actualizarOdometroFinal("10050")
        viewModel.cerrarJornada()
        advanceUntilIdle()

        assertEquals(0, cerrarLlamadas)
        assertNull(viewModel.uiState.value.errorCierre)
    }
}
