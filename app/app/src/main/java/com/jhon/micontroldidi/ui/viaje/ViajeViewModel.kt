package com.jhon.micontroldidi.ui.viaje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.ViajeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ViajeViewModel(
    private val viajeRepository: ViajeRepository
) : ViewModel() {

    /**
     * Filtro activo: null significa "sin filtro" (todos los viajes).
     * Al cambiar este StateFlow, flatMapLatest cancela la suscripción anterior
     * y se suscribe a la consulta correspondiente.
     */
    private val _filtro = MutableStateFlow<FiltroFecha?>(null)

    /**
     * Viajes observados reactivamente: todos o filtrados por rango,
     * según el valor actual de [_filtro].
     */
    private val _viajesFiltrados = _filtro.flatMapLatest { filtro ->
        if (filtro != null) {
            viajeRepository.obtenerPorRango(filtro.inicio, filtro.fin)
        } else {
            viajeRepository.obtenerTodos()
        }
    }

    private val _uiState = MutableStateFlow(ViajeUiState())
    val uiState: StateFlow<ViajeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _viajesFiltrados.collect { lista ->
                val estado = _uiState.value
                val filtro = _filtro.value

                val mensajeVacio = if (filtro != null && lista.isEmpty()) {
                    "No hay viajes en el rango seleccionado"
                } else {
                    null
                }

                _uiState.value = estado.copy(
                    viajes = lista,
                    cargando = false,
                    mensajeFiltroVacio = mensajeVacio
                )
            }
        }
    }

    /**
     * Aplica un filtro por rango de fechas.
     * Al cambiar [_filtro], flatMapLatest recarga automáticamente.
     */
    fun aplicarFiltroFecha(inicio: Long, fin: Long) {
        _filtro.value = FiltroFecha(inicio, fin)
        _uiState.value = _uiState.value.copy(
            filtroActivo = true,
            filtroInicio = inicio,
            filtroFin = fin,
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

/**
 * Filtro por rango de fechas para la lista de viajes.
 * Usa el mismo contrato semiabierto [inicio, fin) del resto del proyecto.
 */
data class FiltroFecha(
    val inicio: Long,
    val fin: Long
)
