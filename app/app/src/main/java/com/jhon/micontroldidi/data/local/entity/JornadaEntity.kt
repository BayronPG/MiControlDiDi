package com.jhon.micontroldidi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jhon.micontroldidi.domain.NivelCombustible
import com.jhon.micontroldidi.domain.PuntoRevision

/**
 * Jornada de trabajo: lo que el conductor registra al empezar el día.
 *
 * - [fechaHoraInicio] se toma automáticamente del reloj al guardar.
 * - [kilometrajeInicialMetros] guarda el odómetro **en metros** (misma unidad
 *   que el resto de distancias del proyecto, en `Long`).
 * - El nivel de combustible se persiste como texto y se expone tipado en
 *   [nivel]; un valor desconocido degrada a [NivelCombustible.POR_DEFECTO].
 * - La revisión previa son doce columnas booleanas: `true` significa **en buen
 *   estado**. Los puntos en mal estado y los críticos se calculan, no se
 *   persisten.
 */
@Entity(tableName = "jornadas")
data class JornadaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fechaHoraInicio: Long,
    val kilometrajeInicialMetros: Long,
    val nivelCombustible: String = NivelCombustible.POR_DEFECTO.nombre,
    val precioGalonExtra: Long,
    val zonaInicial: String = "",
    val plataforma: String,
    val metaBrutaDia: Long,
    val nivelEnergia: Int,
    val clima: String = "",
    val observaciones: String = "",
    /** Hora de cierre de la jornada. `0` significa jornada abierta. */
    val fechaHoraFin: Long = 0,
    /** Odómetro final en metros. `0` significa jornada abierta. */
    val kilometrajeFinalMetros: Long = 0,
    val llantasOk: Boolean = true,
    val frenosOk: Boolean = true,
    val lucesOk: Boolean = true,
    val direccionalesOk: Boolean = true,
    val cadenaOk: Boolean = true,
    val aceiteOk: Boolean = true,
    val gasolinaOk: Boolean = true,
    val soporteTelefonoOk: Boolean = true,
    val cargaTelefonoOk: Boolean = true,
    val impermeableOk: Boolean = true,
    val documentosOk: Boolean = true,
    val aguaOk: Boolean = true
) {

    /** Nivel de combustible tipado. */
    val nivel: NivelCombustible
        get() = NivelCombustible.fromNombre(nivelCombustible) ?: NivelCombustible.POR_DEFECTO

    /** Indica si la jornada ya se cerró con su odómetro final. */
    val cerrada: Boolean
        get() = fechaHoraFin > 0L

    /**
     * Kilómetros totales recorridos: odómetro final menos inicial.
     * Se calcula; no se persiste. Es 0 mientras la jornada siga abierta.
     */
    val kilometrosTotales: Long
        get() = if (cerrada) {
            (kilometrajeFinalMetros - kilometrajeInicialMetros) / METROS_POR_KM
        } else {
            0L
        }

    /** Odómetro final en kilómetros, solo para mostrar. */
    val kilometrajeFinalKm: Long
        get() = kilometrajeFinalMetros / METROS_POR_KM

    /** Odómetro inicial en kilómetros, solo para mostrar. */
    val kilometrajeInicialKm: Long
        get() = kilometrajeInicialMetros / METROS_POR_KM

    fun estaEnBuenEstado(punto: PuntoRevision): Boolean = when (punto) {
        PuntoRevision.LLANTAS -> llantasOk
        PuntoRevision.FRENOS -> frenosOk
        PuntoRevision.LUCES -> lucesOk
        PuntoRevision.DIRECCIONALES -> direccionalesOk
        PuntoRevision.CADENA -> cadenaOk
        PuntoRevision.ACEITE -> aceiteOk
        PuntoRevision.GASOLINA -> gasolinaOk
        PuntoRevision.SOPORTE_TELEFONO -> soporteTelefonoOk
        PuntoRevision.CARGA_TELEFONO -> cargaTelefonoOk
        PuntoRevision.IMPERMEABLE -> impermeableOk
        PuntoRevision.DOCUMENTOS -> documentosOk
        PuntoRevision.AGUA -> aguaOk
    }

    /** Devuelve una copia con el punto de revisión marcado o desmarcado. */
    fun conRevision(punto: PuntoRevision, enBuenEstado: Boolean): JornadaEntity = when (punto) {
        PuntoRevision.LLANTAS -> copy(llantasOk = enBuenEstado)
        PuntoRevision.FRENOS -> copy(frenosOk = enBuenEstado)
        PuntoRevision.LUCES -> copy(lucesOk = enBuenEstado)
        PuntoRevision.DIRECCIONALES -> copy(direccionalesOk = enBuenEstado)
        PuntoRevision.CADENA -> copy(cadenaOk = enBuenEstado)
        PuntoRevision.ACEITE -> copy(aceiteOk = enBuenEstado)
        PuntoRevision.GASOLINA -> copy(gasolinaOk = enBuenEstado)
        PuntoRevision.SOPORTE_TELEFONO -> copy(soporteTelefonoOk = enBuenEstado)
        PuntoRevision.CARGA_TELEFONO -> copy(cargaTelefonoOk = enBuenEstado)
        PuntoRevision.IMPERMEABLE -> copy(impermeableOk = enBuenEstado)
        PuntoRevision.DOCUMENTOS -> copy(documentosOk = enBuenEstado)
        PuntoRevision.AGUA -> copy(aguaOk = enBuenEstado)
    }

    /** Puntos de la revisión marcados en mal estado. */
    val puntosEnMalEstado: List<PuntoRevision>
        get() = PuntoRevision.entries.filter { !estaEnBuenEstado(it) }

    /** Puntos críticos marcados en mal estado (frenos, llantas, luces). */
    val puntosCriticosEnMalEstado: List<PuntoRevision>
        get() = puntosEnMalEstado.filter { it.critico }

    companion object {
        const val METROS_POR_KM = 1000L

        fun kmAMetros(km: Long): Long = km * METROS_POR_KM
    }
}
