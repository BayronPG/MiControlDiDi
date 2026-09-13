package com.jhon.micontroldidi.ui.jornada

import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import com.jhon.micontroldidi.domain.NivelCombustible
import com.jhon.micontroldidi.domain.PuntoRevision

/** Tipo de dato que acepta cada campo del formulario de jornada. */
enum class TipoCampoJornada { TEXTO, ENTERO }

/** Campos editables del registro de jornada. */
enum class CampoJornada(val tipo: TipoCampoJornada) {
    KILOMETRAJE_INICIAL(TipoCampoJornada.ENTERO),
    PRECIO_GALON(TipoCampoJornada.ENTERO),
    ZONA_INICIAL(TipoCampoJornada.TEXTO),
    PLATAFORMA(TipoCampoJornada.TEXTO),
    META_BRUTA(TipoCampoJornada.ENTERO),
    NIVEL_ENERGIA(TipoCampoJornada.ENTERO),
    CLIMA(TipoCampoJornada.TEXTO),
    OBSERVACIONES(TipoCampoJornada.TEXTO)
}

/**
 * Estado del registro de jornada.
 *
 * [revision] guarda los doce puntos de la revisión previa: `true` significa
 * **en buen estado**. Los puntos en mal estado y los críticos se derivan.
 */
data class JornadaUiState(
    val cargando: Boolean = true,
    val valores: Map<CampoJornada, String> = emptyMap(),
    val nivelCombustible: NivelCombustible = NivelCombustible.POR_DEFECTO,
    val revision: Map<PuntoRevision, Boolean> = PuntoRevision.entries.associateWith { true },
    val errores: Map<CampoJornada, String> = emptyMap(),
    /** Jornada ya registrada hoy, si existe. */
    val jornadaDeHoy: JornadaEntity? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    /** Odómetro final digitado para cerrar la jornada de hoy. */
    val odometroFinalText: String = "",
    val errorOdometroFinal: String? = null,
    val errorCierre: String? = null,
    val cerrando: Boolean = false,
    val cierreExitoso: Boolean = false,
    val mensajeError: String? = null
) {

    fun valor(campo: CampoJornada): String = valores[campo].orEmpty()

    fun error(campo: CampoJornada): String? = errores[campo]

    fun estaEnBuenEstado(punto: PuntoRevision): Boolean = revision[punto] ?: true

    val puntosEnMalEstado: List<PuntoRevision>
        get() = PuntoRevision.entries.filter { !estaEnBuenEstado(it) }

    val puntosCriticosEnMalEstado: List<PuntoRevision>
        get() = puntosEnMalEstado.filter { it.critico }

    val formularioValido: Boolean
        get() = valores.isNotEmpty() && errores.isEmpty() && !guardando
}
