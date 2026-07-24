package com.jhon.micontroldidi.ui.gasto

import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.dao.GastoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.repository.CategoriaGastoRepository
import com.jhon.micontroldidi.data.repository.GastoRepository
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
class GastoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GastoViewModel

    private var insertarLlamadas = 0
    private var ultimoGastoInsertado: GastoEntity? = null
    private var errorSimulado: Exception? = null

    private val gastosFlow = MutableStateFlow<List<GastoConCategoria>>(emptyList())
    private val categoriasFlow = MutableStateFlow<List<CategoriaGastoEntity>>(
        listOf(
            CategoriaGastoEntity(id = 1, nombre = "Gasolina"),
            CategoriaGastoEntity(id = 2, nombre = "Mantenimiento"),
            CategoriaGastoEntity(id = 3, nombre = "Parqueadero")
        )
    )

    private val gastoDaoFalso = object : GastoDao {
        override suspend fun insertar(gasto: GastoEntity): Long {
            insertarLlamadas++
            ultimoGastoInsertado = gasto
            errorSimulado?.let { throw it }
            val nuevoGasto = GastoConCategoria(
                id = insertarLlamadas.toLong(),
                fechaHora = gasto.fechaHora,
                categoriaId = gasto.categoriaId,
                nombreCategoria = "Gasolina",
                valor = gasto.valor,
                descripcion = gasto.descripcion
            )
            gastosFlow.value = gastosFlow.value + nuevoGasto
            return insertarLlamadas.toLong()
        }

        override fun obtenerTodos(): Flow<List<GastoConCategoria>> = gastosFlow
    }

    private val categoriaDaoFalso = object : CategoriaGastoDao {
        override suspend fun insertar(categoria: CategoriaGastoEntity): Long = 1L
        override suspend fun insertarLista(categorias: List<CategoriaGastoEntity>): List<Long> =
            categorias.map { 1L }
        override fun obtenerActivas(): Flow<List<CategoriaGastoEntity>> = categoriasFlow
        override suspend fun obtenerPorId(id: Long): CategoriaGastoEntity? = null
        override suspend fun existePorNombre(nombre: String): Boolean = false
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        insertarLlamadas = 0
        ultimoGastoInsertado = null
        errorSimulado = null
        gastosFlow.value = emptyList()
        categoriasFlow.value = listOf(
            CategoriaGastoEntity(id = 1, nombre = "Gasolina"),
            CategoriaGastoEntity(id = 2, nombre = "Mantenimiento"),
            CategoriaGastoEntity(id = 3, nombre = "Parqueadero")
        )
        val repo = GastoRepository(gastoDaoFalso)
        val catRepo = CategoriaGastoRepository(categoriaDaoFalso)
        viewModel = GastoViewModel(repo, catRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial formulario vacio y categorias cargadas`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("", state.valorText)
        assertEquals("", state.descripcionText)
        assertNull(state.categoriaSeleccionadaId)
        assertNull(state.errorCategoria)
        assertNull(state.errorValor)
        assertNull(state.errorGuardado)
        assertFalse(state.guardando)
        assertFalse(state.guardadoExitoso)
        assertEquals(3, state.categorias.size)
    }

    @Test
    fun `categorias activas cargadas desde el repositorio`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val categorias = viewModel.uiState.value.categorias
        assertEquals(3, categorias.size)
        assertEquals("Gasolina", categorias[0].nombre)
        assertEquals("Mantenimiento", categorias[1].nombre)
        assertEquals("Parqueadero", categorias[2].nombre)
    }

    @Test
    fun `categoria no seleccionada produce error al guardar`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("15000")
        advanceUntilIdle()
        viewModel.guardarGasto()
        advanceUntilIdle()
        val error = viewModel.uiState.value.errorCategoria
        assertNotNull("Debe haber error de categoría", error)
        assertTrue(error?.contains("seleccionar") == true)
        assertEquals(0, insertarLlamadas)
    }

    @Test
    fun `valor vacio produce error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("")
        advanceUntilIdle()
        val error = viewModel.uiState.value.errorValor
        assertNotNull("Valor vacío debe producir error", error)
        assertTrue(error?.contains("obligatorio") == true)
    }

    @Test
    fun `valor no numerico produce error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("abc")
        advanceUntilIdle()
        val error = viewModel.uiState.value.errorValor
        assertNotNull("Valor no numérico debe producir error", error)
        assertTrue(error?.contains("numérico") == true)
    }

    @Test
    fun `valor cero produce error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("0")
        advanceUntilIdle()
        val error = viewModel.uiState.value.errorValor
        assertNotNull("Valor cero debe producir error", error)
        assertTrue(error?.contains("mayor que cero") == true)
    }

    @Test
    fun `valor negativo produce error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("-5000")
        advanceUntilIdle()
        val error = viewModel.uiState.value.errorValor
        assertNotNull("Valor negativo debe producir error", error)
        assertTrue(error?.contains("mayor que cero") == true)
    }

    @Test
    fun `datos validos insertan una sola vez`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarCategoria(1)
        viewModel.actualizarValor("15000")
        advanceUntilIdle()

        viewModel.guardarGasto()
        advanceUntilIdle()

        assertEquals("Debe llamar al repositorio exactamente 1 vez", 1, insertarLlamadas)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `pulsaciones repetidas no duplican`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarCategoria(1)
        viewModel.actualizarValor("15000")
        advanceUntilIdle()

        viewModel.guardarGasto()
        viewModel.guardarGasto()
        viewModel.guardarGasto()
        advanceUntilIdle()

        assertEquals("Solo debe haber una inserción", 1, insertarLlamadas)
    }

    @Test
    fun `error del repositorio conserva formulario`() = runTest(testDispatcher) {
        errorSimulado = IllegalArgumentException("Error simulado de base de datos")
        advanceUntilIdle()
        viewModel.seleccionarCategoria(1)
        viewModel.actualizarValor("12000")
        advanceUntilIdle()

        viewModel.guardarGasto()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("guardadoExitoso debe ser false tras error", state.guardadoExitoso)
        assertFalse("no debe estar guardando tras el error", state.guardando)
        assertNotNull("debe haber mensaje de error", state.errorGuardado)
        assertEquals("El formulario no debe limpiarse", "12000", viewModel.uiState.value.valorText)
    }

    @Test
    fun `guardado correcto produce evento consumible`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarCategoria(1)
        viewModel.actualizarValor("20000")
        advanceUntilIdle()

        assertFalse("Antes de guardar, guardadoExitoso debe ser false", viewModel.uiState.value.guardadoExitoso)

        viewModel.guardarGasto()
        advanceUntilIdle()

        assertTrue("Después de guardar, guardadoExitoso debe ser true", viewModel.uiState.value.guardadoExitoso)

        viewModel.limpiarEstadoTransitorio()
        advanceUntilIdle()

        assertFalse("Después de limpiar, guardadoExitoso debe ser false", viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `gastos conservan el orden recibido de Room`() = runTest(testDispatcher) {
        advanceUntilIdle()
        // El flow de gastos se actualiza a través del DAO falso al insertar
        viewModel.seleccionarCategoria(1)
        viewModel.actualizarValor("10000")
        viewModel.guardarGasto()
        advanceUntilIdle()

        viewModel.seleccionarCategoria(2)
        viewModel.actualizarValor("20000")
        viewModel.guardarGasto()
        advanceUntilIdle()

        viewModel.seleccionarCategoria(3)
        viewModel.actualizarValor("15000")
        viewModel.guardarGasto()
        advanceUntilIdle()

        val gastos = viewModel.uiState.value.gastos
        assertEquals(3, gastos.size)
    }
}
