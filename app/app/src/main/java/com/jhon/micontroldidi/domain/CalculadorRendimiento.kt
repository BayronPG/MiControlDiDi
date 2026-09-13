package com.jhon.micontroldidi.domain

import com.jhon.micontroldidi.data.local.entity.TanqueoEntity

/**
 * Calculador puro del rendimiento de combustible entre tanqueos completos.
 *
 * Reglas de negocio (ADR-001 D-16):
 * - Solo se calcula entre dos tanqueos consecutivos con [TanqueoEntity.esLleno] == true.
 * - Utiliza la diferencia positiva de odómetros y los litros del segundo llenado.
 * - Si faltan dos llenados completos o los datos son incoherentes (odómetro no creciente o
 *   litros <= 0), retorna null.
 * - No realiza estimaciones con el combustible aproximado de la jornada.
 */
object CalculadorRendimiento {

    fun calcularKmPorLitro(tanqueos: List<TanqueoEntity>): Long? {
        val llenos = tanqueos
            .filter { it.esLleno }
            .sortedBy { it.fechaHora }

        if (llenos.size < 2) return null

        val anterior = llenos[llenos.size - 2]
        val actual = llenos[llenos.size - 1]

        val diffOdometroMetros = actual.odometroMetros - anterior.odometroMetros
        val litrosMililitros = actual.litrosMililitros

        if (diffOdometroMetros <= 0L || litrosMililitros <= 0L) {
            return null
        }

        // diffOdometroMetros / litrosMililitros = (diffKm * 1000) / (litros * 1000) = diffKm / litros
        return diffOdometroMetros / litrosMililitros
    }
}
