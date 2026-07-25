package com.jhon.micontroldidi.domain

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/**
 * Calcula rangos temporales semiabiertos [inicio, fin) para día, semana y mes.
 *
 * No depende de [java.time.Clock], [ZoneId.systemDefault] ni [java.time.LocalDate.now]
 * internamente; recibe explícitamente la fecha de referencia y la zona horaria.
 */
object CalculadorRangoPeriodo {

    /**
     * Calcula el [RangoPeriodo] correspondiente al [periodo] que contiene la [fechaReferencia]
     * en la zona horaria [zoneId].
     *
     * @param periodo          Periodo deseado (DIA, SEMANA, MES).
     * @param fechaReferencia  Fecha/hora de referencia en milisegundos desde epoch.
     * @param zoneId           Zona horaria para determinar los límites del periodo.
     * @return                 Rango semiabierto [inicioInclusivo, finExclusivo).
     */
    fun calcular(
        periodo: PeriodoDashboard,
        fechaReferencia: Long,
        zoneId: ZoneId
    ): RangoPeriodo {
        val zdt = ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(fechaReferencia),
            zoneId
        )
        val fechaLocal = zdt.toLocalDate()

        return when (periodo) {
            PeriodoDashboard.DIA -> {
                val inicio = fechaLocal.atStartOfDay(zoneId)
                val fin = fechaLocal.plusDays(1).atStartOfDay(zoneId)
                RangoPeriodo(
                    inicioInclusivo = inicio.toInstant().toEpochMilli(),
                    finExclusivo = fin.toInstant().toEpochMilli()
                )
            }

            PeriodoDashboard.SEMANA -> {
                val lunes = fechaLocal.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val inicio = lunes.atStartOfDay(zoneId)
                val fin = lunes.plusWeeks(1).atStartOfDay(zoneId)
                RangoPeriodo(
                    inicioInclusivo = inicio.toInstant().toEpochMilli(),
                    finExclusivo = fin.toInstant().toEpochMilli()
                )
            }

            PeriodoDashboard.MES -> {
                val primero = fechaLocal.withDayOfMonth(1)
                val inicio = primero.atStartOfDay(zoneId)
                val fin = primero.plusMonths(1).atStartOfDay(zoneId)
                RangoPeriodo(
                    inicioInclusivo = inicio.toInstant().toEpochMilli(),
                    finExclusivo = fin.toInstant().toEpochMilli()
                )
            }
        }
    }
}
