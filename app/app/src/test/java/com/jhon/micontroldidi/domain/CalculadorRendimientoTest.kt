package com.jhon.micontroldidi.domain

import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculadorRendimientoTest {

    private fun crearTanqueo(
        id: Long,
        fechaHora: Long,
        odometroMetros: Long,
        litrosMililitros: Long,
        esLleno: Boolean
    ): TanqueoEntity {
        return TanqueoEntity(
            id = id,
            jornadaId = 1L,
            fechaHora = fechaHora,
            odometroMetros = odometroMetros,
            litrosMililitros = litrosMililitros,
            costoTotal = 50_000L,
            esLleno = esLleno,
            precioPorLitro = 10_000L,
            gastoId = 100L + id
        )
    }

    @Test
    fun `sin tanqueos o con menos de dos tanqueos llenos devuelve null`() {
        assertNull("Lista vacía debe ser null", CalculadorRendimiento.calcularKmPorLitro(emptyList()))

        val unLleno = listOf(
            crearTanqueo(1L, 1000L, 10_000_000L, 5_000L, esLleno = true)
        )
        assertNull("Un solo tanqueo lleno debe ser null", CalculadorRendimiento.calcularKmPorLitro(unLleno))

        val dosNoLlenos = listOf(
            crearTanqueo(1L, 1000L, 10_000_000L, 5_000L, esLleno = false),
            crearTanqueo(2L, 2000L, 10_200_000L, 5_000L, esLleno = false)
        )
        assertNull("Tanqueos no llenos no permiten calcular", CalculadorRendimiento.calcularKmPorLitro(dosNoLlenos))

        val unoLlenoYUnoNoLleno = listOf(
            crearTanqueo(1L, 1000L, 10_000_000L, 5_000L, esLleno = true),
            crearTanqueo(2L, 2000L, 10_200_000L, 5_000L, esLleno = false)
        )
        assertNull("Se requieren dos llenos", CalculadorRendimiento.calcularKmPorLitro(unoLlenoYUnoNoLleno))
    }

    @Test
    fun `dos tanqueos llenos consecutivos validos calcula km por litro`() {
        // Tanqueo 1: odómetro 10.000 km (10.000.000 m), lleno.
        // Tanqueo 2: odómetro 10.350 km (10.350.000 m), lleno, 10 litros (10.000 ml).
        // Recorrido: 350 km. Litros: 10.
        // Rendimiento: 350 / 10 = 35 km/l.
        val tanqueos = listOf(
            crearTanqueo(1L, 1000L, 10_000_000L, 10_000L, esLleno = true),
            crearTanqueo(2L, 5000L, 10_350_000L, 10_000L, esLleno = true)
        )

        val rendimiento = CalculadorRendimiento.calcularKmPorLitro(tanqueos)
        assertEquals(35L, rendimiento)
    }

    @Test
    fun `ordena tanqueos cronologicamente antes de calcular`() {
        // Mismos tanqueos pero en orden desordenado en la lista
        val tanqueos = listOf(
            crearTanqueo(2L, 5000L, 10_350_000L, 10_000L, esLleno = true),
            crearTanqueo(1L, 1000L, 10_000_000L, 10_000L, esLleno = true)
        )

        val rendimiento = CalculadorRendimiento.calcularKmPorLitro(tanqueos)
        assertEquals(35L, rendimiento)
    }

    @Test
    fun `ignora tanqueos parciales intermedios y toma los dos ultimos llenos`() {
        // Lleno 1 (t=1000, odo=10.000 km)
        // Parcial (t=2000, odo=10.100 km, parcial)
        // Lleno 2 (t=3000, odo=10.200 km, lleno, 5 litros = 5000 ml)
        // Parcial (t=4000, odo=10.300 km, parcial)
        // Lleno 3 (t=5000, odo=10.500 km, lleno, 10 litros = 10000 ml)
        // Los dos últimos llenos son Lleno 2 (10.200 km) y Lleno 3 (10.500 km).
        // Recorrido: 300 km. Litros: 10 l. Rendimiento: 30 km/l.
        val tanqueos = listOf(
            crearTanqueo(1L, 1000L, 10_000_000L, 10_000L, esLleno = true),
            crearTanqueo(2L, 2000L, 10_100_000L, 3_000L, esLleno = false),
            crearTanqueo(3L, 3000L, 10_200_000L, 5_000L, esLleno = true),
            crearTanqueo(4L, 4000L, 10_300_000L, 4_000L, esLleno = false),
            crearTanqueo(5L, 5000L, 10_500_000L, 10_000L, esLleno = true)
        )

        val rendimiento = CalculadorRendimiento.calcularKmPorLitro(tanqueos)
        assertEquals(30L, rendimiento)
    }

    @Test
    fun `odometros incoherentes devuelve null`() {
        // Odómetro actual menor o igual al anterior
        val odoMenor = listOf(
            crearTanqueo(1L, 1000L, 10_500_000L, 10_000L, esLleno = true),
            crearTanqueo(2L, 2000L, 10_200_000L, 10_000L, esLleno = true)
        )
        assertNull("Odómetro descendente debe retornar null", CalculadorRendimiento.calcularKmPorLitro(odoMenor))

        val odoIgual = listOf(
            crearTanqueo(1L, 1000L, 10_000_000L, 10_000L, esLleno = true),
            crearTanqueo(2L, 2000L, 10_000_000L, 10_000L, esLleno = true)
        )
        assertNull("Odómetro igual (0 km) debe retornar null", CalculadorRendimiento.calcularKmPorLitro(odoIgual))
    }

    @Test
    fun `litros no positivos devuelve null`() {
        val litrosCero = listOf(
            crearTanqueo(1L, 1000L, 10_000_000L, 10_000L, esLleno = true),
            crearTanqueo(2L, 2000L, 10_300_000L, 0L, esLleno = true)
        )
        assertNull("Litros cero debe retornar null", CalculadorRendimiento.calcularKmPorLitro(litrosCero))
    }
}
