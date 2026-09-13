package com.jhon.micontroldidi.domain

/**
 * Puntos de la revisión previa a la jornada.
 *
 * [critico] marca los que exigen una advertencia visible cuando están en mal
 * estado (frenos, llantas y luces). La advertencia **nunca** impide usar la
 * aplicación: solo avisa.
 */
enum class PuntoRevision(val critico: Boolean) {
    LLANTAS(true),
    FRENOS(true),
    LUCES(true),
    DIRECCIONALES(false),
    CADENA(false),
    ACEITE(false),
    GASOLINA(false),
    SOPORTE_TELEFONO(false),
    CARGA_TELEFONO(false),
    IMPERMEABLE(false),
    DOCUMENTOS(false),
    AGUA(false);

    companion object {
        /** Puntos que generan advertencia cuando están en mal estado. */
        val CRITICOS: List<PuntoRevision> get() = entries.filter { it.critico }
    }
}
