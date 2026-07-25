package com.jhon.micontroldidi.domain

/**
 * Intervalo temporal semiabierto [inicioInclusivo, finExclusivo) en milisegundos desde epoch.
 *
 * @param inicioInclusivo Milisegundo exacto de inicio (incluido en el periodo).
 * @param finExclusivo    Milisegundo exacto de fin (excluido del periodo).
 */
data class RangoPeriodo(
    val inicioInclusivo: Long,
    val finExclusivo: Long
) {
    init {
        require(finExclusivo > inicioInclusivo) {
            "finExclusivo ($finExclusivo) debe ser mayor que inicioInclusivo ($inicioInclusivo)"
        }
    }
}
