package com.jhon.micontroldidi.domain

/**
 * Nivel aproximado de combustible al iniciar la jornada.
 *
 * Es deliberadamente aproximado (no un porcentaje exacto): el consumo real se
 * mide con los tanqueos del incremento de gasolina, no con las barras del
 * indicador.
 */
enum class NivelCombustible(val nombre: String) {
    RESERVA("RESERVA"),
    CUARTO("CUARTO"),
    MEDIO("MEDIO"),
    TRES_CUARTOS("TRES_CUARTOS"),
    LLENO("LLENO");

    companion object {
        val POR_DEFECTO: NivelCombustible = MEDIO

        fun fromNombre(nombre: String?): NivelCombustible? =
            entries.firstOrNull { it.nombre.equals(nombre, ignoreCase = true) }
    }
}
