package com.jhon.micontroldidi.ui.viaje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.util.ResourceProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ViajeViewModel(
    private val viajeRepository: ViajeRepository,
    private val resourceProvider: ResourceProvider
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
            _viajesFiltrados
                .catch { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        mensajeError = resourceProvider.getString(R.string.viajes_error)
                    )
                }
                .collect { lista ->
                    val estado = _uiState.value
                    val filtro = _filtro.value

                    val mensajeVacio = if (filtro != null && lista.isEmpty()) {
                        resourceProvider.getString(R.string.viajes_sin_resultados)
                    } else {
                        null
                    }

                    _uiState.value = estado.copy(
                        viajes = lista,
                        cargando = false,
                        mensajeFiltroVacio = mensajeVacio,
                        mensajeError = null
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

    fun cargarViajeParaEditar(id: Long) {
        if (id <= 0) return
        _uiState.value = _uiState.value.copy(viajeEditandoId = null)
        viewModelScope.launch {
            val viaje = viajeRepository.obtenerPorId(id)
            if (viaje != null) {
                _uiState.value = _uiState.value.copy(
                    viajeEditandoId = viaje.id,
                    editando = true,
                    fechaHoraOriginal = viaje.fechaHora,
                    valorText = viaje.valor.toString(),
                    propinaText = viaje.propina.toString(),
                    observacionText = viaje.observacion,
                    errorValor = null,
                    errorPropina = null,
                    errorGuardado = null
                )
            }
        }
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
            if (estado.editando) {
                val viaje = ViajeEntity(
                    id = estado.viajeEditandoId!!,
                    fechaHora = estado.fechaHoraOriginal,
                    valor = estado.valorText.toLong(),
                    propina = if (estado.propinaText.isBlank()) 0L else estado.propinaText.toLong(),
                    observacion = estado.observacionText.trim()
                )
                val resultado = viajeRepository.actualizar(viaje)
                if (resultado.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        viajeEditandoId = null,
                        editando = false,
                        fechaHoraOriginal = 0L,
                        valorText = "",
                        propinaText = "",
                        observacionText = "",
                        errorValor = null,
                        errorPropina = null,
                        errorGuardado = null,
                        guardando = false,
                        guardadoExitoso = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        errorGuardado = resourceProvider.getString(R.string.error_guardar_viaje)
                    )
                }
            } else {
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
                        errorGuardado = null,
                        guardando = false,
                        guardadoExitoso = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        errorGuardado = resourceProvider.getString(R.string.error_guardar_viaje)
                    )
                }
            }
        }
    }

    fun mostrarDialogoEliminar(viajeId: Long) {
        _uiState.value = _uiState.value.copy(viajeIdAEliminar = viajeId)
    }

    fun ocultarDialogoEliminar() {
        _uiState.value = _uiState.value.copy(viajeIdAEliminar = null, errorEliminacion = null)
    }

    fun confirmarEliminacion() {
        val viajeId = _uiState.value.viajeIdAEliminar ?: return
        if (_uiState.value.eliminando) return
        _uiState.value = _uiState.value.copy(eliminando = true, errorEliminacion = null)
        viewModelScope.launch {
            val resultado = viajeRepository.eliminar(viajeId)
            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    viajeIdAEliminar = null,
                    eliminando = false,
                    errorEliminacion = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    eliminando = false,
                    errorEliminacion = resourceProvider.getString(R.string.error_eliminar_viaje)
                )
            }
        }
    }

    fun limpiarEstadoTransitorio() {
        _uiState.value = _uiState.value.copy(guardadoExitoso = false)
    }

    private fun validarValor(texto: String): String? {
        if (texto.isBlank()) return resourceProvider.getString(R.string.error_valor_obligatorio)
        val valor = texto.toLongOrNull()
        if (valor == null) return resourceProvider.getString(R.string.error_valor_numerico)
        if (valor <= 0) return resourceProvider.getString(R.string.error_valor_positivo)
        return null
    }

    private fun validarPropina(texto: String): String? {
        if (texto.isBlank()) return null
        val propina = texto.toLongOrNull()
        if (propina == null) return resourceProvider.getString(R.string.error_propina_numerica)
        if (propina < 0) return resourceProvider.getString(R.string.error_propina_negativa)
        return null
    }

    class Factory(
        private val viajeRepository: ViajeRepository,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViajeViewModel(viajeRepository, resourceProvider) as T
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
