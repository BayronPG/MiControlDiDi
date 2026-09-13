package com.jhon.micontroldidi.domain

import java.time.LocalTime

/**
 * Estado del horario laboral en un instante del día.
 *
 * Todo lo que expone es informativo: **nunca** implica cerrar la jornada
 * automáticamente. La decisión siempre es del conductor.
 */
data class EstadoHorarioLaboral(
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    /** Verdadero si el instante consultado cae dentro del horario. */
    val enJornada: Boolean,
    /** Bloque activo, o `null` antes de empezar o al terminar la jornada. */
    val bloqueActual: BloqueJornada? = null,
    /** Hora en que termina el bloque activo. */
    val finBloque: LocalTime? = null,
    /** Inicio de la próxima pausa pendiente del día (si queda alguna). */
    val proximaPausa: LocalTime? = null,
    /** Progreso de la jornada, de 0 a 100. */
    val progresoPorcentaje: Int = 0,
    /** Minutos de trabajo ya transcurridos. */
    val minutosConectado: Int = 0,
    /** Minutos de pausa ya transcurridos. */
    val minutosEnPausa: Int = 0,
    /** Minutos que faltan para el final de la jornada. */
    val minutosRestantes: Int = 0,
    /** Verdadero durante el bloque de regreso productivo (14:30 – 15:00 por defecto). */
    val modoRegreso: Boolean = false,
    /** Verdadero a partir de la hora de finalización; solo advierte. */
    val advertenciaFinalizacion: Boolean = false
)
