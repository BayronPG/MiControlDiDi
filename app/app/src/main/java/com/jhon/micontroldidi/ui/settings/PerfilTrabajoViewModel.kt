package com.jhon.micontroldidi.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.util.ResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Clock

/**
 * ViewModel del perfil de trabajo.
 *
 * Carga el perfil desde el repositorio, valida el formulario campo por campo
 * y lo guarda. Si todavía no existe fila en la base de datos, trabaja con los
 * valores por defecto de [PerfilTrabajoEntity].
 */
class PerfilTrabajoViewModel(
    private val perfilTrabajoRepository: PerfilTrabajoRepository,
    private val resourceProvider: ResourceProvider,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilTrabajoUiState())
    val uiState: StateFlow<PerfilTrabajoUiState> = _uiState.asStateFlow()

    /**
     * Una vez cargado el perfil, las reemisiones del Flow no vuelven a pisar
     * el formulario: el usuario puede estar editando.
     */
    private var cargadoDesdeFuente = false

    init {
        viewModelScope.launch {
            perfilTrabajoRepository.observar()
                .catch {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        mensajeError = resourceProvider.getString(R.string.error_cargar_perfil)
                    )
                }
                .collect { perfil ->
                    if (!cargadoDesdeFuente) {
                        cargadoDesdeFuente = true
                        _uiState.value = estadoConPerfil(perfil ?: PerfilTrabajoEntity())
                    }
                }
        }
    }

    fun actualizarCampo(campo: CampoPerfil, texto: String) {
        val valores = _uiState.value.valores + (campo to texto)
        _uiState.value = _uiState.value.copy(
            valores = valores,
            errores = validarCampos(valores),
            mensajeError = null
        )
    }

    fun alternarDia(dia: Int) {
        val actuales = _uiState.value.diasSeleccionados
        val nuevos = if (actuales.contains(dia)) actuales - dia else actuales + dia
        _uiState.value = _uiState.value.copy(
            diasSeleccionados = nuevos,
            errorDias = if (nuevos.isEmpty()) {
                resourceProvider.getString(R.string.error_perfil_dias_obligatorios)
            } else {
                null
            },
            mensajeError = null
        )
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        val errores = validarCampos(estado.valores)
        val errorDias = if (estado.diasSeleccionados.isEmpty()) {
            resourceProvider.getString(R.string.error_perfil_dias_obligatorios)
        } else {
            null
        }

        if (errores.isNotEmpty() || errorDias != null) {
            _uiState.value = estado.copy(errores = errores, errorDias = errorDias)
            return
        }

        _uiState.value = estado.copy(guardando = true, mensajeError = null)

        viewModelScope.launch {
            val resultado = perfilTrabajoRepository.guardar(construirPerfil(estado))
            _uiState.value = if (resultado.isSuccess) {
                _uiState.value.copy(guardando = false, guardadoExitoso = true)
            } else {
                _uiState.value.copy(
                    guardando = false,
                    mensajeError = resourceProvider.getString(R.string.error_guardar_perfil)
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

    private fun validarCampos(valores: Map<CampoPerfil, String>): Map<CampoPerfil, String> {
        val errores = mutableMapOf<CampoPerfil, String>()

        fun valorDe(campo: CampoPerfil) = valores[campo].orEmpty()

        if (valorDe(CampoPerfil.PLATAFORMA).isBlank()) {
            errores[CampoPerfil.PLATAFORMA] =
                resourceProvider.getString(R.string.error_perfil_plataforma_obligatoria)
        }
        if (valorDe(CampoPerfil.VEHICULO).isBlank()) {
            errores[CampoPerfil.VEHICULO] =
                resourceProvider.getString(R.string.error_perfil_vehiculo_obligatorio)
        }
        if (valorDe(CampoPerfil.CIUDAD).isBlank()) {
            errores[CampoPerfil.CIUDAD] =
                resourceProvider.getString(R.string.error_perfil_ciudad_obligatoria)
        }

        errorDeHora(valorDe(CampoPerfil.HORA_INICIO))?.let { errores[CampoPerfil.HORA_INICIO] = it }
        errorDeHora(valorDe(CampoPerfil.HORA_FIN))?.let { errores[CampoPerfil.HORA_FIN] = it }

        val inicio = minutosDeTexto(valorDe(CampoPerfil.HORA_INICIO))
        val fin = minutosDeTexto(valorDe(CampoPerfil.HORA_FIN))
        if (inicio != null && fin != null && inicio >= fin) {
            errores[CampoPerfil.HORA_FIN] =
                resourceProvider.getString(R.string.error_perfil_horas_invertidas)
        }

        val porcentajeTexto = valorDe(CampoPerfil.MAX_KM_VACIOS)
        val errorPorcentaje = errorDeEntero(porcentajeTexto)
        if (errorPorcentaje != null) {
            errores[CampoPerfil.MAX_KM_VACIOS] = errorPorcentaje
        } else if ((porcentajeTexto.toIntOrNull() ?: 0) !in 0..100) {
            errores[CampoPerfil.MAX_KM_VACIOS] =
                resourceProvider.getString(R.string.error_perfil_porcentaje_rango)
        }

        RESERVAS.forEach { par ->
            val costoTexto = valorDe(par.costo)
            val intervaloTexto = valorDe(par.intervalo)
            errorDeEntero(costoTexto)?.let { errores[par.costo] = it }
            errorDeEntero(intervaloTexto)?.let { errores[par.intervalo] = it }

            val costo = costoTexto.toLongOrNull()
            val intervalo = intervaloTexto.toLongOrNull()
            if (costo != null && intervalo != null && costo > 0 && intervalo == 0L) {
                errores[par.intervalo] = resourceProvider.getString(R.string.error_perfil_intervalo_cero)
            }
        }

        return errores
    }

    /** Valida un campo numérico obligatorio y no negativo. */
    private fun errorDeEntero(texto: String): String? {
        if (texto.isBlank()) return resourceProvider.getString(R.string.error_valor_obligatorio)
        val valor = texto.toLongOrNull()
            ?: return resourceProvider.getString(R.string.error_valor_numerico)
        if (valor < 0) return resourceProvider.getString(R.string.error_valor_positivo)
        return null
    }

    private fun errorDeHora(texto: String): String? =
        if (minutosDeTexto(texto) == null) {
            resourceProvider.getString(R.string.error_perfil_hora_formato)
        } else {
            null
        }

    /** Convierte "HH:mm" a minutos desde la medianoche; null si el formato no sirve. */
    private fun minutosDeTexto(texto: String): Int? {
        val partes = texto.trim().split(":")
        if (partes.size != 2) return null
        if (partes[0].length !in 1..2 || partes[1].length != 2) return null
        val hora = partes[0].toIntOrNull() ?: return null
        val minuto = partes[1].toIntOrNull() ?: return null
        if (hora !in 0..23 || minuto !in 0..59) return null
        return PerfilTrabajoEntity.horaAMinutos(hora, minuto)
    }

    // --- Conversión ---

    private fun estadoConPerfil(perfil: PerfilTrabajoEntity): PerfilTrabajoUiState {
        val valores = mutableMapOf<CampoPerfil, String>()
        valores[CampoPerfil.PLATAFORMA] = perfil.plataforma
        valores[CampoPerfil.PLATAFORMAS_DISPONIBLES] = perfil.plataformasDisponibles
        valores[CampoPerfil.VEHICULO] = perfil.vehiculo
        valores[CampoPerfil.TIPO_COMBUSTIBLE] = perfil.tipoCombustible
        valores[CampoPerfil.CIUDAD] = perfil.ciudad
        valores[CampoPerfil.HORA_INICIO] = formatearHora(perfil.horaInicioMinutos)
        valores[CampoPerfil.HORA_FIN] = formatearHora(perfil.horaFinMinutos)
        valores[CampoPerfil.MAX_KM_VACIOS] = perfil.maxPorcentajeKmVacios.toString()
        valores[CampoPerfil.COSTO_ACEITE] = perfil.costoAceite.toString()
        valores[CampoPerfil.INTERVALO_ACEITE] = perfil.intervaloAceiteKm.toString()
        valores[CampoPerfil.COSTO_LLANTAS] = perfil.costoLlantas.toString()
        valores[CampoPerfil.INTERVALO_LLANTAS] = perfil.intervaloLlantasKm.toString()
        valores[CampoPerfil.COSTO_FRENOS] = perfil.costoFrenos.toString()
        valores[CampoPerfil.INTERVALO_FRENOS] = perfil.intervaloFrenosKm.toString()
        valores[CampoPerfil.COSTO_KIT_ARRASTRE] = perfil.costoKitArrastre.toString()
        valores[CampoPerfil.INTERVALO_KIT_ARRASTRE] = perfil.intervaloKitArrastreKm.toString()
        valores[CampoPerfil.COSTO_MANTENIMIENTO] = perfil.costoMantenimiento.toString()
        valores[CampoPerfil.INTERVALO_MANTENIMIENTO] = perfil.intervaloMantenimientoKm.toString()
        valores[CampoPerfil.COSTO_DEPRECIACION] = perfil.costoDepreciacion.toString()
        valores[CampoPerfil.INTERVALO_DEPRECIACION] = perfil.intervaloDepreciacionKm.toString()

        return PerfilTrabajoUiState(
            cargando = false,
            valores = valores,
            diasSeleccionados = perfil.diasLaboralesSet
        )
    }

    private fun construirPerfil(estado: PerfilTrabajoUiState): PerfilTrabajoEntity =
        PerfilTrabajoEntity(
            id = PerfilTrabajoEntity.ID_UNICO,
            plataforma = estado.valor(CampoPerfil.PLATAFORMA).trim(),
            plataformasDisponibles = estado.valor(CampoPerfil.PLATAFORMAS_DISPONIBLES).trim(),
            vehiculo = estado.valor(CampoPerfil.VEHICULO).trim(),
            tipoCombustible = estado.valor(CampoPerfil.TIPO_COMBUSTIBLE).trim(),
            ciudad = estado.valor(CampoPerfil.CIUDAD).trim(),
            diasLaborales = PerfilTrabajoEntity.formatearDias(estado.diasSeleccionados),
            horaInicioMinutos = minutosDeTexto(estado.valor(CampoPerfil.HORA_INICIO)) ?: 0,
            horaFinMinutos = minutosDeTexto(estado.valor(CampoPerfil.HORA_FIN)) ?: 0,
            maxPorcentajeKmVacios = estado.valor(CampoPerfil.MAX_KM_VACIOS).toIntOrNull() ?: 0,
            costoAceite = entero(estado, CampoPerfil.COSTO_ACEITE),
            intervaloAceiteKm = entero(estado, CampoPerfil.INTERVALO_ACEITE),
            costoLlantas = entero(estado, CampoPerfil.COSTO_LLANTAS),
            intervaloLlantasKm = entero(estado, CampoPerfil.INTERVALO_LLANTAS),
            costoFrenos = entero(estado, CampoPerfil.COSTO_FRENOS),
            intervaloFrenosKm = entero(estado, CampoPerfil.INTERVALO_FRENOS),
            costoKitArrastre = entero(estado, CampoPerfil.COSTO_KIT_ARRASTRE),
            intervaloKitArrastreKm = entero(estado, CampoPerfil.INTERVALO_KIT_ARRASTRE),
            costoMantenimiento = entero(estado, CampoPerfil.COSTO_MANTENIMIENTO),
            intervaloMantenimientoKm = entero(estado, CampoPerfil.INTERVALO_MANTENIMIENTO),
            costoDepreciacion = entero(estado, CampoPerfil.COSTO_DEPRECIACION),
            intervaloDepreciacionKm = entero(estado, CampoPerfil.INTERVALO_DEPRECIACION),
            actualizadoEn = clock.millis()
        )

    private fun entero(estado: PerfilTrabajoUiState, campo: CampoPerfil): Long =
        estado.valor(campo).toLongOrNull() ?: 0L

    private fun formatearHora(minutos: Int): String {
        val seguro = minutos.coerceIn(0, 1439)
        return "%02d:%02d".format(seguro / 60, seguro % 60)
    }

    class Factory(
        private val perfilTrabajoRepository: PerfilTrabajoRepository,
        private val resourceProvider: ResourceProvider,
        private val clock: Clock = Clock.systemDefaultZone()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PerfilTrabajoViewModel(
                perfilTrabajoRepository,
                resourceProvider,
                clock
            ) as T
        }
    }
}
