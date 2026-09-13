package com.jhon.micontroldidi.ui.tanqueo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.data.repository.TanqueoRepository
import com.jhon.micontroldidi.util.ResourceProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Clock

/**
 * ViewModel de tanqueos.
 *
 * - La fecha y la hora se toman del reloj al guardar.
 * - El tipo de combustible se prellena con el del perfil de trabajo.
 * - Cada guardado mantiene sincronizado el gasto de gasolina del tanqueo.
 */
class TanqueoViewModel(
    private val tanqueoRepository: TanqueoRepository,
    private val perfilTrabajoRepository: PerfilTrabajoRepository,
    private val resourceProvider: ResourceProvider,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TanqueoUiState())
    val uiState: StateFlow<TanqueoUiState> = _uiState.asStateFlow()

    private var tipoPrellenado = false

    init {
        viewModelScope.launch {
            perfilTrabajoRepository.observar()
                .catch { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = _uiState.value.copy(
                        errorGuardado = resourceProvider.getString(R.string.error_cargar_perfil)
                    )
                }
                .collect { perfil ->
                    if (!tipoPrellenado) {
                        tipoPrellenado = true
                        aplicarTipoCombustible(perfil?.tipoCombustible.orEmpty())
                    }
                }
        }

        viewModelScope.launch {
            tanqueoRepository.observarTodos()
                .catch { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        mensajeError = resourceProvider.getString(R.string.tanqueos_error)
                    )
                }
                .collect { lista ->
                    _uiState.value = _uiState.value.copy(
                        tanqueos = lista,
                        cargando = false,
                        mensajeError = null
                    )
                }
        }
    }

    fun actualizarOdometro(texto: String) = aplicar(_uiState.value.copy(odometroText = texto))

    fun actualizarLitros(texto: String) = aplicar(_uiState.value.copy(litrosText = texto))

    fun actualizarImporte(texto: String) = aplicar(_uiState.value.copy(importeText = texto))

    fun actualizarTipoCombustible(texto: String) =
        aplicar(_uiState.value.copy(tipoCombustible = texto))

    fun actualizarObservacion(texto: String) {
        _uiState.value = _uiState.value.copy(observacionText = texto)
    }

    fun alternarLleno() {
        _uiState.value = _uiState.value.copy(esLleno = !_uiState.value.esLleno)
    }

    fun cargarTanqueoParaEditar(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            val tanqueo = tanqueoRepository.obtenerPorId(id)
            if (tanqueo != null) {
                _uiState.value = _uiState.value.copy(
                    tanqueoEditandoId = tanqueo.id,
                    editando = true,
                    fechaHoraOriginal = tanqueo.fechaHora,
                    gastoIdOriginal = tanqueo.gastoId,
                    odometroText = (tanqueo.odometroMetros / METROS_POR_KM).toString(),
                    litrosText = tanqueo.litros.toString(),
                    importeText = tanqueo.importePagado.toString(),
                    esLleno = tanqueo.esLleno,
                    tipoCombustible = tanqueo.tipoCombustible,
                    observacionText = tanqueo.observacion,
                    errorOdometro = null,
                    errorLitros = null,
                    errorImporte = null,
                    errorTipoCombustible = null,
                    errorGuardado = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorGuardado = resourceProvider.getString(R.string.tanqueo_no_encontrado)
                )
            }
        }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        val validado = conErrores(estado)
        if (validado.hayErrores) {
            _uiState.value = validado
            return
        }

        _uiState.value = validado.copy(guardando = true, errorGuardado = null)

        viewModelScope.launch {
            val resultado = if (estado.editando) {
                tanqueoRepository.actualizar(construirTanqueo(estado))
            } else {
                tanqueoRepository.crear(construirTanqueo(estado))
            }

            _uiState.value = if (resultado.isSuccess) {
                val limpio = _uiState.value.copy(
                    tanqueoEditandoId = null,
                    editando = false,
                    fechaHoraOriginal = 0L,
                    gastoIdOriginal = 0L,
                    odometroText = "",
                    litrosText = "",
                    importeText = "",
                    observacionText = "",
                    guardando = false,
                    guardadoExitoso = true
                )
                conErrores(limpio)
            } else {
                _uiState.value.copy(
                    guardando = false,
                    errorGuardado = resourceProvider.getString(R.string.error_guardar_tanqueo)
                )
            }
        }
    }

    fun mostrarDialogoEliminar(id: Long) {
        _uiState.value = _uiState.value.copy(tanqueoIdAEliminar = id)
    }

    fun ocultarDialogoEliminar() {
        _uiState.value = _uiState.value.copy(tanqueoIdAEliminar = null, errorEliminacion = null)
    }

    fun confirmarEliminacion() {
        val id = _uiState.value.tanqueoIdAEliminar ?: return
        if (_uiState.value.eliminando) return
        _uiState.value = _uiState.value.copy(eliminando = true, errorEliminacion = null)
        viewModelScope.launch {
            val resultado = tanqueoRepository.eliminar(id)
            _uiState.value = if (resultado.isSuccess) {
                _uiState.value.copy(tanqueoIdAEliminar = null, eliminando = false, errorEliminacion = null)
            } else {
                _uiState.value.copy(
                    eliminando = false,
                    errorEliminacion = resourceProvider.getString(R.string.error_eliminar_tanqueo)
                )
            }
        }
    }

    fun limpiarEstadoTransitorio() {
        _uiState.value = _uiState.value.copy(guardadoExitoso = false)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(mensajeError = null, errorGuardado = null)
    }

    private fun aplicar(estado: TanqueoUiState) {
        _uiState.value = conErrores(estado).copy(errorGuardado = null)
    }

    private fun aplicarTipoCombustible(tipo: String) {
        val estado = _uiState.value
        if (tipo.isBlank() || estado.editando || estado.tipoCombustible.isNotBlank()) return
        _uiState.value = estado.copy(tipoCombustible = tipo)
    }

    private fun conErrores(estado: TanqueoUiState): TanqueoUiState {
        return estado.copy(
            errorOdometro = validarNoNegativo(estado.odometroText, R.string.error_tanqueo_odometro),
            errorLitros = validarPositivo(estado.litrosText, R.string.error_tanqueo_litros),
            errorImporte = validarPositivo(estado.importeText, R.string.error_tanqueo_importe),
            errorTipoCombustible = if (estado.tipoCombustible.isBlank()) {
                resourceProvider.getString(R.string.error_tanqueo_tipo)
            } else {
                null
            }
        )
    }

    private fun validarNoNegativo(texto: String, mensajeRes: Int): String? {
        if (texto.isBlank()) return resourceProvider.getString(R.string.error_valor_obligatorio)
        val valor = texto.toLongOrNull()
            ?: return resourceProvider.getString(R.string.error_valor_numerico)
        if (valor < 0) return resourceProvider.getString(mensajeRes)
        return null
    }

    private fun validarPositivo(texto: String, mensajeRes: Int): String? {
        if (texto.isBlank()) return resourceProvider.getString(R.string.error_valor_obligatorio)
        val valor = texto.toLongOrNull()
            ?: return resourceProvider.getString(R.string.error_valor_numerico)
        if (valor <= 0) return resourceProvider.getString(mensajeRes)
        return null
    }

    private fun construirTanqueo(estado: TanqueoUiState) = TanqueoEntity(
        id = estado.tanqueoEditandoId ?: 0L,
        fechaHora = if (estado.editando) estado.fechaHoraOriginal else clock.millis(),
        odometroMetros = (estado.odometroText.toLongOrNull() ?: 0L) * METROS_POR_KM,
        litrosMililitros = (estado.litrosText.toLongOrNull() ?: 0L) * MILILITROS_POR_LITRO,
        importePagado = estado.importeText.toLongOrNull() ?: 0L,
        esLleno = estado.esLleno,
        tipoCombustible = estado.tipoCombustible.trim(),
        observacion = estado.observacionText.trim(),
        gastoId = estado.gastoIdOriginal
    )

    class Factory(
        private val tanqueoRepository: TanqueoRepository,
        private val perfilTrabajoRepository: PerfilTrabajoRepository,
        private val resourceProvider: ResourceProvider,
        private val clock: Clock = Clock.systemDefaultZone()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TanqueoViewModel(
                tanqueoRepository,
                perfilTrabajoRepository,
                resourceProvider,
                clock
            ) as T
        }
    }

    private companion object {
        const val METROS_POR_KM = 1000L
        const val MILILITROS_POR_LITRO = 1000L
    }
}
