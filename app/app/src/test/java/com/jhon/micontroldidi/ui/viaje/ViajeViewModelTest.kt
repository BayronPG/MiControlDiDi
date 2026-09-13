package com.jhon.micontroldidi.ui.viaje

import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.domain.FormaPago
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
    private var actualizarLlamadas = 0
    private var ultimoViajeInsertado: ViajeEntity? = null
    private var errorSimulado: Exception? = null
    private var errorPerfil: Exception? = null
    private var viajePorId: ViajeEntity? = null

    private val viajesFlow = MutableStateFlow<List<ViajeEntity>>(emptyList())

    private val perfil = MutableStateFlow<PerfilTrabajoEntity?>(PerfilTrabajoEntity())

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

    private fun crearViewModel(): ViajeViewModel = ViajeViewModel(
        ViajeRepository(daoFalso),
        PerfilTrabajoRepository(perfilDaoFalso),
        FakeResourceProvider()
    )

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

        override suspend fun obtenerPorId(id: Long): ViajeEntity? = viajePorId
        override suspend fun actualizar(id: Long, fechaHora: Long, valor: Long, propina: Long, observacion: String, plataforma: String, zona: String, distanciaMetros: Long, formaPago: String, peaje: Long): Int {
            actualizarLlamadas++
            errorSimulado?.let { throw it }
            return 1
        }
        override suspend fun eliminar(id: Long): Int {
            errorSimulado?.let { throw it }
            return 1
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        insertarLlamadas = 0
        actualizarLlamadas = 0
        ultimoViajeInsertado = null
        errorSimulado = null
        errorPerfil = null
        viajePorId = null
        viajesFlow.value = emptyList()
        perfil.value = PerfilTrabajoEntity()
        viewModel = crearViewModel()
    }

    /** Completa los datos de plataforma que exige un viaje nuevo. */
    private fun completarDatosDePlataforma() {
        viewModel.actualizarPlataforma("inDrive")
        viewModel.actualizarZona("Belén")
        viewModel.seleccionarFormaPago(FormaPago.EFECTIVO)
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
        completarDatosDePlataforma()
        advanceUntilIdle()
        viewModel.guardarViaje()
        advanceUntilIdle()
        assertEquals(1, insertarLlamadas)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun guardadoExitoso_actualizaEstado() = runTest(testDispatcher) {
        viewModel.actualizarValor("20000")
        completarDatosDePlataforma()
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
        completarDatosDePlataforma()
        viewModel.actualizarValor("10000"); viewModel.guardarViaje(); advanceUntilIdle()
        viewModel.actualizarValor("20000"); viewModel.guardarViaje(); advanceUntilIdle()
        viewModel.actualizarValor("15000"); viewModel.guardarViaje(); advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.viajes.size)
    }

    @Test
    fun errorDeGuardado_noCierraFormulario() = runTest(testDispatcher) {
        errorSimulado = IllegalArgumentException("Error simulado")
        viewModel.actualizarValor("12000")
        completarDatosDePlataforma()
        viewModel.guardarViaje()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.guardadoExitoso)
        assertFalse(state.guardando)
        assertNotNull("Debe haber mensaje descriptivo de guardado", state.errorGuardado)
    }

    @Test
    fun falloDeEliminacion_estableceMensajeDeError() = runTest(testDispatcher) {
        daoFalso.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000))
        advanceUntilIdle()
        viewModel.mostrarDialogoEliminar(1L)
        advanceUntilIdle()

        errorSimulado = RuntimeException("Error al eliminar")
        viewModel.confirmarEliminacion()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull("Debe haber mensaje descriptivo de eliminación", state.errorEliminacion)
        assertFalse("No debe seguir eliminando", state.eliminando)
    }

    @Test
    fun pulsacionesRepetidas_noInsertanDuplicados() = runTest(testDispatcher) {
        viewModel.actualizarValor("10000")
        completarDatosDePlataforma()
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
        viewModel = crearViewModel()
        advanceUntilIdle()
        assertNotNull("mensajeError esperado", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun errorEnFlow_dejaCargandoEnFalse() = runTest(testDispatcher) {
        errorSimulado = RuntimeException("Error")
        viewModel = crearViewModel()
        advanceUntilIdle()
        assertFalse("cargando debe ser false tras error", viewModel.uiState.value.cargando)
    }

    // --- Incremento E: datos de plataforma ---

    @Test
    fun `la plataforma se prellena desde el perfil`() = runTest(testDispatcher) {
        advanceUntilIdle()

        assertEquals("inDrive", viewModel.uiState.value.plataformaText)
        assertNull(viewModel.uiState.value.errorPlataforma)
    }

    @Test
    fun `el prellenado no pisa lo que escribio el usuario`() = runTest(testDispatcher) {
        viewModel.actualizarPlataforma("DiDi")
        advanceUntilIdle()

        assertEquals("DiDi", viewModel.uiState.value.plataformaText)
    }

    @Test
    fun `fallo al cargar el perfil no bloquea el registro`() = runTest(testDispatcher) {
        errorPerfil = IllegalStateException("perfil caido")
        viewModel = crearViewModel()
        advanceUntilIdle()

        assertEquals(
            "No se pudo cargar el perfil. Inténtalo de nuevo.",
            viewModel.uiState.value.errorGuardado
        )
    }

    @Test
    fun `un viaje nuevo exige plataforma zona y forma de pago`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("15000")

        assertFalse(
            "El formulario no debe ser válido sin datos de plataforma",
            viewModel.uiState.value.formularioValido
        )

        viewModel.guardarViaje()
        advanceUntilIdle()

        assertEquals(0, insertarLlamadas)
        assertNotNull(viewModel.uiState.value.errorZona)
        assertNotNull(viewModel.uiState.value.errorFormaPago)
    }

    @Test
    fun `un viaje nuevo completo guarda todos sus datos`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("20000")
        viewModel.actualizarPropina("1000")
        viewModel.actualizarZona("Belén")
        viewModel.seleccionarFormaPago(FormaPago.TRANSFERENCIA)
        viewModel.actualizarDistancia("8")
        viewModel.actualizarPeaje("12000")
        advanceUntilIdle()

        viewModel.guardarViaje()
        advanceUntilIdle()

        assertEquals(1, insertarLlamadas)
        val guardado = ultimoViajeInsertado!!
        assertEquals("inDrive", guardado.plataforma)
        assertEquals("Belén", guardado.zona)
        assertEquals(8000L, guardado.distanciaMetros)
        assertEquals("TRANSFERENCIA", guardado.formaPago)
        assertEquals(12000L, guardado.peaje)
        assertEquals(21000L, guardado.ingresoTotal)
    }

    @Test
    fun `un viaje nuevo con distancia negativa no se guarda`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("20000")
        completarDatosDePlataforma()
        viewModel.actualizarDistancia("-1")

        viewModel.guardarViaje()
        advanceUntilIdle()

        assertEquals(0, insertarLlamadas)
        assertNotNull(viewModel.uiState.value.errorDistancia)
    }

    @Test
    fun `un viaje nuevo con peaje negativo no se guarda`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.actualizarValor("20000")
        completarDatosDePlataforma()
        viewModel.actualizarPeaje("-500")

        viewModel.guardarViaje()
        advanceUntilIdle()

        assertEquals(0, insertarLlamadas)
        assertNotNull(viewModel.uiState.value.errorPeaje)
    }

    @Test
    fun `editar un viaje migrado no exige datos de plataforma`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viajePorId = ViajeEntity(
            id = 1L,
            fechaHora = 1000L,
            valor = 15000L,
            observacion = "Viaje migrado"
        )

        viewModel.cargarViajeParaEditar(1L)
        advanceUntilIdle()

        val cargado = viewModel.uiState.value
        assertTrue(cargado.editando)
        assertEquals("", cargado.plataformaText)
        assertNull("Un viaje migrado no debe tener error de plataforma", cargado.errorPlataforma)
        assertNull(cargado.errorZona)
        assertNull(cargado.errorFormaPago)

        viewModel.guardarViaje()
        advanceUntilIdle()

        assertEquals("El viaje migrado debe poder guardarse", 1, actualizarLlamadas)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `editar un viaje conserva su forma de pago y su fecha`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viajePorId = ViajeEntity(
            id = 7L,
            fechaHora = 5000L,
            valor = 18000L,
            zona = "Laureles",
            distanciaMetros = 6500L,
            formaPago = FormaPago.TARJETA.nombre,
            peaje = 9000L
        )

        viewModel.cargarViajeParaEditar(7L)
        advanceUntilIdle()

        val cargado = viewModel.uiState.value
        assertEquals(FormaPago.TARJETA, cargado.formaPago)
        assertEquals("6", cargado.distanciaText)
        assertEquals("9000", cargado.peajeText)
        assertEquals(5000L, cargado.fechaHoraOriginal)
    }
}
