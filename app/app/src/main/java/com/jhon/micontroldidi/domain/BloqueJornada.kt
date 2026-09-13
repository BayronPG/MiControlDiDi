package com.jhon.micontroldidi.domain

/** Naturaleza de un bloque del horario: se trabaja o se descansa. */
enum class TipoBloque { TRABAJO, PAUSA }

/**
 * Bloques del horario laboral, en orden, con su duración en minutos.
 *
 * Las duraciones reproducen el horario definido para MiControlDiDi
 * (9 horas en total: 8 de trabajo y 1 de pausas) y se **anclan a la hora de
 * inicio del perfil de trabajo**, de modo que todo el horario se desplace
 * cuando el conductor cambie su hora de inicio.
 *
 * Con el inicio por defecto (06:00):
 *
 * | Bloque | Horario |
 * |---|---|
 * | [PRINCIPAL] | 06:00 – 08:30 |
 * | [PRIMERA_PAUSA] | 08:30 – 08:45 |
 * | [SEGUNDO_BLOQUE] | 08:45 – 11:00 |
 * | [SNACK_REVISION] | 11:00 – 11:15 |
 * | [BLOQUE_SELECTIVO] | 11:15 – 12:30 |
 * | [ALMUERZO] | 12:30 – 13:00 |
 * | [BLOQUE_FINAL] | 13:00 – 14:30 |
 * | [REGRESO_PRODUCTIVO] | 14:30 – 15:00 |
 */
enum class BloqueJornada(val tipo: TipoBloque, val duracionMinutos: Int) {
    PRINCIPAL(TipoBloque.TRABAJO, 150),
    PRIMERA_PAUSA(TipoBloque.PAUSA, 15),
    SEGUNDO_BLOQUE(TipoBloque.TRABAJO, 135),
    SNACK_REVISION(TipoBloque.PAUSA, 15),
    BLOQUE_SELECTIVO(TipoBloque.TRABAJO, 75),
    ALMUERZO(TipoBloque.PAUSA, 30),
    BLOQUE_FINAL(TipoBloque.TRABAJO, 90),
    REGRESO_PRODUCTIVO(TipoBloque.TRABAJO, 30);

    val esTrabajo: Boolean get() = tipo == TipoBloque.TRABAJO

    val esPausa: Boolean get() = tipo == TipoBloque.PAUSA

    companion object {
        /** Duración total del horario: 540 minutos (9 horas). */
        val DURACION_TOTAL_MINUTOS: Int
            get() = entries.sumOf { it.duracionMinutos }

        /** Minutos de trabajo del horario completo: 480 (8 horas). */
        val MINUTOS_TRABAJO: Int
            get() = entries.filter { it.esTrabajo }.sumOf { it.duracionMinutos }

        /** Minutos de pausa del horario completo: 60 (1 hora). */
        val MINUTOS_PAUSA: Int
            get() = entries.filter { it.esPausa }.sumOf { it.duracionMinutos }
    }
}
