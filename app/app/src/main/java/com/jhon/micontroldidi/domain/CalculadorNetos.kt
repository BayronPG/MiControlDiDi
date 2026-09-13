package com.jhon.micontroldidi.domain

/**
 * Resultado de los cálculos netos operativos y económicos.
 *
 * - En jornada abierta, las métricas diferidas ([reservaMantenimientoTotal],
 *   [costoCombustiblePorKm] y [netoEconomico]) son null y [esPendiente] es true.
 * - Dinero en [Long] entero (pesos colombianos).
 */
data class ResultadoNetos(
    val ingresos: Long,
    val gastos: Long,
    val netoOperativo: Long,
    val reservaMantenimientoTotal: Long?,
    val costoCombustiblePorKm: Long?,
    val netoEconomico: Long?,
    val esPendiente: Boolean
)

/**
 * Calculador puro de netos operativos y económicos.
 *
 * Reglas de cálculo (ADR-001 D-06, D-08, D-15, D-16):
 * - Neto operativo = ingresos (valor + propina) - gastos reales.
 * - La gasolina sincronizada desde tanqueos ya vive dentro de gastos: no se suma ni resta dos veces.
 * - Los peajes son informativos: no se descuentan automáticamente.
 * - reservaTotal = (kmTotalesMetros * reservaTotalPorKm) / 1000L (sin truncar metros antes de multiplicar).
 * - costoCombustiblePorKm = (gastoGasolina * 1000L) / kmTotalesMetros.
 * - Neto económico = netoOperativo - reservaTotal.
 */
object CalculadorNetos {

    fun calcular(
        ingresos: Long,
        gastosReales: Long,
        gastoGasolina: Long,
        kmTotalesMetros: Long,
        reservaTotalPorKm: Long,
        jornadaCerrada: Boolean
    ): ResultadoNetos {
        val netoOperativo = ingresos - gastosReales

        if (!jornadaCerrada) {
            return ResultadoNetos(
                ingresos = ingresos,
                gastos = gastosReales,
                netoOperativo = netoOperativo,
                reservaMantenimientoTotal = null,
                costoCombustiblePorKm = null,
                netoEconomico = null,
                esPendiente = true
            )
        }

        val reservaTotal = if (kmTotalesMetros > 0L && reservaTotalPorKm > 0L) {
            (kmTotalesMetros * reservaTotalPorKm) / 1000L
        } else {
            0L
        }

        val costoCombustiblePorKm = if (kmTotalesMetros > 0L && gastoGasolina > 0L) {
            (gastoGasolina * 1000L) / kmTotalesMetros
        } else {
            0L
        }

        val netoEconomico = netoOperativo - reservaTotal

        return ResultadoNetos(
            ingresos = ingresos,
            gastos = gastosReales,
            netoOperativo = netoOperativo,
            reservaMantenimientoTotal = reservaTotal,
            costoCombustiblePorKm = costoCombustiblePorKm,
            netoEconomico = netoEconomico,
            esPendiente = false
        )
    }
}
