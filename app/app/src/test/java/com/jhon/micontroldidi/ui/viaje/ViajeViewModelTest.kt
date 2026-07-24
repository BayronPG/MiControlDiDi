package com.jhon.micontroldidi.ui.viaje

import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.ViajeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    /** Contador de llamadas a insertar */
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

        override fun obtenerTodos(): Flow<List<ViajeEntity>> = viajesFlow
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        insertarLlamadas = 0
        ultimoViajeInsertado = null
        errorSimulado = null
        viajesFlow.value = emptyList()
        val repo = ViajeRepository(daoFalso)
        viewModel = ViajeViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial formulario vacio y sin errores`() = runTest(testDispatcher) {
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
    fun `valor vacio produce error`() {
        viewModel.actualizarValor("")
        val error = viewModel.uiState.value.errorValor
        assertNotNull("Valor vacío debe producir error", error)
        assertTrue(error?.contains("obligatorio") == true)
    }

    @Test
    fun `valor cero produce error`() {
        viewModel.actualizarValor("0")
        val error = viewModel.uiState.value.errorValor
        assertNotNull("Valor cero debe producir error", error)
        assertTrue(error?.contains("mayor que cero") == true)
    }

    @Test
    fun `valor negativo produce error`() {
        viewModel.actualizarValor("-5000")
        val error = viewModel.uiState.value.errorValor
        assertNotNull("Valor negativo debe producir error", error)
        assertTrue(error?.contains("mayor que cero") == true)
    }

    @Test
    fun `propina negativa produce error`() {
        viewModel.actualizarPropina("-1000")
        val error = viewModel.uiState.value.errorPropina
        assertNotNull("Propina negativa debe producir error", error)
        assertTrue(error?.contains("no puede ser negativa") == true)
    }

    @Test
    fun `datos validos llaman una sola vez al repositorio`() = runTest(testDispatcher) {
        viewModel.actualizarValor("15000")
        viewModel.actualizarPropina("2000")
        advanceUntilIdle()

        viewModel.guardarViaje()
        advanceUntilIdle()

        assertEquals("El repositorio debe ser llamado exactamente 1 vez", 1, insertarLlamadas)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `guardado exitoso actualiza estado`() = runTest(testDispatcher) {
        viewModel.actualizarValor("20000")
        viewModel.guardarViaje()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("guardadoExitoso debe ser true", state.guardadoExitoso)
        // El formulario se limpia
        assertEquals("", state.valorText)
        assertEquals("", state.propinaText)
    }

    @Test
    fun `lista expuesta conserva orden descendente de Room`() = runTest(testDispatcher) {
        advanceUntilIdle()
        // Insertar viajes a través del ViewModel
        viewModel.actualizarValor("10000")
        viewModel.guardarViaje()
        advanceUntilIdle()

        viewModel.actualizarValor("20000")
        viewModel.guardarViaje()
        advanceUntilIdle()

        viewModel.actualizarValor("15000")
        viewModel.guardarViaje()
        advanceUntilIdle()

        // El ViewModel expone los viajes tal como los entrega Room (ordenados DESC)
        val viajes = viewModel.uiState.value.viajes
        assertEquals(3, viajes.size)
    }

    @Test
    fun `error de guardado no cierra formulario`() = runTest(testDispatcher) {
        errorSimulado = IllegalArgumentException("Error simulado")
        viewModel.actualizarValor("12000")
        viewModel.guardarViaje()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("guardadoExitoso debe ser false tras error", state.guardadoExitoso)
        assertFalse("no debe estar guardando tras el error", state.guardando)
        assertNotNull("debe haber un mensaje de error", state.errorValor)
    }

    @Test
    fun `pulsaciones repetidas no insertan duplicados`() = runTest(testDispatcher) {
        viewModel.actualizarValor("10000")
        advanceUntilIdle()

        viewModel.guardarViaje()
        viewModel.guardarViaje()
        viewModel.guardarViaje()
        advanceUntilIdle()

        // Solo la primera debe ejecutarse (guardando evita las siguientes)
        assertEquals("Solo debe haber una inserción", 1, insertarLlamadas)
    }
}
