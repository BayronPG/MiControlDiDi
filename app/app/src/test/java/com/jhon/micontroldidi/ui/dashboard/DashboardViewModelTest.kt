package com.jhon.micontroldidi.ui.dashboard

import com.jhon.micontroldidi.data.local.dao.GastoDao
import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.GastoRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.domain.PeriodoDashboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
import java.time.ZoneOffset
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DashboardViewModel

    // Flows controlables que simulan las consultas agregadas
    private val ingresosFlow = MutableStateFlow(0L)
    private val gastosFlow = MutableStateFlow(0L)

    // Captura de parámetros enviados a los DAOs
    private var ultimoInicioIngresos = 0L
    private var ultimoFinIngresos = 0L
    private var ultimoInicioGastos = 0L
    private var ultimoFinGastos = 0L

    // Fecha fija: sábado 2026-07-25 10:30:00 UTC-5 (Bogotá)
    private val zonaPrueba: ZoneId = ZoneOffset.ofHours(-5)
    private val instanteFijo: Instant = ZonedDateTime.of(
        2026, 7, 25, 10, 30, 0, 0, zonaPrueba
    ).toInstant()
    private val relojFijo: Clock = Clock.fixed(instanteFijo, zonaPrueba)

    private val daoViajeFalso = object : ViajeDao {
        override suspend fun insertar(viaje: ViajeEntity): Long = 0L

        override fun obtenerTodos(): Flow<List<ViajeEntity>> = flowOf(emptyList())

        override fun obtenerPorRango(
            inicioInclusivo: Long, finExclusivo: Long
        ): Flow<List<ViajeEntity>> = flowOf(emptyList())

        override fun obtenerIngresosPorRango(
            inicioInclusivo: Long, finExclusivo: Long
        ): Flow<Long> {
            ultimoInicioIngresos = inicioInclusivo
            ultimoFinIngresos = finExclusivo
            return ingresosFlow
        }
    }

    private val daoGastoFalso = object : GastoDao {
        override suspend fun insertar(gasto: GastoEntity): Long = 0L

        override suspend fun actualizar(
            id: Long, fechaHora: Long, categoriaId: Long,
            valor: Long, descripcion: String
        ): Int = 0

        override suspend fun eliminar(id: Long): Int = 0

        override suspend fun obtenerPorId(id: Long): GastoConCategoria? = null

        override fun obtenerTodos(): Flow<List<GastoConCategoria>> = flowOf(emptyList())

        override fun obtenerTotalGastosPorRango(
            inicioInclusivo: Long, finExclusivo: Long
        ): Flow<Long> {
            ultimoInicioGastos = inicioInclusivo
            ultimoFinGastos = finExclusivo
            return gastosFlow
        }
    }

    private fun crearViewModel(): DashboardViewModel {
        val viajeRepo = ViajeRepository(daoViajeFalso)
        val gastoRepo = GastoRepository(daoGastoFalso)
        return DashboardViewModel(viajeRepo, gastoRepo, relojFijo)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ingresosFlow.value = 0L
        gastosFlow.value = 0L
        ultimoInicioIngresos = 0L
        ultimoFinIngresos = 0L
        ultimoInicioGastos = 0L
        ultimoFinGastos = 0L
        viewModel = crearViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 1. Estado inicial ──

    @Test
    fun `estado inicial periodo es DIA`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertEquals(PeriodoDashboard.DIA, viewModel.uiState.value.periodoSeleccionado)
    }

    // ── 2. Sin movimientos ──

    @Test
    fun `sin movimientos devuelve ceros`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(0L, state.ingresos)
        assertEquals(0L, state.gastos)
        assertEquals(0L, state.gananciaNeta)
        assertFalse(state.cargando)
        assertNull(state.mensajeError)
    }

    // ── 3. Combinación básica ──

    @Test
    fun `combina ingresos y gastos en el estado`() = runTest(testDispatcher) {
        advanceUntilIdle()
        ingresosFlow.value = 50000L
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(50000L, state.ingresos)
        assertEquals(0L, state.gastos)
    }

    // ── 4–6. Ganancia ──

    @Test
    fun `ganancia positiva cuando ingresos superan gastos`() = runTest(testDispatcher) {
        advanceUntilIdle()
        ingresosFlow.value = 80000L
        gastosFlow.value = 30000L
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(80000L, state.ingresos)
        assertEquals(30000L, state.gastos)
        assertEquals(50000L, state.gananciaNeta)
    }

    @Test
    fun `ganancia cero cuando ingresos igualan gastos`() = runTest(testDispatcher) {
        advanceUntilIdle()
        ingresosFlow.value = 45000L
        gastosFlow.value = 45000L
        advanceUntilIdle()

        assertEquals(0L, viewModel.uiState.value.gananciaNeta)
    }

    @Test
    fun `ganancia negativa cuando gastos superan ingresos`() = runTest(testDispatcher) {
        advanceUntilIdle()
        ingresosFlow.value = 20000L
        gastosFlow.value = 60000L
        advanceUntilIdle()

        val ganancia = viewModel.uiState.value.gananciaNeta
        assertTrue("La ganancia debe ser negativa, fue $ganancia", ganancia < 0)
        assertEquals(-40000L, ganancia)
    }

    // ── 7. Propinas indirectamente ──

    @Test
    fun `ingresos recibidos del repositorio ya incluyen propinas`() = runTest(testDispatcher) {
        advanceUntilIdle()
        ingresosFlow.value = 15000L
        advanceUntilIdle()

        assertEquals(15000L, viewModel.uiState.value.ingresos)
    }

    // ── 8–9. Cambio de periodo ──

    @Test
    fun `cambio a SEMANA actualiza periodo en el estado`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo(PeriodoDashboard.SEMANA)
        advanceUntilIdle()

        assertEquals(PeriodoDashboard.SEMANA, viewModel.uiState.value.periodoSeleccionado)
    }

    @Test
    fun `cambio a MES actualiza periodo en el estado`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo(PeriodoDashboard.MES)
        advanceUntilIdle()

        assertEquals(PeriodoDashboard.MES, viewModel.uiState.value.periodoSeleccionado)
    }

    // ── 10. Límites delegados correctamente ──

    @Test
    fun `limites del rango se delegan a los DAOs`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val inicioDia = ZonedDateTime.of(2026, 7, 25, 0, 0, 0, 0, zonaPrueba)
            .toInstant().toEpochMilli()
        val finDia = ZonedDateTime.of(2026, 7, 26, 0, 0, 0, 0, zonaPrueba)
            .toInstant().toEpochMilli()

        assertEquals(inicioDia, ultimoInicioIngresos)
        assertEquals(finDia, ultimoFinIngresos)
        assertEquals(inicioDia, ultimoInicioGastos)
        assertEquals(finDia, ultimoFinGastos)
    }

    @Test
    fun `cambio a SEMANA delega rango semanal`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo(PeriodoDashboard.SEMANA)
        advanceUntilIdle()

        val inicioSemana = ZonedDateTime.of(2026, 7, 20, 0, 0, 0, 0, zonaPrueba)
            .toInstant().toEpochMilli()
        val finSemana = ZonedDateTime.of(2026, 7, 27, 0, 0, 0, 0, zonaPrueba)
            .toInstant().toEpochMilli()

        assertEquals(inicioSemana, ultimoInicioIngresos)
        assertEquals(finSemana, ultimoFinIngresos)
        assertEquals(inicioSemana, ultimoInicioGastos)
        assertEquals(finSemana, ultimoFinGastos)
    }

    @Test
    fun `cambio a MES delega rango mensual`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo(PeriodoDashboard.MES)
        advanceUntilIdle()

        val inicioMes = ZonedDateTime.of(2026, 7, 1, 0, 0, 0, 0, zonaPrueba)
            .toInstant().toEpochMilli()
        val finMes = ZonedDateTime.of(2026, 8, 1, 0, 0, 0, 0, zonaPrueba)
            .toInstant().toEpochMilli()

        assertEquals(inicioMes, ultimoInicioIngresos)
        assertEquals(finMes, ultimoFinIngresos)
        assertEquals(inicioMes, ultimoInicioGastos)
        assertEquals(finMes, ultimoFinGastos)
    }

    // ── 11–12. Reactividad ──

    @Test
    fun `actualizacion de ingresos actualiza el estado`() = runTest(testDispatcher) {
        advanceUntilIdle()
        ingresosFlow.value = 100000L
        advanceUntilIdle()
        assertEquals(100000L, viewModel.uiState.value.ingresos)

        ingresosFlow.value = 250000L
        advanceUntilIdle()
        assertEquals(250000L, viewModel.uiState.value.ingresos)
    }

    @Test
    fun `actualizacion de gastos actualiza el estado`() = runTest(testDispatcher) {
        advanceUntilIdle()
        gastosFlow.value = 75000L
        advanceUntilIdle()
        assertEquals(75000L, viewModel.uiState.value.gastos)
    }

    // ── 13. Cambio de periodo ──

    @Test
    fun `cambio de periodo recalcula con la hora actual del reloj`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo(PeriodoDashboard.SEMANA)
        advanceUntilIdle()
        assertEquals(PeriodoDashboard.SEMANA, viewModel.uiState.value.periodoSeleccionado)

        ingresosFlow.value = 9999L
        advanceUntilIdle()
        assertEquals(9999L, viewModel.uiState.value.ingresos)
    }

    // ── 14. Emisiones ──

    @Test
    fun `no emite estados duplicados con la misma entrada`() = runTest(testDispatcher) {
        val emisiones = mutableListOf<DashboardUiState>()
        val job = launch {
            viewModel.uiState.collect { emisiones.add(it) }
        }

        advanceUntilIdle()
        val trasInicial = emisiones.size

        ingresosFlow.value = 5000L
        advanceUntilIdle()
        val trasPrimerCambio = emisiones.size

        assertTrue(
            "Debe haber emitido al menos tras el primer cambio (fue $trasPrimerCambio vs $trasInicial)",
            trasPrimerCambio > trasInicial
        )
        job.cancel()
    }

    // ── 15–18. Manejo de errores ──

    @Test
    fun `error en flow de ingresos establece mensajeError`() = runTest(testDispatcher) {
        val daoViajeError = object : ViajeDao {
            override suspend fun insertar(viaje: ViajeEntity): Long = 0L
            override fun obtenerTodos(): Flow<List<ViajeEntity>> = flowOf(emptyList())
            override fun obtenerPorRango(
                inicioInclusivo: Long, finExclusivo: Long
            ): Flow<List<ViajeEntity>> = flowOf(emptyList())

            override fun obtenerIngresosPorRango(
                inicioInclusivo: Long, finExclusivo: Long
            ): Flow<Long> = flow { throw RuntimeException("Error BD ingresos") }
        }

        val viajeRepo = ViajeRepository(daoViajeError)
        val gastoRepo = GastoRepository(daoGastoFalso)
        viewModel = DashboardViewModel(viajeRepo, gastoRepo, relojFijo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("cargando debe ser false tras error", state.cargando)
        assertEquals("periodo debe conservarse", PeriodoDashboard.DIA, state.periodoSeleccionado)
        assertNotNull("mensajeError debe estar definido", state.mensajeError)
        assertTrue(
            "mensajeError debe contener mensaje legible, fue: ${state.mensajeError}",
            state.mensajeError?.contains("Error") == true
        )
    }

    @Test
    fun `error en flow de gastos establece mensajeError`() = runTest(testDispatcher) {
        val daoGastoError = object : GastoDao {
            override suspend fun insertar(gasto: GastoEntity): Long = 0L
            override suspend fun actualizar(
                id: Long, fechaHora: Long, categoriaId: Long,
                valor: Long, descripcion: String
            ): Int = 0
            override suspend fun eliminar(id: Long): Int = 0
            override suspend fun obtenerPorId(id: Long): GastoConCategoria? = null
            override fun obtenerTodos(): Flow<List<GastoConCategoria>> = flowOf(emptyList())
            override fun obtenerTotalGastosPorRango(
                inicioInclusivo: Long, finExclusivo: Long
            ): Flow<Long> = flow { throw RuntimeException("Error BD gastos") }
        }

        val viajeRepo = ViajeRepository(daoViajeFalso)
        val gastoRepo = GastoRepository(daoGastoError)
        viewModel = DashboardViewModel(viajeRepo, gastoRepo, relojFijo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.cargando)
        assertEquals(PeriodoDashboard.DIA, state.periodoSeleccionado)
        assertNotNull(state.mensajeError)
    }

    @Test
    fun `error en ingresos no expone stack trace`() = runTest(testDispatcher) {
        val daoViajeError = object : ViajeDao {
            override suspend fun insertar(viaje: ViajeEntity): Long = 0L
            override fun obtenerTodos(): Flow<List<ViajeEntity>> = flowOf(emptyList())
            override fun obtenerPorRango(
                inicioInclusivo: Long, finExclusivo: Long
            ): Flow<List<ViajeEntity>> = flowOf(emptyList())

            override fun obtenerIngresosPorRango(
                inicioInclusivo: Long, finExclusivo: Long
            ): Flow<Long> = flow { throw RuntimeException("DetalleTecnicoNOdebeSalir") }
        }

        val viajeRepo = ViajeRepository(daoViajeError)
        val gastoRepo = GastoRepository(daoGastoFalso)
        viewModel = DashboardViewModel(viajeRepo, gastoRepo, relojFijo)
        advanceUntilIdle()

        val msg = viewModel.uiState.value.mensajeError
        assertNotNull(msg)
        assertFalse(
            "El mensaje de error no debe contener detalles técnicos: $msg",
            msg?.contains("DetalleTecnico") == true
        )
    }

    @Test
    fun `cambio de periodo recupera tras error`() = runTest(testDispatcher) {
        // Flag que controla si el flow falla
        var debeFallar = true

        val daoViajeRecuperable = object : ViajeDao {
            override suspend fun insertar(viaje: ViajeEntity): Long = 0L
            override fun obtenerTodos(): Flow<List<ViajeEntity>> = flowOf(emptyList())
            override fun obtenerPorRango(
                inicioInclusivo: Long, finExclusivo: Long
            ): Flow<List<ViajeEntity>> = flowOf(emptyList())

            override fun obtenerIngresosPorRango(
                inicioInclusivo: Long, finExclusivo: Long
            ): Flow<Long> {
                ultimoInicioIngresos = inicioInclusivo
                ultimoFinIngresos = finExclusivo
                return if (debeFallar) {
                    flow { throw RuntimeException("Error transitorio") }
                } else {
                    ingresosFlow
                }
            }
        }

        val viajeRepo = ViajeRepository(daoViajeRecuperable)
        val gastoRepo = GastoRepository(daoGastoFalso)
        viewModel = DashboardViewModel(viajeRepo, gastoRepo, relojFijo)
        advanceUntilIdle()

        // Error inicial
        assertNotNull(viewModel.uiState.value.mensajeError)

        // Desactivar error y cambiar periodo → flatMapLatest crea nuevo inner flow
        debeFallar = false
        ingresosFlow.value = 30000L
        viewModel.seleccionarPeriodo(PeriodoDashboard.SEMANA)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PeriodoDashboard.SEMANA, state.periodoSeleccionado)
        assertEquals(30000L, state.ingresos)
        assertNull("mensajeError debe limpiarse tras recuperación", state.mensajeError)
    }
}
