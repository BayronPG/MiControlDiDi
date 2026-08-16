package com.jhon.micontroldidi.util

import com.jhon.micontroldidi.domain.RangoPeriodo
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Convierte las selecciones del DatePicker de Material 3 a rangos semiabiertos
 * [inicioInclusivo, finExclusivo) en la zona horaria del dispositivo.
 *
 * El `selectedDateMillis` que expone el DatePicker de Material 3 está normalizado a
 * **medianoche UTC** (comportamiento del componente). Interpretarlo como hora local
 * desplaza el día seleccionado: en UTC-5 (Colombia) el filtro quedaba un día atrás y
 * excluía el día elegido (hallazgo H-8.7-01).
 */
object CalculadorRangoFiltro {

    /**
     * Rango [inicio, fin) del día seleccionado en el DatePicker.
     *
     * @param fechaSeleccionadaUtcMs medianoche UTC del día elegido, tal como la expone
     * el DatePicker de Material 3 (`selectedDateMillis`).
     * @param zona zona horaria del dispositivo donde se resuelve el rango.
     */
    fun rangoDelDiaSeleccionado(
        fechaSeleccionadaUtcMs: Long,
        zona: ZoneId = ZoneId.systemDefault()
    ): RangoPeriodo {
        val fecha = Instant.ofEpochMilli(fechaSeleccionadaUtcMs)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        return RangoPeriodo(
            inicioInclusivo = fecha.atStartOfDay(zona).toInstant().toEpochMilli(),
            finExclusivo = fecha.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli()
        )
    }

    /**
     * Rango del filtro a partir de las fechas inicial y final seleccionadas en el DatePicker.
     *
     * @return el rango [inicio, fin) si la fecha inicial no es posterior a la final;
     * `null` si el rango es inválido (inicio posterior a fin).
     */
    fun rangoFiltro(
        inicioSeleccionadoUtcMs: Long,
        finSeleccionadoUtcMs: Long,
        zona: ZoneId = ZoneId.systemDefault()
    ): RangoPeriodo? {
        val inicio = rangoDelDiaSeleccionado(inicioSeleccionadoUtcMs, zona).inicioInclusivo
        val fin = rangoDelDiaSeleccionado(finSeleccionadoUtcMs, zona).finExclusivo
        return if (inicio < fin) RangoPeriodo(inicio, fin) else null
    }

    /**
     * Convierte un timestamp al valor que el DatePicker de Material 3 espera como fecha
     * inicial: la medianoche UTC del día local que contiene [epochMs].
     *
     * Útil para inicializar el picker con un filtro ya aplicado (guardado en hora local).
     */
    fun aUtcMedianoche(
        epochMs: Long,
        zona: ZoneId = ZoneId.systemDefault()
    ): Long {
        val fecha = Instant.ofEpochMilli(epochMs).atZone(zona).toLocalDate()
        return fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
}
