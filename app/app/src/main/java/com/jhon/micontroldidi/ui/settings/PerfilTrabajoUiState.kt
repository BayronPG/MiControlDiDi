package com.jhon.micontroldidi.ui.settings

import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity

/** Tipo de dato que acepta cada campo del perfil. */
enum class TipoCampo { TEXTO, ENTERO, HORA }

/**
 * Campos editables del perfil de trabajo. Se usan como clave del formulario
 * para no repetir un campo de estado por cada valor.
 */
enum class CampoPerfil(val tipo: TipoCampo) {
    PLATAFORMA(TipoCampo.TEXTO),
    PLATAFORMAS_DISPONIBLES(TipoCampo.TEXTO),
    VEHICULO(TipoCampo.TEXTO),
    TIPO_COMBUSTIBLE(TipoCampo.TEXTO),
    CIUDAD(TipoCampo.TEXTO),
    HORA_INICIO(TipoCampo.HORA),
    HORA_FIN(TipoCampo.HORA),
    MAX_KM_VACIOS(TipoCampo.ENTERO),
    COSTO_ACEITE(TipoCampo.ENTERO),
    INTERVALO_ACEITE(TipoCampo.ENTERO),
    COSTO_LLANTAS(TipoCampo.ENTERO),
    INTERVALO_LLANTAS(TipoCampo.ENTERO),
    COSTO_FRENOS(TipoCampo.ENTERO),
    INTERVALO_FRENOS(TipoCampo.ENTERO),
    COSTO_KIT_ARRASTRE(TipoCampo.ENTERO),
    INTERVALO_KIT_ARRASTRE(TipoCampo.ENTERO),
    COSTO_MANTENIMIENTO(TipoCampo.ENTERO),
    INTERVALO_MANTENIMIENTO(TipoCampo.ENTERO),
    COSTO_DEPRECIACION(TipoCampo.ENTERO),
    INTERVALO_DEPRECIACION(TipoCampo.ENTERO)
}

/** Par costo/intervalo de un mantenimiento. */
data class ParReserva(val costo: CampoPerfil, val intervalo: CampoPerfil)

/** Los seis mantenimientos que alimentan la reserva por kilómetro. */
val RESERVAS: List<ParReserva> = listOf(
    ParReserva(CampoPerfil.COSTO_ACEITE, CampoPerfil.INTERVALO_ACEITE),
    ParReserva(CampoPerfil.COSTO_LLANTAS, CampoPerfil.INTERVALO_LLANTAS),
    ParReserva(CampoPerfil.COSTO_FRENOS, CampoPerfil.INTERVALO_FRENOS),
    ParReserva(CampoPerfil.COSTO_KIT_ARRASTRE, CampoPerfil.INTERVALO_KIT_ARRASTRE),
    ParReserva(CampoPerfil.COSTO_MANTENIMIENTO, CampoPerfil.INTERVALO_MANTENIMIENTO),
    ParReserva(CampoPerfil.COSTO_DEPRECIACION, CampoPerfil.INTERVALO_DEPRECIACION)
)

/**
 * Estado de la pantalla del perfil de trabajo.
 *
 * Los valores del formulario viven en [valores] (siempre como texto, tal como
 * los escribe el usuario) y los errores por campo en [errores].
 */
data class PerfilTrabajoUiState(
    val cargando: Boolean = true,
    val valores: Map<CampoPerfil, String> = emptyMap(),
    val diasSeleccionados: Set<Int> = emptySet(),
    val errores: Map<CampoPerfil, String> = emptyMap(),
    val errorDias: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val mensajeError: String? = null
) {

    fun valor(campo: CampoPerfil): String = valores[campo].orEmpty()

    fun error(campo: CampoPerfil): String? = errores[campo]

    fun estaSeleccionado(dia: Int): Boolean = diasSeleccionados.contains(dia)

    val formularioValido: Boolean
        get() = valores.isNotEmpty() && errores.isEmpty() && errorDias == null && !guardando

    /** Reserva por kilómetro de un mantenimiento (valor calculado, no persistido). */
    fun reservaPorKm(par: ParReserva): Long = PerfilTrabajoEntity.reservaPorKm(
        costo = valor(par.costo).toLongOrNull() ?: 0L,
        intervaloKm = valor(par.intervalo).toLongOrNull() ?: 0L
    )

    /** Reserva total por kilómetro: suma de los seis mantenimientos. */
    val reservaTotalPorKm: Long
        get() = RESERVAS.sumOf { reservaPorKm(it) }
}
