package com.jhon.micontroldidi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * Perfil de trabajo del conductor: contexto de la operación diaria.
 *
 * - Es una tabla de **fila única**: siempre existe como máximo un perfil
 *   (identificado por [ID_UNICO]).
 * - Guarda únicamente **datos de entrada** (lo que el conductor decide):
 *   plataforma, vehículo, horario, umbral y el costo y el intervalo de cada
 *   mantenimiento. La **reserva por kilómetro se calcula** con
 *   [reservaPorKm] y no se persiste.
 * - Los intervalos de mantenimiento son distancias expresadas en kilómetros
 *   (`Long`), igual que el resto de distancias del proyecto.
 */
@Entity(tableName = "perfil_trabajo")
data class PerfilTrabajoEntity(
    @PrimaryKey val id: Int = ID_UNICO,
    val plataforma: String = PLATAFORMA_POR_DEFECTO,
    val plataformasDisponibles: String = PLATAFORMAS_POR_DEFECTO,
    val vehiculo: String = VEHICULO_POR_DEFECTO,
    val tipoCombustible: String = COMBUSTIBLE_POR_DEFECTO,
    val ciudad: String = CIUDAD_POR_DEFECTO,
    /** Días laborales en formato CSV con numeración ISO (1 = lunes … 7 = domingo). */
    val diasLaborales: String = DIAS_POR_DEFECTO,
    /** Minutos transcurridos desde la medianoche (360 = 06:00). */
    val horaInicioMinutos: Int = HORA_INICIO_POR_DEFECTO,
    /** Minutos transcurridos desde la medianoche (900 = 15:00). */
    val horaFinMinutos: Int = HORA_FIN_POR_DEFECTO,
    /** Porcentaje máximo aceptable de kilómetros en vacío (0 a 100). */
    val maxPorcentajeKmVacios: Int = MAX_KMVACIOS_POR_DEFECTO,
    val costoAceite: Long = 0,
    val intervaloAceiteKm: Long = 0,
    val costoLlantas: Long = 0,
    val intervaloLlantasKm: Long = 0,
    val costoFrenos: Long = 0,
    val intervaloFrenosKm: Long = 0,
    val costoKitArrastre: Long = 0,
    val intervaloKitArrastreKm: Long = 0,
    val costoMantenimiento: Long = 0,
    val intervaloMantenimientoKm: Long = 0,
    val costoDepreciacion: Long = 0,
    val intervaloDepreciacionKm: Long = 0,
    val actualizadoEn: Long = 0
) {

    /** Días laborales normalizados (solo valores válidos de 1 a 7). */
    val diasLaboralesSet: Set<Int>
        get() = diasLaborales.split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
            .toSet()

    val horaInicio: LocalTime
        get() = minutosAHora(horaInicioMinutos)

    val horaFin: LocalTime
        get() = minutosAHora(horaFinMinutos)

    /** Reserva por kilómetro del aceite (costo ÷ intervalo; 0 si no configurado). */
    val reservaAceitePorKm: Long get() = reservaPorKm(costoAceite, intervaloAceiteKm)

    /** Reserva por kilómetro de las llantas. */
    val reservaLlantasPorKm: Long get() = reservaPorKm(costoLlantas, intervaloLlantasKm)

    /** Reserva por kilómetro de los frenos. */
    val reservaFrenosPorKm: Long get() = reservaPorKm(costoFrenos, intervaloFrenosKm)

    /** Reserva por kilómetro del kit de arrastre. */
    val reservaKitArrastrePorKm: Long get() = reservaPorKm(costoKitArrastre, intervaloKitArrastreKm)

    /** Reserva por kilómetro del mantenimiento general. */
    val reservaMantenimientoGeneralPorKm: Long
        get() = reservaPorKm(costoMantenimiento, intervaloMantenimientoKm)

    /** Reserva por kilómetro de la depreciación del vehículo. */
    val reservaDepreciacionPorKm: Long get() = reservaPorKm(costoDepreciacion, intervaloDepreciacionKm)

    /**
     * Reserva total por kilómetro: suma de las seis reservas configuradas.
     * Es un valor calculado; no se persiste.
     */
    val reservaTotalPorKm: Long
        get() = reservaAceitePorKm +
            reservaLlantasPorKm +
            reservaFrenosPorKm +
            reservaKitArrastrePorKm +
            reservaMantenimientoGeneralPorKm +
            reservaDepreciacionPorKm

    companion object {
        /** Identificador de la única fila del perfil. */
        const val ID_UNICO = 1

        const val PLATAFORMA_POR_DEFECTO = "inDrive"
        const val PLATAFORMAS_POR_DEFECTO = "inDrive, DiDi"
        const val VEHICULO_POR_DEFECTO = "TVS Raider 125 FI"
        const val COMBUSTIBLE_POR_DEFECTO = "Extra"
        const val CIUDAD_POR_DEFECTO = "Medellín"
        const val DIAS_POR_DEFECTO = "1,2,3,4,5"
        const val HORA_INICIO_POR_DEFECTO = 360
        const val HORA_FIN_POR_DEFECTO = 900
        const val MAX_KMVACIOS_POR_DEFECTO = 25

        private const val MINUTOS_DIA = 24 * 60

        /**
         * Reserva por kilómetro de un mantenimiento: costo ÷ intervalo.
         * Devuelve 0 cuando el intervalo no está configurado.
         */
        fun reservaPorKm(costo: Long, intervaloKm: Long): Long =
            if (intervaloKm > 0) costo / intervaloKm else 0L

        /** Convierte un conjunto de días ISO (1 lunes … 7 domingo) a CSV ordenado. */
        fun formatearDias(dias: Set<Int>): String = dias
            .filter { it in 1..7 }
            .sorted()
            .joinToString(",")

        /** Convierte hora y minuto a minutos desde la medianoche. */
        fun horaAMinutos(hora: Int, minuto: Int): Int = hora * 60 + minuto

        private fun minutosAHora(minutosDelDia: Int): LocalTime {
            val seguro = minutosDelDia.coerceIn(0, MINUTOS_DIA - 1)
            return LocalTime.of(seguro / 60, seguro % 60)
        }
    }
}
