package com.jhon.micontroldidi.ui.gasto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.repository.CategoriaGastoRepository
import com.jhon.micontroldidi.data.repository.GastoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GastoViewModel(
    private val gastoRepository: GastoRepository,
    private val categoriaGastoRepository: CategoriaGastoRepository
) : ViewModel() {

    /**
     * Filtro activo: null significa "sin filtro" (todos los gastos).
     */
    private val _filtro = MutableStateFlow<FiltroGasto?>(null)

    /**
     * Gastos observados reactivamente: todos o filtrados,
     * según el valor actual de [_filtro].
     */
    private val _gastosFiltrados = _filtro.flatMapLatest { filtro ->
        if (filtro != null) {
            gastoRepository.obtenerPorRango(filtro.inicio, filtro.fin, filtro.categoriaId)
        } else {
            gastoRepository.obtenerTodos()
        }
    }

    private val _uiState = MutableStateFlow(GastoUiState())
    val uiState: StateFlow<GastoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _gastosFiltrados,
                categoriaGastoRepository.obtenerActivas()
            ) { gastos, categorias ->
                val filtro = _filtro.value
                val mensajeVacio = if (filtro != null && gastos.isEmpty()) {
                    "No hay gastos en el rango seleccionado"
                } else {
                    null
                }
                _uiState.value.copy(
                    gastos = gastos,
                    categorias = categorias,
                    cargando = false,
                    mensajeFiltroVacio = mensajeVacio
                )
            }.collect { nuevoEstado ->
                _uiState.value = nuevoEstado
            }
        }
    }

    /**
     * Aplica un filtro por rango de fechas y categoría opcional.
     */
    fun aplicarFiltro(inicio: Long, fin: Long, categoriaId: Long? = null) {
        _filtro.value = FiltroGasto(inicio, fin, categoriaId)
        _uiState.value = _uiState.value.copy(
            filtroActivo = true,
            filtroInicio = inicio,
            filtroFin = fin,
            filtroCategoriaId = categoriaId,
            mostrarSelectorFecha = false
        )
    }

    /**
     * Limpia el filtro y restaura la lista completa.
     */
    fun limpiarFiltro() {
        _filtro.value = null
        _uiState.value = _uiState.value.copy(
            filtroActivo = false,
            filtroInicio = null,
            filtroFin = null,
            filtroCategoriaId = null,
            mostrarSelectorFecha = false,
            mensajeFiltroVacio = null
        )
    }

    /**
     * Muestra u oculta el selector de fechas.
     */
    fun toggleSelectorFecha() {
        _uiState.value = _uiState.value.copy(
            mostrarSelectorFecha = !_uiState.value.mostrarSelectorFecha
        )
    }

    fun seleccionarCategoria(categoriaId: Long?) {
        val error = if (categoriaId == null) "Debes seleccionar una categoría" else null
        _uiState.value = _uiState.value.copy(
            categoriaSeleccionadaId = categoriaId,
            errorCategoria = error
        )
    }

    fun actualizarValor(texto: String) {
        val error = validarValor(texto)
        _uiState.value = _uiState.value.copy(
            valorText = texto,
            errorValor = error
        )
    }

    fun actualizarDescripcion(texto: String) {
        _uiState.value = _uiState.value.copy(descripcionText = texto)
    }

    fun limpiarErrorGuardado() {
        _uiState.value = _uiState.value.copy(errorGuardado = null)
    }

    fun cargarGastoParaEditar(id: Long) {
        if (id <= 0) return
        _uiState.value = _uiState.value.copy(
            modoFormulario = ModoFormulario.CARGANDO_EDICION,
            gastoEditandoId = null,
            errorEdicion = null
        )
        viewModelScope.launch {
            val gasto = gastoRepository.obtenerPorId(id)
            if (gasto != null) {
                _uiState.value = _uiState.value.copy(
                    gastoEditandoId = gasto.id,
                    modoFormulario = ModoFormulario.EDICION,
                    fechaHoraOriginal = gasto.fechaHora,
                    categoriaSeleccionadaId = gasto.categoriaId,
                    valorText = gasto.valor.toString(),
                    descripcionText = gasto.descripcion,
                    errorCategoria = null,
                    errorValor = null,
                    errorGuardado = null,
                    errorEdicion = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    modoFormulario = ModoFormulario.ERROR_EDICION,
                    errorEdicion = "Gasto no encontrado"
                )
            }
        }
    }

    fun guardarGasto() {
        val estado = _uiState.value

        if (estado.modoFormulario != ModoFormulario.CREACION &&
            estado.modoFormulario != ModoFormulario.EDICION
        ) return

        if (estado.guardando) return

        val errorC = if (estado.categoriaSeleccionadaId == null) "Debes seleccionar una categoría" else null
        val errorV = validarValor(estado.valorText)
        if (errorC != null || errorV != null) {
            _uiState.value = estado.copy(errorCategoria = errorC, errorValor = errorV)
            return
        }

        _uiState.value = estado.copy(guardando = true, errorGuardado = null)

        viewModelScope.launch {
            if (estado.modoFormulario == ModoFormulario.EDICION) {
                val gasto = GastoEntity(
                    id = estado.gastoEditandoId!!,
                    fechaHora = estado.fechaHoraOriginal,
                    categoriaId = estado.categoriaSeleccionadaId!!,
                    valor = estado.valorText.toLong(),
                    descripcion = estado.descripcionText.trim()
                )
                val resultado = gastoRepository.actualizar(gasto)
                if (resultado.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        gastoEditandoId = null,
                        modoFormulario = ModoFormulario.CREACION,
                        categoriaSeleccionadaId = null,
                        valorText = "",
                        descripcionText = "",
                        errorCategoria = null,
                        errorValor = null,
                        guardando = false,
                        guardadoExitoso = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        errorGuardado = resultado.exceptionOrNull()?.message
                    )
                }
            } else {
                val gasto = GastoEntity(
                    fechaHora = System.currentTimeMillis(),
                    categoriaId = estado.categoriaSeleccionadaId!!,
                    valor = estado.valorText.toLong(),
                    descripcion = estado.descripcionText.trim()
                )
                val resultado = gastoRepository.insertar(gasto)
                if (resultado.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        categoriaSeleccionadaId = null,
                        valorText = "",
                        descripcionText = "",
                        errorCategoria = null,
                        errorValor = null,
                        guardando = false,
                        guardadoExitoso = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        errorGuardado = resultado.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun mostrarDialogoEliminar(gastoId: Long) {
        _uiState.value = _uiState.value.copy(gastoIdAEliminar = gastoId)
    }

    fun ocultarDialogoEliminar() {
        _uiState.value = _uiState.value.copy(gastoIdAEliminar = null, errorEliminacion = null)
    }

    fun confirmarEliminacion() {
        val gastoId = _uiState.value.gastoIdAEliminar ?: return
        if (_uiState.value.eliminando) return
        _uiState.value = _uiState.value.copy(eliminando = true, errorEliminacion = null)
        viewModelScope.launch {
            val resultado = gastoRepository.eliminar(gastoId)
            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    gastoIdAEliminar = null,
                    eliminando = false,
                    errorEliminacion = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    eliminando = false,
                    errorEliminacion = resultado.exceptionOrNull()?.message
                )
            }
        }
    }

    fun limpiarEstadoTransitorio() {
        _uiState.value = _uiState.value.copy(guardadoExitoso = false)
    }

    private fun validarValor(texto: String): String? {
        if (texto.isBlank()) return "El valor es obligatorio"
        val valor = texto.toLongOrNull()
        if (valor == null) return "El valor debe ser numérico"
        if (valor <= 0) return "El valor debe ser mayor que cero"
        return null
    }

    class Factory(
        private val gastoRepository: GastoRepository,
        private val categoriaGastoRepository: CategoriaGastoRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GastoViewModel(gastoRepository, categoriaGastoRepository) as T
        }
    }
}

/**
 * Filtro por rango de fechas y categoría para la lista de gastos.
 * categoriaId nulo significa "todas las categorías".
 */
data class FiltroGasto(
    val inicio: Long,
    val fin: Long,
    val categoriaId: Long? = null
)
