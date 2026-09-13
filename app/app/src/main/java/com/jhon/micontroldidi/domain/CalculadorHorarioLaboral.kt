package com.jhon.micontroldidi.domain

import java.time.LocalTime

/**
 * Calcula el estado del horario laboral para una hora del día.
 *
 * Reglas:
 * - Los bloques ([BloqueJornada]) se anclan a `horaInicio` con sus duraciones,
 *   así que el horario completo se desplaza si el conductor cambia su hora de
 *   inicio.
 * - Los rangos son **semiabiertos**: un bloque está activo desde su minuto
 *   inicial y deja de estarlo en su minuto final.
 * - El trabajo y las pausas se cuentan solo hasta `horaFin`; un horario más
 *   corto que la suma de los bloques simplemente recorta los últimos.
 * - **No finaliza la jornada**: únicamente informa el modo regreso y la
 *   advertencia de finalización.
 * - Es una función pura: no depende de Android, de la base de datos ni del reloj.
 *
 * Se asume un horario dentro del mismo día (`horaInicio` anterior a `horaFin`).
 */
object CalculadorHorarioLaboral {

    fun calcular(
        ahora: LocalTime,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): EstadoHorarioLaboral {
        val minutoAhora = ahora.hour * 60 + ahora.minute
        val minutoInicio = horaInicio.hour * 60 + horaInicio.minute
        val minutoFin = horaFin.hour * 60 + horaFin.minute
        val totalJornada = (minutoFin - minutoInicio).coerceAtLeast(0)

        var conectado = 0
        var enPausa = 0
        var bloqueActual: BloqueJornada? = null
        var finBloque: LocalTime? = null
        var proximaPausa: LocalTime? = null

        var cursor = minutoInicio
        for (bloque in BloqueJornada.entries) {
            val inicioBloque = cursor
            val finTeorico = inicioBloque + bloque.duracionMinutos
            cursor = finTeorico

            // Si el horario del perfil es más corto que la suma de bloques,
            // los últimos bloques quedan recortados por la hora de fin.
            val finEfectivo = minOf(finTeorico, minutoFin)

            if (minutoAhora in inicioBloque until finEfectivo) {
                bloqueActual = bloque
                finBloque = aHora(finEfectivo)
            }

            val transcurrido = (minOf(minutoAhora, finEfectivo) - inicioBloque)
                .coerceAtLeast(0)
            if (bloque.esTrabajo) {
                conectado += transcurrido
            } else {
                enPausa += transcurrido
            }

            // "Siguiente pausa" es la próxima que todavía no ha empezado.
            if (bloque.esPausa && proximaPausa == null && inicioBloque > minutoAhora) {
                proximaPausa = aHora(inicioBloque)
            }
        }

        val transcurridos = (minutoAhora - minutoInicio).coerceIn(0, totalJornada)
        val progreso = if (totalJornada == 0) 0 else transcurridos * 100 / totalJornada

        return EstadoHorarioLaboral(
            horaInicio = horaInicio,
            horaFin = horaFin,
            enJornada = minutoAhora in minutoInicio until minutoFin,
            bloqueActual = bloqueActual,
            finBloque = finBloque,
            proximaPausa = proximaPausa,
            progresoPorcentaje = progreso,
            minutosConectado = conectado,
            minutosEnPausa = enPausa,
            minutosRestantes = (totalJornada - transcurridos).coerceAtLeast(0),
            modoRegreso = bloqueActual == BloqueJornada.REGRESO_PRODUCTIVO,
            advertenciaFinalizacion = minutoAhora >= minutoFin
        )
    }

    private fun aHora(minutosDelDia: Int): LocalTime {
        val seguro = minutosDelDia.coerceIn(0, 1439)
        return LocalTime.of(seguro / 60, seguro % 60)
    }
}
