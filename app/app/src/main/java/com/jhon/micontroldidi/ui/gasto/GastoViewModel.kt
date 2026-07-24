package com.jhon.micontroldidi.ui.gasto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.repository.CategoriaGastoRepository
import com.jhon.micontroldidi.data.repository.GastoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GastoViewModel(
    private val gastoRepository: GastoRepository,
    private val categoriaGastoRepository: CategoriaGastoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GastoUiState())
    val uiState: StateFlow<GastoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                gastoRepository.obtenerTodos(),
                categoriaGastoRepository.obtenerActivas()
            ) { gastos, categorias ->
                _uiState.value.copy(
                    gastos = gastos,
                    categorias = categorias,
                    cargando = false
                )
            }.collect { nuevoEstado ->
                _uiState.value = nuevoEstado
            }
        }
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
