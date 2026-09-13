package com.jhon.micontroldidi.ui.settings

import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilTrabajoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val perfilFlow = MutableStateFlow<PerfilTrabajoEntity?>(null)
    private var guardarLlamadas = 0
    private var errorSimulado: Exception? = null
    private var perfilEscrito: PerfilTrabajoEntity? = null

    private val clockFijo: Clock = Clock.fixed(
        Instant.parse("2026-09-12T17:00:00Z"),
        ZoneId.of("America/Bogota")
    )

    private val daoFalso = object : PerfilTrabajoDao {
        override fun observar(id: Int): Flow<PerfilTrabajoEntity?> = perfilFlow

        override suspend fun obtener(id: Int): PerfilTrabajoEntity? = perfilFlow.value

        override suspend fun guardar(perfil: PerfilTrabajoEntity) {
            guardarLlamadas++
            errorSimulado?.let { throw it }
            perfilEscrito = perfil
            perfilFlow.value = perfil
        }
    }

    private lateinit var viewModel: PerfilTrabajoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        perfilFlow.value = null
        guardarLlamadas = 0
        errorSimulado = null
        perfilEscrito = null
        val repository = PerfilTrabajoRepository(daoFalso)
        viewModel = PerfilTrabajoViewModel(repository, FakeResourceProvider(), clockFijo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carga el perfil existente`() = runTest {
        perfilFlow.value = PerfilTrabajoEntity(
            plataforma = "DiDi",
            vehiculo = "Yamaha FZ",
            ciudad = "Bogotá",
            diasLaborales = "1,3,5",
            horaInicioMinutos = 420,
            horaFinMinutos = 1080,
            maxPorcentajeKmVacios = 30,
            costoAceite = 60000,
            intervaloAceiteKm = 3000
        )

        viewModel = PerfilTrabajoViewModel(PerfilTrabajoRepository(daoFalso), FakeResourceProvider(), clockFijo)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertEquals("DiDi", estado.valor(CampoPerfil.PLATAFORMA))
        assertEquals("Yamaha FZ", estado.valor(CampoPerfil.VEHICULO))
        assertEquals("Bogotá", estado.valor(CampoPerfil.CIUDAD))
        assertEquals(setOf(1, 3, 5), estado.diasSeleccionados)
        assertEquals("07:00", estado.valor(CampoPerfil.HORA_INICIO))
        assertEquals("18:00", estado.valor(CampoPerfil.HORA_FIN))
        assertEquals("30", estado.valor(CampoPerfil.MAX_KM_VACIOS))
        assertEquals("60000", estado.valor(CampoPerfil.COSTO_ACEITE))
        assertEquals("3000", estado.valor(CampoPerfil.INTERVALO_ACEITE))
        assertEquals(20L, estado.reservaPorKm(RESERVAS.first()))
    }

    @Test
    fun `sin perfil en la base usa los valores por defecto`() = runTest {
        perfilFlow.value = null
        viewModel = PerfilTrabajoViewModel(PerfilTrabajoRepository(daoFalso), FakeResourceProvider(), clockFijo)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertEquals("inDrive", estado.valor(CampoPerfil.PLATAFORMA))
        assertEquals("06:00", estado.valor(CampoPerfil.HORA_INICIO))
        assertEquals("15:00", estado.valor(CampoPerfil.HORA_FIN))
        assertEquals(setOf(1, 2, 3, 4, 5), estado.diasSeleccionados)
    }

    @Test
    fun `actualizar un campo cambia el valor`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.CIUDAD, "Cali")

        assertEquals("Cali", viewModel.uiState.value.valor(CampoPerfil.CIUDAD))
        assertNull(viewModel.uiState.value.error(CampoPerfil.CIUDAD))
    }

    @Test
    fun `alternar dia lo agrega y lo quita`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.estaSeleccionado(6))

        viewModel.alternarDia(6)
        assertTrue(viewModel.uiState.value.estaSeleccionado(6))

        viewModel.alternarDia(6)
        assertFalse(viewModel.uiState.value.estaSeleccionado(6))
    }

    @Test
    fun `quitar todos los dias deja error de dias`() = runTest {
        advanceUntilIdle()

        viewModel.alternarDia(1)
        viewModel.alternarDia(2)
        viewModel.alternarDia(3)
        viewModel.alternarDia(4)
        viewModel.alternarDia(5)

        val estado = viewModel.uiState.value
        assertTrue(estado.diasSeleccionados.isEmpty())
        assertNotNull(estado.errorDias)
        assertFalse(estado.formularioValido)
    }

    @Test
    fun `plataforma vacia produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.PLATAFORMA, "")

        assertNotNull(viewModel.uiState.value.error(CampoPerfil.PLATAFORMA))
        assertFalse(viewModel.uiState.value.formularioValido)
    }

    @Test
    fun `hora con formato invalido produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.HORA_INICIO, "6am")

        assertNotNull(viewModel.uiState.value.error(CampoPerfil.HORA_INICIO))
    }

    @Test
    fun `hora de inicio posterior a la de fin produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.HORA_INICIO, "16:00")

        assertNotNull(viewModel.uiState.value.error(CampoPerfil.HORA_FIN))
        assertFalse(viewModel.uiState.value.formularioValido)
    }

    @Test
    fun `porcentaje fuera de rango produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.MAX_KM_VACIOS, "120")

        assertNotNull(viewModel.uiState.value.error(CampoPerfil.MAX_KM_VACIOS))
    }

    @Test
    fun `costo con intervalo cero produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.COSTO_ACEITE, "60000")
        viewModel.actualizarCampo(CampoPerfil.INTERVALO_ACEITE, "0")

        assertNotNull(viewModel.uiState.value.error(CampoPerfil.INTERVALO_ACEITE))
    }

    @Test
    fun `reserva total se calcula desde el formulario`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.COSTO_ACEITE, "60000")
        viewModel.actualizarCampo(CampoPerfil.INTERVALO_ACEITE, "3000")
        viewModel.actualizarCampo(CampoPerfil.COSTO_LLANTAS, "180000")
        viewModel.actualizarCampo(CampoPerfil.INTERVALO_LLANTAS, "12000")

        assertEquals(35L, viewModel.uiState.value.reservaTotalPorKm)
    }

    @Test
    fun `guardar con datos validos persiste y marca exito`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.COSTO_ACEITE, "60000")
        viewModel.actualizarCampo(CampoPerfil.INTERVALO_ACEITE, "3000")
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(1, guardarLlamadas)
        assertNotNull(perfilEscrito)
        assertEquals(PerfilTrabajoEntity.ID_UNICO, perfilEscrito!!.id)
        assertEquals(60000L, perfilEscrito!!.costoAceite)
        assertEquals(3000L, perfilEscrito!!.intervaloAceiteKm)
        assertEquals("1,2,3,4,5", perfilEscrito!!.diasLaborales)
        assertEquals(clockFijo.millis(), perfilEscrito!!.actualizadoEn)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
        assertNull(viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `guardar con datos invalidos no persiste`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.VEHICULO, "")
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(0, guardarLlamadas)
        assertNotNull(viewModel.uiState.value.error(CampoPerfil.VEHICULO))
        assertFalse(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `guardar sin dias no persiste`() = runTest {
        advanceUntilIdle()

        listOf(1, 2, 3, 4, 5).forEach { viewModel.alternarDia(it) }
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(0, guardarLlamadas)
        assertNotNull(viewModel.uiState.value.errorDias)
    }

    @Test
    fun `doble clic en guardar solo persiste una vez`() = runTest {
        advanceUntilIdle()

        viewModel.guardar()
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(1, guardarLlamadas)
    }

    @Test
    fun `fallo al guardar muestra mensaje descriptivo`() = runTest {
        advanceUntilIdle()
        errorSimulado = IllegalStateException("disco lleno")

        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(
            "No se pudo guardar el perfil. Inténtalo de nuevo.",
            viewModel.uiState.value.mensajeError
        )
        assertFalse(viewModel.uiState.value.guardadoExitoso)
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun `limpiar estado transitorio quita el mensaje de guardado`() = runTest {
        advanceUntilIdle()

        viewModel.guardar()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.guardadoExitoso)

        viewModel.limpiarEstadoTransitorio()

        assertFalse(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `las horas se guardan como minutos desde la medianoche`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoPerfil.HORA_INICIO, "06:30")
        viewModel.actualizarCampo(CampoPerfil.HORA_FIN, "14:45")
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(390, perfilEscrito!!.horaInicioMinutos)
        assertEquals(885, perfilEscrito!!.horaFinMinutos)
    }
}
