package com.jhon.micontroldidi.ui.meta

import com.jhon.micontroldidi.data.local.dao.MetaDao
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import com.jhon.micontroldidi.data.repository.MetaRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MetaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MetaViewModel

    private val metaFlow = MutableStateFlow<MetaEntity?>(null)
    private var guardarLlamadas = 0
    private var desactivarLlamadas = 0
    private var errorSimulado: Exception? = null

    private val daoFalso = object : MetaDao {
        override suspend fun insertar(meta: MetaEntity): Long {
            guardarLlamadas++
            errorSimulado?.let { throw it }
            val nueva = meta.copy(id = guardarLlamadas.toLong())
            metaFlow.value = nueva
            return guardarLlamadas.toLong()
        }

        override fun obtenerActiva(): Flow<MetaEntity?> = metaFlow

        override suspend fun obtenerUltima(): MetaEntity? = metaFlow.value

        override suspend fun desactivarTodas() {
            desactivarLlamadas++
        }

        override suspend fun actualizar(id: Long, tipoPeriodo: String, valorObjetivo: Long): Int {
            guardarLlamadas++
            errorSimulado?.let { throw it }
            metaFlow.value = metaFlow.value?.copy(tipoPeriodo = tipoPeriodo, valorObjetivo = valorObjetivo)
            return 1
        }

        override suspend fun eliminar(id: Long): Int {
            errorSimulado?.let { throw it }
            metaFlow.value = null
            return 1
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        guardarLlamadas = 0
        desactivarLlamadas = 0
        errorSimulado = null
        metaFlow.value = null
        val repo = MetaRepository(daoFalso)
        viewModel = MetaViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial sin meta y formulario vacio`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertNull("No debe haber meta activa", state.metaActiva)
        assertEquals("DIA", state.tipoPeriodo)
        assertEquals("", state.valorText)
        assertFalse(state.cargando)
    }

    @Test
    fun `cuando hay meta activa se carga en el formulario`() = runTest(testDispatcher) {
        metaFlow.value = MetaEntity(id = 1, tipoPeriodo = "MES", valorObjetivo = 1000000)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull("Debe haber meta activa", state.metaActiva)
        assertEquals("MES", state.tipoPeriodo)
        assertEquals("1000000", state.valorText)
    }

    @Test
    fun `seleccionarPeriodo cambia a MES`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo("MES")
        assertEquals("MES", viewModel.uiState.value.tipoPeriodo)
    }

    @Test
    fun `seleccionarPeriodo cambia a DIA`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo("MES")
        viewModel.seleccionarPeriodo("DIA")
        assertEquals("DIA", viewModel.uiState.value.tipoPeriodo)
    }

    @Test
    fun `valor vacio produce error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("")
        assertNotNull(viewModel.uiState.value.errorValor)
    }

    @Test
    fun `valor cero produce error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("0")
        assertNotNull(viewModel.uiState.value.errorValor)
        assertTrue(viewModel.uiState.value.errorValor?.contains("mayor que cero") == true)
    }

    @Test
    fun `valor negativo produce error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("-1000")
        assertNotNull(viewModel.uiState.value.errorValor)
    }

    @Test
    fun `valor valido no produce error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("50000")
        assertNull(viewModel.uiState.value.errorValor)
    }

    @Test
    fun `guardar nueva meta llama al repositorio`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("60000")
        advanceUntilIdle()

        viewModel.guardarMeta()
        advanceUntilIdle()

        assertEquals(1, guardarLlamadas)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `guardar con valor invalido no llama al repositorio`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("0")
        advanceUntilIdle()

        viewModel.guardarMeta()
        advanceUntilIdle()

        assertEquals(0, guardarLlamadas)
    }

    @Test
    fun `pulsaciones repetidas no duplican`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("50000")
        advanceUntilIdle()

        viewModel.guardarMeta()
        viewModel.guardarMeta()
        viewModel.guardarMeta()
        advanceUntilIdle()

        assertEquals("Solo una llamada al repositorio", 1, guardarLlamadas)
    }

    @Test
    fun `eliminar meta funciona`() = runTest(testDispatcher) {
        metaFlow.value = MetaEntity(id = 1, tipoPeriodo = "DIA", valorObjetivo = 60000)
        advanceUntilIdle()

        viewModel.eliminarMeta()
        advanceUntilIdle()

        assertNull("No debe haber meta activa", viewModel.uiState.value.metaActiva)
    }

    @Test
    fun `actualizar meta existente`() = runTest(testDispatcher) {
        metaFlow.value = MetaEntity(id = 1, tipoPeriodo = "DIA", valorObjetivo = 50000)
        advanceUntilIdle()

        viewModel.actualizarValor("75000")
        advanceUntilIdle()

        viewModel.guardarMeta()
        advanceUntilIdle()

        assertEquals(1, guardarLlamadas)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `limpiarEstadoTransitorio resetea guardadoExitoso`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("50000")
        advanceUntilIdle()
        viewModel.guardarMeta()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.guardadoExitoso)

        viewModel.limpiarEstadoTransitorio()
        assertFalse(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `error en repositorio establece mensajeError`() = runTest(testDispatcher) {
        errorSimulado = IllegalArgumentException("Error simulado")
        advanceUntilIdle()
        viewModel.actualizarValor("50000")
        advanceUntilIdle()

        viewModel.guardarMeta()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.mensajeError)
        assertFalse(viewModel.uiState.value.guardando)
    }
}
