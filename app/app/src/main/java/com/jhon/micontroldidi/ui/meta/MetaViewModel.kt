package com.jhon.micontroldidi.ui.meta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.repository.MetaRepository
import com.jhon.micontroldidi.util.ResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MetaViewModel(
    private val metaRepository: MetaRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetaUiState())
    val uiState: StateFlow<MetaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            metaRepository.obtenerActiva().collect { meta ->
                _uiState.value = if (meta != null) {
                    _uiState.value.copy(
                        metaActiva = meta,
                        cargando = false,
                        tipoPeriodo = meta.tipoPeriodo,
                        valorText = meta.valorObjetivo.toString()
                    )
                } else {
                    _uiState.value.copy(
                        metaActiva = null,
                        cargando = false
                    )
                }
            }
        }
    }

    fun seleccionarPeriodo(periodo: String) {
        _uiState.value = _uiState.value.copy(
            tipoPeriodo = periodo,
            errorPeriodo = null
        )
    }

    fun actualizarValor(texto: String) {
        val error = validarValor(texto)
        _uiState.value = _uiState.value.copy(
            valorText = texto,
            errorValor = error
        )
    }

    fun guardarMeta() {
        val estado = _uiState.value
        if (estado.guardando) return

        val errorV = validarValor(estado.valorText)
        if (errorV != null) {
            _uiState.value = estado.copy(errorValor = errorV)
            return
        }

        _uiState.value = estado.copy(guardando = true, mensajeError = null)

        viewModelScope.launch {
            val valor = estado.valorText.toLong()
            val resultado = if (estado.metaActiva != null) {
                metaRepository.actualizar(estado.metaActiva.id, estado.tipoPeriodo, valor)
            } else {
                metaRepository.guardar(estado.tipoPeriodo, valor)
            }

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    guardadoExitoso = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensajeError = resourceProvider.getString(R.string.error_guardar_meta)
                )
            }
        }
    }

    fun eliminarMeta() {
        val meta = _uiState.value.metaActiva ?: return
        if (_uiState.value.guardando) return

        _uiState.value = _uiState.value.copy(guardando = true)

        viewModelScope.launch {
            val resultado = metaRepository.eliminar(meta.id)
            if (resultado.isSuccess) {
                _uiState.value = MetaUiState(cargando = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensajeError = resourceProvider.getString(R.string.error_eliminar_meta)
                )
            }
        }
    }

    fun limpiarEstadoTransitorio() {
        _uiState.value = _uiState.value.copy(guardadoExitoso = false)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(mensajeError = null)
    }

    private fun validarValor(texto: String): String? {
        if (texto.isBlank()) return resourceProvider.getString(R.string.error_valor_obligatorio)
        val valor = texto.toLongOrNull()
        if (valor == null) return resourceProvider.getString(R.string.error_valor_numerico)
        if (valor <= 0) return resourceProvider.getString(R.string.error_valor_positivo)
        return null
    }

    class Factory(
        private val metaRepository: MetaRepository,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MetaViewModel(metaRepository, resourceProvider) as T
        }
    }
}
