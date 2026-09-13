package com.jhon.micontroldidi.ui.gasto

import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.dao.GastoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.repository.CategoriaGastoRepository
import com.jhon.micontroldidi.data.repository.GastoRepository
import com.jhon.micontroldidi.util.FakeResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

/**
 * Un gasto que procede de un tanqueo no se edita ni se elimina desde la
 * pantalla general de Gastos: se gestiona desde Tanqueos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GastoVinculadoTest {

    private val testDispatcher = StandardTestDispatcher()

    private val gastoDeTanqueo = GastoConCategoria(
        id = 1L,
        fechaHora = 5000L,
        categoriaId = 1L,
        nombreCategoria = "Gasolina",
        valor = 60000L,
        descripcion = "Tanqueo",
        esTanqueo = true
    )

    private val gastoLibre = GastoConCategoria(
        id = 2L,
        fechaHora = 6000L,
        categoriaId = 2L,
        nombreCategoria = "Lavado",
        valor = 8000L,
        descripcion = "Lavado",
        esTanqueo = false
    )

    private val gastoDaoFalso = object : GastoDao {
        override suspend fun insertar(gasto: GastoEntity): Long = 1L
        override suspend fun actualizar(
            id: Long,
            fechaHora: Long,
            categoriaId: Long,
            valor: Long,
            descripcion: String
        ): Int = 1

        override suspend fun eliminar(id: Long): Int = 1
        override fun obtenerTodos(): Flow<List<GastoConCategoria>> =
            flowOf(listOf(gastoDeTanqueo, gastoLibre))

        override suspend fun obtenerPorId(id: Long): GastoConCategoria? = gastoDeTanqueo
        override fun obtenerPorRango(
            inicioInclusivo: Long,
            finExclusivo: Long,
            categoriaId: Long?
        ): Flow<List<GastoConCategoria>> = flowOf(listOf(gastoDeTanqueo, gastoLibre))

        override fun obtenerTotalGastosPorRango(
            inicioInclusivo: Long,
            finExclusivo: Long
        ): Flow<Long> = flowOf(68000L)
    }

    private val categoriaDaoFalso = object : CategoriaGastoDao {
        override suspend fun insertar(categoria: CategoriaGastoEntity): Long = 1L
        override suspend fun insertarLista(categorias: List<CategoriaGastoEntity>): List<Long> =
            emptyList()
        override fun obtenerActivas(): Flow<List<CategoriaGastoEntity>> =
            flowOf(listOf(CategoriaGastoEntity(id = 1L, nombre = "Gasolina")))

        override suspend fun obtenerPorId(id: Long): CategoriaGastoEntity? = null
        override suspend fun existePorNombre(nombre: String): Boolean = true
        override suspend fun obtenerIdGasolina(): Long? = 1L
    }

    private lateinit var viewModel: GastoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GastoViewModel(
            GastoRepository(gastoDaoFalso),
            CategoriaGastoRepository(categoriaDaoFalso),
            FakeResourceProvider()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `la lista marca el gasto que viene de un tanqueo`() = runTest {
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.gastos.first { it.id == 1L }.esTanqueo)
        assertEquals(false, viewModel.uiState.value.gastos.first { it.id == 2L }.esTanqueo)
    }

    @Test
    fun `un gasto de tanqueo no se carga para editar`() = runTest {
        advanceUntilIdle()

        viewModel.cargarGastoParaEditar(1L)
        advanceUntilIdle()

        assertEquals(
            "Este gasto viene de un tanqueo: se edita en Tanqueos",
            viewModel.uiState.value.errorEdicion
        )
        assertNull("No debe entrar en modo edición", viewModel.uiState.value.gastoEditandoId)
    }

    @Test
    fun `un gasto de tanqueo no abre el dialogo de eliminacion`() = runTest {
        advanceUntilIdle()

        viewModel.mostrarDialogoEliminar(1L)

        assertNull("No debe abrirse el diálogo", viewModel.uiState.value.gastoIdAEliminar)
        assertNotNull(viewModel.uiState.value.errorEliminacion)
    }

    @Test
    fun `un gasto libre si se carga para editar`() = runTest {
        advanceUntilIdle()

        viewModel.cargarGastoParaEditar(2L)
        advanceUntilIdle()

        assertNull(
            "Un gasto sin vínculo no debe bloquearse",
            viewModel.uiState.value.errorEdicion?.takeIf { it.contains("tanqueo") }
        )
    }
}
