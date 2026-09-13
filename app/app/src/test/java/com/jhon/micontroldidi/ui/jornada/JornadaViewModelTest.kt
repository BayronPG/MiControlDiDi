package com.jhon.micontroldidi.ui.jornada

import com.jhon.micontroldidi.data.local.dao.JornadaDao
import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.repository.JornadaRepository
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.domain.NivelCombustible
import com.jhon.micontroldidi.domain.PuntoRevision
import com.jhon.micontroldidi.util.FakeResourceProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
class JornadaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val ultimaJornada = MutableStateFlow<JornadaEntity?>(null)
    private val perfil = MutableStateFlow<PerfilTrabajoEntity?>(PerfilTrabajoEntity())
    private var insertarLlamadas = 0
    private var errorSimulado: Exception? = null
    private var errorPerfil: Exception? = null
    private var jornadaInsertada: JornadaEntity? = null

    private val clockFijo: Clock = Clock.fixed(
        Instant.parse("2026-09-13T15:00:00Z"),
        ZoneId.of("America/Bogota")
    )

    private val jornadaDaoFalso = object : JornadaDao {
        override suspend fun insertar(jornada: JornadaEntity): Long {
            insertarLlamadas++
            errorSimulado?.let { throw it }
            jornadaInsertada = jornada
            ultimaJornada.value = jornada.copy(id = insertarLlamadas.toLong())
            return insertarLlamadas.toLong()
        }

        override fun observarTodas(): Flow<List<JornadaEntity>> =
            MutableStateFlow(listOfNotNull(ultimaJornada.value))

        override fun observarUltima(): Flow<JornadaEntity?> = ultimaJornada
    }

    private val perfilDaoFalso = object : PerfilTrabajoDao {
        override fun observar(id: Int): Flow<PerfilTrabajoEntity?> {
            errorPerfil?.let { return flow { throw it } }
            return perfil
        }

        override suspend fun obtener(id: Int): PerfilTrabajoEntity? = perfil.value

        override suspend fun guardar(perfilNuevo: PerfilTrabajoEntity) {
            perfil.value = perfilNuevo
        }
    }

    private lateinit var viewModel: JornadaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ultimaJornada.value = null
        perfil.value = PerfilTrabajoEntity()
        insertarLlamadas = 0
        errorSimulado = null
        errorPerfil = null
        jornadaInsertada = null
        viewModel = crearViewModel()
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

    private fun completarFormulario() {
        viewModel.actualizarCampo(CampoJornada.KILOMETRAJE_INICIAL, "12345")
        viewModel.actualizarCampo(CampoJornada.PRECIO_GALON, "17000")
        viewModel.actualizarCampo(CampoJornada.META_BRUTA, "120000")
        viewModel.actualizarCampo(CampoJornada.NIVEL_ENERGIA, "8")
        viewModel.actualizarCampo(CampoJornada.ZONA_INICIAL, "El Poblado")
        viewModel.actualizarCampo(CampoJornada.CLIMA, "Soleado")
    }

    @Test
    fun `prellena la plataforma con la del perfil`() = runTest {
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertEquals("inDrive", viewModel.uiState.value.valor(CampoJornada.PLATAFORMA))
        assertNull(viewModel.uiState.value.error(CampoJornada.PLATAFORMA))
    }

    @Test
    fun `sin jornada previa no hay jornada de hoy`() = runTest {
        ultimaJornada.value = null
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.jornadaDeHoy)
    }

    @Test
    fun `una jornada de hoy se muestra como jornada de hoy`() = runTest {
        ultimaJornada.value = JornadaEntity(
            id = 1,
            fechaHoraInicio = clockFijo.millis(),
            kilometrajeInicialMetros = 1000L,
            precioGalonExtra = 17000L,
            plataforma = "inDrive",
            metaBrutaDia = 100000L,
            nivelEnergia = 7
        )
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.jornadaDeHoy)
    }

    @Test
    fun `una jornada de ayer no cuenta como la de hoy`() = runTest {
        ultimaJornada.value = JornadaEntity(
            id = 1,
            fechaHoraInicio = clockFijo.millis() - 24L * 60 * 60 * 1000,
            kilometrajeInicialMetros = 1000L,
            precioGalonExtra = 17000L,
            plataforma = "inDrive",
            metaBrutaDia = 100000L,
            nivelEnergia = 7
        )
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.jornadaDeHoy)
    }

    @Test
    fun `el kilometraje vacio produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoJornada.KILOMETRAJE_INICIAL, "")

        assertNotNull(viewModel.uiState.value.error(CampoJornada.KILOMETRAJE_INICIAL))
    }

    @Test
    fun `el kilometraje negativo produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoJornada.KILOMETRAJE_INICIAL, "-5")

        assertEquals(
            "El kilometraje no puede ser negativo",
            viewModel.uiState.value.error(CampoJornada.KILOMETRAJE_INICIAL)
        )
    }

    @Test
    fun `la energia fuera de rango produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoJornada.NIVEL_ENERGIA, "11")

        assertEquals(
            "La energía debe estar entre 0 y 10",
            viewModel.uiState.value.error(CampoJornada.NIVEL_ENERGIA)
        )
    }

    @Test
    fun `la plataforma vacia produce error`() = runTest {
        advanceUntilIdle()

        viewModel.actualizarCampo(CampoJornada.PLATAFORMA, "")

        assertNotNull(viewModel.uiState.value.error(CampoJornada.PLATAFORMA))
        assertFalse(viewModel.uiState.value.formularioValido)
    }

    @Test
    fun `alternar un punto lo marca en mal estado`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.estaEnBuenEstado(PuntoRevision.FRENOS))

        viewModel.alternarRevision(PuntoRevision.FRENOS)

        assertFalse(viewModel.uiState.value.estaEnBuenEstado(PuntoRevision.FRENOS))
        assertEquals(listOf(PuntoRevision.FRENOS), viewModel.uiState.value.puntosEnMalEstado)
    }

    @Test
    fun `los criticos en mal estado se detectan`() = runTest {
        advanceUntilIdle()

        viewModel.alternarRevision(PuntoRevision.FRENOS)
        viewModel.alternarRevision(PuntoRevision.LLANTAS)
        viewModel.alternarRevision(PuntoRevision.AGUA)

        val estado = viewModel.uiState.value
        assertEquals(3, estado.puntosEnMalEstado.size)
        assertEquals(2, estado.puntosCriticosEnMalEstado.size)
    }

    @Test
    fun `guardar con datos validos persiste la jornada`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        viewModel.seleccionarCombustible(NivelCombustible.TRES_CUARTOS)
        viewModel.alternarRevision(PuntoRevision.DOCUMENTOS)

        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(1, insertarLlamadas)
        val guardada = jornadaInsertada!!
        assertEquals(clockFijo.millis(), guardada.fechaHoraInicio)
        assertEquals(12345000L, guardada.kilometrajeInicialMetros)
        assertEquals(17000L, guardada.precioGalonExtra)
        assertEquals(120000L, guardada.metaBrutaDia)
        assertEquals(8, guardada.nivelEnergia)
        assertEquals("El Poblado", guardada.zonaInicial)
        assertEquals("Soleado", guardada.clima)
        assertEquals("inDrive", guardada.plataforma)
        assertEquals(NivelCombustible.TRES_CUARTOS, guardada.nivel)
        assertFalse(guardada.estaEnBuenEstado(PuntoRevision.DOCUMENTOS))
        assertTrue(guardada.estaEnBuenEstado(PuntoRevision.FRENOS))
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `guardar con datos invalidos no persiste`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        viewModel.actualizarCampo(CampoJornada.META_BRUTA, "0")

        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(0, insertarLlamadas)
        assertFalse(viewModel.uiState.value.guardadoExitoso)
        assertNotNull(viewModel.uiState.value.error(CampoJornada.META_BRUTA))
    }

    @Test
    fun `doble clic en guardar solo registra una jornada`() = runTest {
        advanceUntilIdle()
        completarFormulario()

        viewModel.guardar()
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(1, insertarLlamadas)
    }

    @Test
    fun `fallo al guardar muestra mensaje descriptivo`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        errorSimulado = IllegalStateException("disco lleno")

        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(
            "No se pudo registrar la jornada. Inténtalo de nuevo.",
            viewModel.uiState.value.mensajeError
        )
        assertFalse(viewModel.uiState.value.guardando)
        assertFalse(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `guardar limpia el formulario y la revision`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        viewModel.alternarRevision(PuntoRevision.FRENOS)

        viewModel.guardar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("", estado.valor(CampoJornada.KILOMETRAJE_INICIAL))
        assertEquals("", estado.valor(CampoJornada.META_BRUTA))
        assertEquals(NivelCombustible.POR_DEFECTO, estado.nivelCombustible)
        assertTrue(estado.puntosEnMalEstado.isEmpty())
        assertTrue(estado.guardadoExitoso)
    }

    @Test
    fun `guardar en una jornada de dos dias distintos conserva la fecha`() = runTest {
        advanceUntilIdle()
        completarFormulario()

        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(clockFijo.millis(), jornadaInsertada!!.fechaHoraInicio)
        assertNotNull(viewModel.uiState.value.jornadaDeHoy)
    }

    @Test
    fun `fallo al cargar el perfil publica mensaje descriptivo`() = runTest {
        errorPerfil = IllegalStateException("perfil caido")
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertEquals(
            "No se pudo cargar el perfil. Inténtalo de nuevo.",
            viewModel.uiState.value.mensajeError
        )
    }

    @Test
    fun `fallo al cargar el perfil no prellena la plataforma`() = runTest {
        errorPerfil = IllegalStateException("perfil caido")
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.valor(CampoJornada.PLATAFORMA))
        assertNotNull(viewModel.uiState.value.error(CampoJornada.PLATAFORMA))
    }

    @Test
    fun `la cancelacion del flujo del perfil no publica mensaje de error`() = runTest {
        errorPerfil = CancellationException("cancelado")
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.mensajeError)
    }
}
