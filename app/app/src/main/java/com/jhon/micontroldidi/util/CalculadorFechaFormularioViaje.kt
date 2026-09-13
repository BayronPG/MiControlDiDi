package com.jhon.micontroldidi.util

/**
 * Decide la fecha y hora informativa que se muestra en el formulario de viaje.
 *
 * - Creación de un viaje nuevo: la fecha y hora actuales.
 * - Edición de un viaje ya cargado: su fecha y hora originales.
 * - Edición todavía sin cargar: `null`, para no mostrar una fecha incorrecta.
 *
 * Es una función pura para poder probarla sin Android.
 */
object CalculadorFechaFormularioViaje {

    fun fechaVisible(
        esEdicion: Boolean,
        editando: Boolean,
        fechaHoraOriginal: Long,
        ahora: Long
    ): Long? = when {
        !esEdicion -> ahora
        editando && fechaHoraOriginal > 0L -> fechaHoraOriginal
        else -> null
    }
}
