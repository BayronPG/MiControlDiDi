package com.jhon.micontroldidi.ui.viaje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.domain.FormaPago
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
    private val perfilTrabajoRepository: PerfilTrabajoRepository,
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

    /** Indica si la plataforma ya se intentó prellenar desde el perfil. */
    private var plataformaPrellenada = false

    init {
        viewModelScope.launch {
            perfilTrabajoRepository.observar()
                .catch { e ->
                    if (e is CancellationException) throw e
                    // El fallo no bloquea el registro: se avisa y el conductor
                    // puede escribir la plataforma a mano.
                    _uiState.value = _uiState.value.copy(
                        errorGuardado = resourceProvider.getString(R.string.error_cargar_perfil)
                    )
                }
                .collect { perfil ->
                    if (!plataformaPrellenada) {
                        plataformaPrellenada = true
                        aplicarPlataformaPrellenada(perfil?.plataforma.orEmpty())
                    }
                }
        }

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
        aplicar(_uiState.value.copy(valorText = texto))
    }

    fun actualizarPropina(texto: String) {
        aplicar(_uiState.value.copy(propinaText = texto))
    }

    fun actualizarObservacion(texto: String) {
        _uiState.value = _uiState.value.copy(observacionText = texto)
    }

    fun actualizarPlataforma(texto: String) {
        aplicar(_uiState.value.copy(plataformaText = texto))
    }

    fun actualizarZona(texto: String) {
        aplicar(_uiState.value.copy(zonaText = texto))
    }

    fun actualizarDistancia(texto: String) {
        aplicar(_uiState.value.copy(distanciaText = texto))
    }

    fun seleccionarFormaPago(forma: FormaPago) {
        aplicar(_uiState.value.copy(formaPago = forma))
    }

    fun actualizarPeaje(texto: String) {
        aplicar(_uiState.value.copy(peajeText = texto))
    }

    /** Aplica el cambio y recalcula los errores del formulario. */
    private fun aplicar(estado: ViajeUiState) {
        _uiState.value = conErrores(estado).copy(errorGuardado = null)
    }

    /** Prellena la plataforma con la del perfil si el formulario está vacío. */
    private fun aplicarPlataformaPrellenada(plataforma: String) {
        val estado = _uiState.value
        if (plataforma.isBlank() || estado.editando || estado.plataformaText.isNotBlank()) return
        // No se recalculan errores: el formulario recién abierto no debe mostrar
        // validaciones que el conductor todavía no provocó.
        _uiState.value = estado.copy(plataformaText = plataforma)
    }

    /**
     * Errores de formato en vivo: solo se marcan cuando el campo ya tiene texto.
     * Los campos obligatorios se validan al guardar (ver [conErroresAlGuardar]).
     */
    private fun conErrores(estado: ViajeUiState): ViajeUiState {
        return estado.copy(
            errorValor = validarValor(estado.valorText),
            errorPropina = validarPropina(estado.propinaText),
            errorDistancia = validarNoNegativo(
                estado.distanciaText,
                R.string.error_distancia_negativa
            ),
            errorPeaje = validarNoNegativo(estado.peajeText, R.string.error_peaje_negativo),
            errorPlataforma = null,
            errorZona = null,
            errorFormaPago = null
        )
    }

    /**
     * Errores al intentar guardar: añade los campos obligatorios de un viaje nuevo.
     *
     * Los datos de plataforma (plataforma, zona y forma de pago) son obligatorios
     * al registrar un viaje nuevo; al editar se toleran vacíos para que los viajes
     * migrados desde versiones anteriores puedan guardarse sin cambios.
     */
    private fun conErroresAlGuardar(estado: ViajeUiState): ViajeUiState {
        val exigirDatosDePlataforma = !estado.editando
        return conErrores(estado).copy(
            errorValor = validarValor(estado.valorText),
            errorPlataforma = if (exigirDatosDePlataforma && estado.plataformaText.isBlank()) {
                resourceProvider.getString(R.string.error_viaje_plataforma_obligatoria)
            } else {
                null
            },
            errorZona = if (exigirDatosDePlataforma && estado.zonaText.isBlank()) {
                resourceProvider.getString(R.string.error_viaje_zona_obligatoria)
            } else {
                null
            },
            errorFormaPago = if (exigirDatosDePlataforma && estado.formaPago == null) {
                resourceProvider.getString(R.string.error_viaje_forma_pago_obligatoria)
            } else {
                null
            }
        )
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
                    plataformaText = viaje.plataforma,
                    zonaText = viaje.zona,
                    distanciaText = viaje.distanciaKm.toString(),
                    formaPago = viaje.formaPagoTipo,
                    peajeText = viaje.peaje.toString(),
                    errorValor = null,
                    errorPropina = null,
                    errorPlataforma = null,
                    errorZona = null,
                    errorDistancia = null,
                    errorFormaPago = null,
                    errorPeaje = null,
                    errorGuardado = null
                )
            }
        }
    }

    fun guardarViaje() {
        val estado = _uiState.value

        // Re-validar antes de guardar
        val validado = conErroresAlGuardar(estado)
        if (validado.hayErrores) {
            _uiState.value = validado
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
                    observacion = estado.observacionText.trim(),
                    plataforma = estado.plataformaText.trim(),
                    zona = estado.zonaText.trim(),
                    distanciaMetros = ViajeEntity.kmAMetros(
                        estado.distanciaText.toLongOrNull() ?: 0L
                    ),
                    formaPago = estado.formaPago?.nombre.orEmpty(),
                    peaje = estado.peajeText.toLongOrNull() ?: 0L
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
                        plataformaText = "",
                        zonaText = "",
                        distanciaText = "",
                        formaPago = null,
                        peajeText = "",
                        errorValor = null,
                        errorPropina = null,
                        errorPlataforma = null,
                        errorZona = null,
                        errorDistancia = null,
                        errorFormaPago = null,
                        errorPeaje = null,
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
                    observacion = estado.observacionText.trim(),
                    plataforma = estado.plataformaText.trim(),
                    zona = estado.zonaText.trim(),
                    distanciaMetros = ViajeEntity.kmAMetros(
                        estado.distanciaText.toLongOrNull() ?: 0L
                    ),
                    formaPago = estado.formaPago?.nombre.orEmpty(),
                    peaje = estado.peajeText.toLongOrNull() ?: 0L
                )
                val resultado = viajeRepository.insertar(viaje)
                if (resultado.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        valorText = "",
                        propinaText = "",
                        observacionText = "",
                        distanciaText = "",
                        peajeText = "",
                        errorValor = null,
                        errorPropina = null,
                        errorDistancia = null,
                        errorPeaje = null,
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

    /** Campo numérico opcional: vacío es válido, pero no admite negativos. */
    private fun validarNoNegativo(texto: String, mensajeRes: Int): String? {
        if (texto.isBlank()) return null
        val valor = texto.toLongOrNull()
            ?: return resourceProvider.getString(R.string.error_valor_numerico)
        if (valor < 0) return resourceProvider.getString(mensajeRes)
        return null
    }

    class Factory(
        private val viajeRepository: ViajeRepository,
        private val perfilTrabajoRepository: PerfilTrabajoRepository,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViajeViewModel(
                viajeRepository,
                perfilTrabajoRepository,
                resourceProvider
            ) as T
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
