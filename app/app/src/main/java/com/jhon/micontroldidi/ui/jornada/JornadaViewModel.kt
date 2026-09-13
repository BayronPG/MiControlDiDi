package com.jhon.micontroldidi.ui.jornada

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import com.jhon.micontroldidi.data.repository.JornadaRepository
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.domain.NivelCombustible
import com.jhon.micontroldidi.domain.PuntoRevision
import com.jhon.micontroldidi.util.ResourceProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant

/**
 * ViewModel del registro de jornada.
 *
 * - La fecha y la hora de inicio se toman del reloj al guardar.
 * - La plataforma se prellena con la del perfil de trabajo.
 * - La jornada ya registrada hoy se muestra como información, no bloquea.
 */
class JornadaViewModel(
    private val jornadaRepository: JornadaRepository,
    private val perfilTrabajoRepository: PerfilTrabajoRepository,
    private val resourceProvider: ResourceProvider,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val _uiState = MutableStateFlow(JornadaUiState())
    val uiState: StateFlow<JornadaUiState> = _uiState.asStateFlow()

    private var plataformaPrellenada = false

    init {
        viewModelScope.launch {
            perfilTrabajoRepository.observar()
                .catch { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = _uiState.value.copy(
                        mensajeError = resourceProvider.getString(R.string.error_cargar_perfil)
                    )
                }
                .collect { perfil ->
                    if (!plataformaPrellenada) {
                        plataformaPrellenada = true
                        val nuevos = _uiState.value.valores +
                            (CampoJornada.PLATAFORMA to perfil?.plataforma.orEmpty())
                        _uiState.value = _uiState.value.copy(
                            valores = nuevos,
                            errores = validarCampos(nuevos)
                        )
                    }
                }
        }

        viewModelScope.launch {
            jornadaRepository.observarUltima()
                .catch { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        mensajeError = resourceProvider.getString(R.string.error_cargar_jornada)
                    )
                }
                .collect { ultima ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        jornadaDeHoy = ultima?.takeIf { esDeHoy(it.fechaHoraInicio) }
                    )
                }
        }

        _uiState.value = _uiState.value.copy(
            valores = valoresVacios(),
            errores = validarCampos(valoresVacios())
        )
    }

    fun actualizarCampo(campo: CampoJornada, texto: String) {
        val valores = _uiState.value.valores + (campo to texto)
        _uiState.value = _uiState.value.copy(
            valores = valores,
            errores = validarCampos(valores),
            mensajeError = null
        )
    }

    fun seleccionarCombustible(nivel: NivelCombustible) {
        _uiState.value = _uiState.value.copy(nivelCombustible = nivel)
    }

    fun alternarRevision(punto: PuntoRevision) {
        val actual = _uiState.value.estaEnBuenEstado(punto)
        val revision = _uiState.value.revision + (punto to !actual)
        _uiState.value = _uiState.value.copy(revision = revision, mensajeError = null)
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        val errores = validarCampos(estado.valores)
        if (errores.isNotEmpty()) {
            _uiState.value = estado.copy(errores = errores)
            return
        }

        _uiState.value = estado.copy(guardando = true, mensajeError = null)

        viewModelScope.launch {
            val resultado = jornadaRepository.insertar(construirJornada(estado))
            _uiState.value = if (resultado.isSuccess) {
                _uiState.value.copy(
                    guardando = false,
                    guardadoExitoso = true,
                    valores = valoresVacios(),
                    revision = PuntoRevision.entries.associateWith { true },
                    nivelCombustible = NivelCombustible.POR_DEFECTO,
                    errores = validarCampos(valoresVacios())
                )
            } else {
                _uiState.value.copy(
                    guardando = false,
                    mensajeError = resourceProvider.getString(R.string.error_guardar_jornada)
                )
            }
        }
    }

    /** Odómetro final digitado por el conductor para cerrar la jornada de hoy. */
    fun actualizarOdometroFinal(texto: String) {
        _uiState.value = _uiState.value.copy(
            odometroFinalText = texto,
            errorOdometroFinal = null,
            errorCierre = null
        )
    }

    /**
     * Cierra la jornada de hoy con la hora actual y el odómetro final.
     * Rechaza cerrar dos veces y un odómetro final menor que el inicial.
     */
    fun cerrarJornada() {
        val estado = _uiState.value
        val jornada = estado.jornadaDeHoy ?: return
        if (estado.cerrando) return

        if (jornada.cerrada) {
            _uiState.value = estado.copy(
                errorCierre = resourceProvider.getString(R.string.error_jornada_ya_cerrada)
            )
            return
        }

        val kmFinal = estado.odometroFinalText.toLongOrNull()
        if (kmFinal == null || kmFinal < 0L) {
            _uiState.value = estado.copy(
                errorOdometroFinal = resourceProvider.getString(R.string.error_valor_obligatorio)
            )
            return
        }

        val kmFinalMetros = kmFinal * JornadaEntity.METROS_POR_KM
        if (kmFinalMetros < jornada.kilometrajeInicialMetros) {
            _uiState.value = estado.copy(
                errorOdometroFinal =
                resourceProvider.getString(R.string.error_jornada_odometro_final)
            )
            return
        }

        _uiState.value = estado.copy(
            cerrando = true,
            errorCierre = null,
            errorOdometroFinal = null
        )

        viewModelScope.launch {
            val resultado = jornadaRepository.cerrar(jornada.id, clock.millis(), kmFinalMetros)
            _uiState.value = if (resultado.isSuccess) {
                _uiState.value.copy(
                    cerrando = false,
                    cierreExitoso = true,
                    odometroFinalText = ""
                )
            } else {
                _uiState.value.copy(
                    cerrando = false,
                    errorCierre = resourceProvider.getString(R.string.error_cerrar_jornada)
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

    // --- Validación ---

    private fun validarCampos(valores: Map<CampoJornada, String>): Map<CampoJornada, String> {
        val errores = mutableMapOf<CampoJornada, String>()

        fun valorDe(campo: CampoJornada) = valores[campo].orEmpty()

        if (valorDe(CampoJornada.PLATAFORMA).isBlank()) {
            errores[CampoJornada.PLATAFORMA] =
                resourceProvider.getString(R.string.error_jornada_plataforma_obligatoria)
        }

        val kilometraje = valorDe(CampoJornada.KILOMETRAJE_INICIAL)
        errorDeEntero(kilometraje)?.let { errores[CampoJornada.KILOMETRAJE_INICIAL] = it }
        if (kilometraje.toLongOrNull()?.let { it < 0 } == true) {
            errores[CampoJornada.KILOMETRAJE_INICIAL] =
                resourceProvider.getString(R.string.error_jornada_kilometraje_negativo)
        }

        errorDePositivo(
            valorDe(CampoJornada.PRECIO_GALON),
            R.string.error_jornada_precio_positivo
        )?.let { errores[CampoJornada.PRECIO_GALON] = it }

        errorDePositivo(
            valorDe(CampoJornada.META_BRUTA),
            R.string.error_jornada_meta_positiva
        )?.let { errores[CampoJornada.META_BRUTA] = it }

        val energia = valorDe(CampoJornada.NIVEL_ENERGIA)
        val errorEnergia = errorDeEntero(energia)
        if (errorEnergia != null) {
            errores[CampoJornada.NIVEL_ENERGIA] = errorEnergia
        } else if ((energia.toIntOrNull() ?: 0) !in 0..10) {
            errores[CampoJornada.NIVEL_ENERGIA] =
                resourceProvider.getString(R.string.error_jornada_energia_rango)
        }

        return errores
    }

    /** Entero obligatorio y no negativo. */
    private fun errorDeEntero(texto: String): String? {
        if (texto.isBlank()) return resourceProvider.getString(R.string.error_valor_obligatorio)
        val valor = texto.toLongOrNull()
            ?: return resourceProvider.getString(R.string.error_valor_numerico)
        if (valor < 0) return resourceProvider.getString(R.string.error_valor_positivo)
        return null
    }

    /** Entero obligatorio y mayor que cero. */
    private fun errorDePositivo(texto: String, mensajeRes: Int): String? {
        if (texto.isBlank()) return resourceProvider.getString(R.string.error_valor_obligatorio)
        val valor = texto.toLongOrNull()
            ?: return resourceProvider.getString(R.string.error_valor_numerico)
        if (valor <= 0) return resourceProvider.getString(mensajeRes)
        return null
    }

    // --- Conversión ---

    private fun valoresVacios(): Map<CampoJornada, String> = mapOf(
        CampoJornada.KILOMETRAJE_INICIAL to "",
        CampoJornada.PRECIO_GALON to "",
        CampoJornada.ZONA_INICIAL to "",
        CampoJornada.PLATAFORMA to "",
        CampoJornada.META_BRUTA to "",
        CampoJornada.NIVEL_ENERGIA to "",
        CampoJornada.CLIMA to "",
        CampoJornada.OBSERVACIONES to ""
    )

    private fun construirJornada(estado: JornadaUiState): JornadaEntity {
        var jornada = JornadaEntity(
            fechaHoraInicio = clock.millis(),
            kilometrajeInicialMetros = JornadaEntity.kmAMetros(
                estado.valor(CampoJornada.KILOMETRAJE_INICIAL).toLongOrNull() ?: 0L
            ),
            nivelCombustible = estado.nivelCombustible.nombre,
            precioGalonExtra = estado.valor(CampoJornada.PRECIO_GALON).toLongOrNull() ?: 0L,
            zonaInicial = estado.valor(CampoJornada.ZONA_INICIAL).trim(),
            plataforma = estado.valor(CampoJornada.PLATAFORMA).trim(),
            metaBrutaDia = estado.valor(CampoJornada.META_BRUTA).toLongOrNull() ?: 0L,
            nivelEnergia = estado.valor(CampoJornada.NIVEL_ENERGIA).toIntOrNull() ?: 0,
            clima = estado.valor(CampoJornada.CLIMA).trim(),
            observaciones = estado.valor(CampoJornada.OBSERVACIONES).trim()
        )

        PuntoRevision.entries.forEach { punto ->
            jornada = jornada.conRevision(punto, estado.estaEnBuenEstado(punto))
        }

        return jornada
    }

    private fun esDeHoy(fechaHora: Long): Boolean {
        val zona = clock.zone
        val momento = Instant.ofEpochMilli(fechaHora).atZone(zona).toLocalDate()
        return momento == Instant.ofEpochMilli(clock.millis()).atZone(zona).toLocalDate()
    }

    class Factory(
        private val jornadaRepository: JornadaRepository,
        private val perfilTrabajoRepository: PerfilTrabajoRepository,
        private val resourceProvider: ResourceProvider,
        private val clock: Clock = Clock.systemDefaultZone()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JornadaViewModel(
                jornadaRepository,
                perfilTrabajoRepository,
                resourceProvider,
                clock
            ) as T
        }
    }
}
