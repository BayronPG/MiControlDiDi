package com.jhon.micontroldidi.ui.viaje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.ViajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViajeViewModel(
    private val viajeRepository: ViajeRepository
) : ViewModel() {

    /** Viajes expuestos directamente desde Room, ordenados DESC */
    val viajes: StateFlow<List<ViajeEntity>> = viajeRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ViajeUiState())
    val uiState: StateFlow<ViajeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            viajes.collect { lista ->
                _uiState.value = _uiState.value.copy(
                    viajes = lista,
                    cargando = false
                )
            }
        }
    }

    fun actualizarValor(texto: String) {
        val error = validarValor(texto)
        _uiState.value = _uiState.value.copy(
            valorText = texto,
            errorValor = error
        )
    }

    fun actualizarPropina(texto: String) {
        val error = validarPropina(texto)
        _uiState.value = _uiState.value.copy(
            propinaText = texto,
            errorPropina = error
        )
    }

    fun actualizarObservacion(texto: String) {
        _uiState.value = _uiState.value.copy(observacionText = texto)
    }

    fun guardarViaje() {
        val estado = _uiState.value

        // Re-validar antes de guardar
        val errorV = validarValor(estado.valorText)
        val errorP = validarPropina(estado.propinaText)
        if (errorV != null || errorP != null) {
            _uiState.value = estado.copy(errorValor = errorV, errorPropina = errorP)
            return
        }

        if (estado.guardando) return

        _uiState.value = estado.copy(guardando = true)

        viewModelScope.launch {
            val viaje = ViajeEntity(
                fechaHora = System.currentTimeMillis(),
                valor = estado.valorText.toLong(),
                propina = if (estado.propinaText.isBlank()) 0L else estado.propinaText.toLong(),
                observacion = estado.observacionText.trim()
            )

            val resultado = viajeRepository.insertar(viaje)

            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    valorText = "",
                    propinaText = "",
                    observacionText = "",
                    errorValor = null,
                    errorPropina = null,
                    guardando = false,
                    guardadoExitoso = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorValor = resultado.exceptionOrNull()?.message
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

    private fun validarPropina(texto: String): String? {
        if (texto.isBlank()) return null
        val propina = texto.toLongOrNull()
        if (propina == null) return "La propina debe ser numérica"
        if (propina < 0) return "La propina no puede ser negativa"
        return null
    }

    class Factory(private val viajeRepository: ViajeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViajeViewModel(viajeRepository) as T
        }
    }
}
