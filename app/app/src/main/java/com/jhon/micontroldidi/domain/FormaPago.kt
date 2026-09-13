package com.jhon.micontroldidi.domain

/**
 * Forma de pago con la que el pasajero pagó el viaje.
 *
 * Es un catálogo cerrado. Se persiste como texto estable ([nombre]) y los viajes
 * migrados desde versiones anteriores quedan con cadena vacía, así que
 * [fromNombre] devuelve `null` para la cadena vacía y para cualquier valor
 * desconocido.
 */
enum class FormaPago(val nombre: String) {
    EFECTIVO("EFECTIVO"),
    TRANSFERENCIA("TRANSFERENCIA"),
    TARJETA("TARJETA"),
    OTRO("OTRO");

    companion object {
        fun fromNombre(nombre: String?): FormaPago? =
            entries.firstOrNull { it.nombre.equals(nombre, ignoreCase = true) }
    }
}
