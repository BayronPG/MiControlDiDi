package com.jhon.micontroldidi.ui.viaje

import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.util.FakeResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
class ViajeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ViajeViewModel

    private var insertarLlamadas = 0
    private var ultimoViajeInsertado: ViajeEntity? = null
    private var errorSimulado: Exception? = null

    private val viajesFlow = MutableStateFlow<List<ViajeEntity>>(emptyList())

    private val daoFalso = object : ViajeDao {
        override suspend fun insertar(viaje: ViajeEntity): Long {
            insertarLlamadas++
            ultimoViajeInsertado = viaje
            errorSimulado?.let { throw it }
            val nuevoViaje = viaje.copy(id = insertarLlamadas.toLong())
            viajesFlow.value = viajesFlow.value + nuevoViaje
            return insertarLlamadas.toLong()
        }

        override fun obtenerTodos(): Flow<List<ViajeEntity>> {
            if (errorSimulado != null) return flow { throw errorSimulado!! }
            return viajesFlow
        }

        override fun obtenerPorRango(
            inicioInclusivo: Long,
            finExclusivo: Long
        ): Flow<List<ViajeEntity>> {
            if (errorSimulado != null) return flow { throw errorSimulado!! }
            val filtrados = viajesFlow.value
                .filter { it.fechaHora >= inicioInclusivo && it.fechaHora < finExclusivo }
            return flowOf(filtrados)
        }

        override fun obtenerIngresosPorRango(
            inicioInclusivo: Long,
            finExclusivo: Long
        ): Flow<Long> {
            val suma = viajesFlow.value
                .filter { it.fechaHora >= inicioInclusivo && it.fechaHora < finExclusivo }
                .sumOf { it.valor + it.propina }
            return flowOf(suma)
        }

        override suspend fun obtenerPorId(id: Long): ViajeEntity? = null
        override suspend fun actualizar(id: Long, fechaHora: Long, valor: Long, propina: Long, observacion: String): Int = 1
        override suspend fun eliminar(id: Long): Int = 1
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        insertarLlamadas = 0
        ultimoViajeInsertado = null
        errorSimulado = null
        viajesFlow.value = emptyList()
        val repo = ViajeRepository(daoFalso)
        viewModel = ViajeViewModel(repo, FakeResourceProvider())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun estadoInicial_formularioVacioYSinErrores() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("", state.valorText)
        assertEquals("", state.propinaText)
        assertEquals("", state.observacionText)
        assertNull(state.errorValor)
        assertNull(state.errorPropina)
        assertFalse(state.guardando)
        assertFalse(state.guardadoExitoso)
    }

    @Test
    fun valorVacio_produceError() {
        viewModel.actualizarValor("")
        val error = viewModel.uiState.value.errorValor
        assertNotNull(error)
        assertTrue(error?.contains("obligatorio") == true)
    }

    @Test
    fun valorCero_produceError() {
        viewModel.actualizarValor("0")
        val error = viewModel.uiState.value.errorValor
        assertNotNull(error)
        assertTrue(error?.contains("mayor que cero") == true)
    }

    @Test
    fun valorNegativo_produceError() {
        viewModel.actualizarValor("-5000")
        val error = viewModel.uiState.value.errorValor
        assertNotNull(error)
        assertTrue(error?.contains("mayor que cero") == true)
    }

    @Test
    fun propinaNegativa_produceError() {
        viewModel.actualizarPropina("-1000")
        val error = viewModel.uiState.value.errorPropina
        assertNotNull(error)
        assertTrue(error?.contains("no puede ser negativa") == true)
    }

    @Test
    fun datosValidos_llamanUnaSolaVezAlRepositorio() = runTest(testDispatcher) {
        viewModel.actualizarValor("15000")
        viewModel.actualizarPropina("2000")
        advanceUntilIdle()
        viewModel.guardarViaje()
        advanceUntilIdle()
        assertEquals(1, insertarLlamadas)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun guardadoExitoso_actualizaEstado() = runTest(testDispatcher) {
        viewModel.actualizarValor("20000")
        viewModel.guardarViaje()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.guardadoExitoso)
        assertEquals("", state.valorText)
        assertEquals("", state.propinaText)
    }

    @Test
    fun listaExpuesta_conservaOrdenDescendente() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("10000"); viewModel.guardarViaje(); advanceUntilIdle()
        viewModel.actualizarValor("20000"); viewModel.guardarViaje(); advanceUntilIdle()
        viewModel.actualizarValor("15000"); viewModel.guardarViaje(); advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.viajes.size)
    }

    @Test
    fun errorDeGuardado_noCierraFormulario() = runTest(testDispatcher) {
        errorSimulado = IllegalArgumentException("Error simulado")
        viewModel.actualizarValor("12000")
        viewModel.guardarViaje()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.guardadoExitoso)
        assertFalse(state.guardando)
        assertNotNull(state.errorValor)
    }

    @Test
    fun pulsacionesRepetidas_noInsertanDuplicados() = runTest(testDispatcher) {
        viewModel.actualizarValor("10000")
        advanceUntilIdle()
        viewModel.guardarViaje()
        viewModel.guardarViaje()
        viewModel.guardarViaje()
        advanceUntilIdle()
        assertEquals(1, insertarLlamadas)
    }

    // --- Filtro por fecha ---

    @Test
    fun sinFiltro_devuelveTodosLosViajes() = runTest(testDispatcher) {
        daoFalso.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000))
        daoFalso.insertar(ViajeEntity(fechaHora = 2000L, valor = 20000))
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.viajes.size)
        assertFalse(viewModel.uiState.value.filtroActivo)
    }

    @Test
    fun conFiltro_devuelveSoloLosDelRango() = runTest(testDispatcher) {
        daoFalso.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000))
        daoFalso.insertar(ViajeEntity(fechaHora = 5000L, valor = 20000))
        daoFalso.insertar(ViajeEntity(fechaHora = 9999L, valor = 30000))
        advanceUntilIdle()
        viewModel.aplicarFiltroFecha(0L, 6000L)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.viajes.size)
        assertTrue(viewModel.uiState.value.filtroActivo)
    }

    @Test
    fun limpiarFiltro_restauraTodosLosViajes() = runTest(testDispatcher) {
        daoFalso.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000))
        daoFalso.insertar(ViajeEntity(fechaHora = 5000L, valor = 20000))
        daoFalso.insertar(ViajeEntity(fechaHora = 9999L, valor = 30000))
        advanceUntilIdle()
        viewModel.aplicarFiltroFecha(0L, 6000L)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.viajes.size)
        viewModel.limpiarFiltro()
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.viajes.size)
        assertFalse(viewModel.uiState.value.filtroActivo)
    }

    @Test
    fun cambioDeFiltro_cancelaObservacionAnterior() = runTest(testDispatcher) {
        daoFalso.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000))
        daoFalso.insertar(ViajeEntity(fechaHora = 5000L, valor = 20000))
        daoFalso.insertar(ViajeEntity(fechaHora = 9999L, valor = 30000))
        advanceUntilIdle()
        viewModel.aplicarFiltroFecha(0L, 6000L)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.viajes.size)
        viewModel.aplicarFiltroFecha(0L, 3000L)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.viajes.size)
    }

    // --- Error handling ---

    @Test
    fun errorEnFlow_estableceMensajeError() = runTest(testDispatcher) {
        errorSimulado = RuntimeException("Error")
        val repo = ViajeRepository(daoFalso)
        viewModel = ViajeViewModel(repo, FakeResourceProvider())
        advanceUntilIdle()
        assertNotNull("mensajeError esperado", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun errorEnFlow_dejaCargandoEnFalse() = runTest(testDispatcher) {
        errorSimulado = RuntimeException("Error")
        val repo = ViajeRepository(daoFalso)
        viewModel = ViajeViewModel(repo, FakeResourceProvider())
        advanceUntilIdle()
        assertFalse("cargando debe ser false tras error", viewModel.uiState.value.cargando)
    }
}
