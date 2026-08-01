package com.jhon.micontroldidi.domain

/**
 * Periodo de una meta de ganancia.
 *
 * Reemplaza las strings crudas "DIA"/"MES" usadas antes en el código,
 * eliminando errores por typos (p. ej. "dia" en lugar de "DIA").
 *
 * Se persiste en la base de datos como [nombre] (texto "DIA" o "MES"),
 * por lo que el esquema de Room no cambia.
 */
enum class PeriodoMeta(val nombre: String) {
    DIA("DIA"),
    MES("MES");

    companion object {
        /**
         * Convierte el texto persistido a [PeriodoMeta].
         * Devuelve null si el texto no corresponde a ningún periodo conocido.
         */
        fun fromNombre(nombre: String): PeriodoMeta? =
            entries.firstOrNull { it.nombre == nombre }
    }
}
