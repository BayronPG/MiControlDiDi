package com.jhon.micontroldidi.ui.tanqueo

import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.dao.TanqueoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.data.repository.TanqueoRepository
import com.jhon.micontroldidi.util.FakeResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class TanqueoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tanqueos = MutableStateFlow<List<TanqueoEntity>>(emptyList())
    private val perfil = MutableStateFlow<PerfilTrabajoEntity?>(PerfilTrabajoEntity())
    private var crearLlamadas = 0
    private var eliminarLlamadas = 0
    private var gastoCreado: GastoEntity? = null
    private var tanqueoCreado: TanqueoEntity? = null
    private var errorSimulado: Exception? = null

    private val clockFijo: Clock = Clock.fixed(
        Instant.parse("2026-09-13T15:00:00Z"),
        ZoneId.of("America/Bogota")
    )

    private val tanqueoDaoFalso = object : TanqueoDao() {
        override suspend fun insertarGasto(gasto: GastoEntity): Long {
            gastoCreado = gasto
            return 42L
        }

        override suspend fun actualizarGasto(
            id: Long,
            fechaHora: Long,
            categoriaId: Long,
            valor: Long,
            descripcion: String
        ): Int = 1

        override suspend fun eliminarGasto(id: Long): Int = 1

        override suspend fun insertarTanqueo(tanqueo: TanqueoEntity): Long {
            crearLlamadas++
            errorSimulado?.let { throw it }
            tanqueoCreado = tanqueo
            return 7L
        }

        override suspend fun actualizarTanqueo(tanqueo: TanqueoEntity): Int = 1

        override suspend fun eliminarTanqueo(id: Long): Int {
            eliminarLlamadas++
            return 1
        }

        override fun observarTodos(): Flow<List<TanqueoEntity>> = tanqueos

        override fun observarUltimo(): Flow<TanqueoEntity?> = flowOf(null)

        override suspend fun obtenerPorId(id: Long): TanqueoEntity? = TanqueoEntity(
            id = id,
            fechaHora = 5000L,
            odometroMetros = 10_045_000L,
            litrosMililitros = 4000L,
            importePagado = 60000L,
            tipoCombustible = "Extra",
            gastoId = 42L
        )
    }

    private val categoriaDaoFalso = object : CategoriaGastoDao {
        override suspend fun insertar(categoria: CategoriaGastoEntity): Long = 1L
        override suspend fun insertarLista(categorias: List<CategoriaGastoEntity>): List<Long> =
            emptyList()
        override fun obtenerActivas(): Flow<List<CategoriaGastoEntity>> = flowOf(emptyList())
        override suspend fun obtenerPorId(id: Long): CategoriaGastoEntity? = null
        override suspend fun existePorNombre(nombre: String): Boolean = true
        override suspend fun obtenerIdGasolina(): Long? = 1L
    }

    private val perfilDaoFalso = object : PerfilTrabajoDao {
        override fun observar(id: Int): Flow<PerfilTrabajoEntity?> = perfil
        override suspend fun obtener(id: Int): PerfilTrabajoEntity? = perfil.value
        override suspend fun guardar(perfilNuevo: PerfilTrabajoEntity) {
            perfil.value = perfilNuevo
        }
    }

    private lateinit var viewModel: TanqueoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tanqueos.value = emptyList()
        perfil.value = PerfilTrabajoEntity()
        crearLlamadas = 0
        eliminarLlamadas = 0
        gastoCreado = null
        tanqueoCreado = null
        errorSimulado = null
        viewModel = crearViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel() = TanqueoViewModel(
        TanqueoRepository(tanqueoDaoFalso, categoriaDaoFalso),
        PerfilTrabajoRepository(perfilDaoFalso),
        FakeResourceProvider(),
        clockFijo
    )

    private fun completarFormulario() {
        viewModel.actualizarOdometro("10045")
        viewModel.actualizarLitros("4")
        viewModel.actualizarImporte("60000")
    }

    @Test
    fun `el tipo de combustible se prellena desde el perfil`() = runTest {
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertEquals("Extra", viewModel.uiState.value.tipoCombustible)
    }

    @Test
    fun `un tanqueo sin litros no es valido`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        viewModel.actualizarLitros("0")

        assertNotNull(viewModel.uiState.value.errorLitros)
        assertFalse(viewModel.uiState.value.formularioValido)
    }

    @Test
    fun `un tanqueo sin importe no es valido`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        viewModel.actualizarImporte("")

        assertNotNull(viewModel.uiState.value.errorImporte)
    }

    @Test
    fun `un tanqueo con odometro negativo no es valido`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        viewModel.actualizarOdometro("-1")

        assertNotNull(viewModel.uiState.value.errorOdometro)
    }

    @Test
    fun `guardar crea el tanqueo con su gasto y en metros`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        viewModel.alternarLleno()

        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(1, crearLlamadas)
        assertEquals("El gasto vale exactamente el importe", 60000L, gastoCreado!!.valor)
        assertEquals(42L, tanqueoCreado!!.gastoId)
        assertEquals(10_045_000L, tanqueoCreado!!.odometroMetros)
        assertEquals(4000L, tanqueoCreado!!.litrosMililitros)
        assertEquals(clockFijo.millis(), tanqueoCreado!!.fechaHora)
        assertFalse("Se desmarcó el tanque lleno", tanqueoCreado!!.esLleno)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
        assertEquals("", viewModel.uiState.value.litrosText)
    }

    @Test
    fun `un fallo al guardar no pierde el formulario`() = runTest {
        advanceUntilIdle()
        completarFormulario()
        errorSimulado = IllegalStateException("disco lleno")

        viewModel.guardar()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorGuardado)
        assertFalse(viewModel.uiState.value.guardadoExitoso)
        assertEquals("4", viewModel.uiState.value.litrosText)
    }

    @Test
    fun `confirmar la eliminacion delega en el repositorio`() = runTest {
        advanceUntilIdle()

        viewModel.mostrarDialogoEliminar(7L)
        viewModel.confirmarEliminacion()
        advanceUntilIdle()

        assertEquals(1, eliminarLlamadas)
        assertEquals(null, viewModel.uiState.value.tanqueoIdAEliminar)
    }

    @Test
    fun `cargar un tanqueo para editar completa el formulario`() = runTest {
        advanceUntilIdle()

        viewModel.cargarTanqueoParaEditar(7L)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado.editando)
        assertEquals("10045", estado.odometroText)
        assertEquals("4", estado.litrosText)
        assertEquals("60000", estado.importeText)
        assertEquals("Extra", estado.tipoCombustible)
        assertEquals(42L, estado.gastoIdOriginal)
    }
}
