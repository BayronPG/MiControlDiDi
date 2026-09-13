package com.jhon.micontroldidi.ui.stats

import com.jhon.micontroldidi.data.local.dao.GastoDao
import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.GastoRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
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
import java.time.ZoneOffset
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: StatsViewModel

    private val ingresosFlow = MutableStateFlow(0L)
    private val gastosFlow = MutableStateFlow(0L)
    private val viajesFlow = MutableStateFlow<List<ViajeEntity>>(emptyList())

    private val zonaPrueba: ZoneId = ZoneOffset.ofHours(-5)
    private val instanteFijo: Instant = ZonedDateTime.of(
        2026, 7, 25, 10, 30, 0, 0, zonaPrueba
    ).toInstant()
    private val relojFijo: Clock = Clock.fixed(instanteFijo, zonaPrueba)

    private val daoViajeFalso = object : ViajeDao {
        override suspend fun insertar(viaje: ViajeEntity): Long = 0L
        override fun obtenerTodos(): Flow<List<ViajeEntity>> = viajesFlow
        override fun obtenerPorRango(i: Long, f: Long): Flow<List<ViajeEntity>> = viajesFlow
        override fun obtenerIngresosPorRango(i: Long, f: Long): Flow<Long> = ingresosFlow
        override suspend fun obtenerPorId(id: Long): ViajeEntity? = null
        override suspend fun actualizar(id: Long, fechaHora: Long, valor: Long, propina: Long, observacion: String, plataforma: String, zona: String, distanciaMetros: Long, formaPago: String, peaje: Long): Int = 0
        override suspend fun eliminar(id: Long): Int = 0
    }

    private val daoGastoFalso = object : GastoDao {
        override suspend fun insertar(gasto: GastoEntity): Long = 0L
        override suspend fun actualizar(id: Long, fh: Long, cid: Long, v: Long, d: String): Int = 0
        override suspend fun eliminar(id: Long): Int = 0
        override suspend fun obtenerPorId(id: Long): GastoConCategoria? = null
        override fun obtenerTodos(): Flow<List<GastoConCategoria>> = flowOf(emptyList())
        override fun obtenerPorRango(i: Long, f: Long, cid: Long?): Flow<List<GastoConCategoria>> = flowOf(emptyList())
        override fun obtenerTotalGastosPorRango(i: Long, f: Long): Flow<Long> = gastosFlow
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ingresosFlow.value = 0L
        gastosFlow.value = 0L
        viajesFlow.value = emptyList()
        viewModel = StatsViewModel(
            ViajeRepository(daoViajeFalso),
            GastoRepository(daoGastoFalso),
            relojFijo,
            FakeResourceProvider()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial periodo es HOY`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertEquals(StatsPeriodo.HOY, viewModel.uiState.value.periodoSeleccionado)
    }

    @Test
    fun `sin datos devuelve ceros`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val s = viewModel.uiState.value
        assertEquals(0L, s.ingresosActual)
        assertEquals(0L, s.gastosActual)
        assertEquals(0L, s.gananciaNetaActual)
        assertEquals(0L, s.ingresosAnterior)
        assertEquals(0L, s.gastosAnterior)
        assertEquals(0L, s.gananciaNetaAnterior)
        assertEquals(0, s.cantidadViajesActual)
        assertFalse(s.cargando)
    }

    @Test
    fun `cambio a SEMANA actualiza periodo`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo(StatsPeriodo.SEMANA)
        advanceUntilIdle()
        assertEquals(StatsPeriodo.SEMANA, viewModel.uiState.value.periodoSeleccionado)
    }

    @Test
    fun `cambio a MES actualiza periodo`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.seleccionarPeriodo(StatsPeriodo.MES)
        advanceUntilIdle()
        assertEquals(StatsPeriodo.MES, viewModel.uiState.value.periodoSeleccionado)
    }

    @Test
    fun `ingresos actuales se reflejan en el estado`() = runTest(testDispatcher) {
        advanceUntilIdle()
        ingresosFlow.value = 50000L
        advanceUntilIdle()
        assertEquals(50000L, viewModel.uiState.value.ingresosActual)
    }

    @Test
    fun `gastos actuales se reflejan en el estado`() = runTest(testDispatcher) {
        advanceUntilIdle()
        gastosFlow.value = 20000L
        advanceUntilIdle()
        assertEquals(20000L, viewModel.uiState.value.gastosActual)
    }

    @Test
    fun `ganancia neta es ingresos menos gastos`() = runTest(testDispatcher) {
        advanceUntilIdle()
        ingresosFlow.value = 80000L
        gastosFlow.value = 30000L
        advanceUntilIdle()
        assertEquals(50000L, viewModel.uiState.value.gananciaNetaActual)
    }

    @Test
    fun `diferenciaIngresos muestra cambio respecto al periodo anterior`() = runTest(testDispatcher) {
        advanceUntilIdle()
        // Sin cambios en flows, ambos periodos deben tener 0
        assertEquals(0L, viewModel.uiState.value.diferenciaIngresos)
    }

    @Test
    fun `no cargando despues de inicializar`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.cargando)
    }
}
