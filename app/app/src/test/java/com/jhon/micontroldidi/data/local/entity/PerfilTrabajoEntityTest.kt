package com.jhon.micontroldidi.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class PerfilTrabajoEntityTest {

    @Test
    fun `valores por defecto del perfil`() {
        val perfil = PerfilTrabajoEntity()

        assertEquals(1, perfil.id)
        assertEquals("inDrive", perfil.plataforma)
        assertEquals("TVS Raider 125 FI", perfil.vehiculo)
        assertEquals("Extra", perfil.tipoCombustible)
        assertEquals("Medellín", perfil.ciudad)
        assertEquals("1,2,3,4,5", perfil.diasLaborales)
        assertEquals(360, perfil.horaInicioMinutos)
        assertEquals(900, perfil.horaFinMinutos)
        assertEquals(25, perfil.maxPorcentajeKmVacios)
    }

    @Test
    fun `reserva por km divide el costo entre el intervalo`() {
        assertEquals(20L, PerfilTrabajoEntity.reservaPorKm(costo = 60000, intervaloKm = 3000))
    }

    @Test
    fun `reserva por km sin intervalo es cero`() {
        assertEquals(0L, PerfilTrabajoEntity.reservaPorKm(costo = 60000, intervaloKm = 0))
        assertEquals(0L, PerfilTrabajoEntity.reservaPorKm(costo = 0, intervaloKm = 0))
    }

    @Test
    fun `reserva por km sin costo es cero`() {
        assertEquals(0L, PerfilTrabajoEntity.reservaPorKm(costo = 0, intervaloKm = 5000))
    }

    @Test
    fun `reserva por km trunca la division`() {
        // 50000 / 3000 = 16.66 → 16 pesos por kilómetro
        assertEquals(16L, PerfilTrabajoEntity.reservaPorKm(costo = 50000, intervaloKm = 3000))
    }

    @Test
    fun `reserva total suma las seis reservas`() {
        val perfil = PerfilTrabajoEntity(
            costoAceite = 60000, intervaloAceiteKm = 3000,
            costoLlantas = 180000, intervaloLlantasKm = 12000,
            costoFrenos = 80000, intervaloFrenosKm = 8000,
            costoKitArrastre = 90000, intervaloKitArrastreKm = 9000,
            costoMantenimiento = 40000, intervaloMantenimientoKm = 4000,
            costoDepreciacion = 240000, intervaloDepreciacionKm = 24000
        )

        // 20 + 15 + 10 + 10 + 10 + 10
        assertEquals(75L, perfil.reservaTotalPorKm)
    }

    @Test
    fun `reserva total por defecto es cero`() {
        assertEquals(0L, PerfilTrabajoEntity().reservaTotalPorKm)
    }

    @Test
    fun `dias laborales se interpretan desde el CSV`() {
        assertEquals(setOf(1, 2, 3, 4, 5), PerfilTrabajoEntity().diasLaboralesSet)
    }

    @Test
    fun `dias laborales ignoran valores invalidos`() {
        val perfil = PerfilTrabajoEntity(diasLaborales = "1,9,0,x, 3 ")
        assertEquals(setOf(1, 3), perfil.diasLaboralesSet)
    }

    @Test
    fun `dias laborales vacios no producen dias`() {
        assertEquals(emptySet<Int>(), PerfilTrabajoEntity(diasLaborales = "").diasLaboralesSet)
    }

    @Test
    fun `horas se convierten a LocalTime`() {
        val perfil = PerfilTrabajoEntity(horaInicioMinutos = 360, horaFinMinutos = 900)

        assertEquals(LocalTime.of(6, 0), perfil.horaInicio)
        assertEquals(LocalTime.of(15, 0), perfil.horaFin)
    }

    @Test
    fun `hora fuera de rango se satura`() {
        assertEquals(LocalTime.of(23, 59), PerfilTrabajoEntity(horaInicioMinutos = 99999).horaInicio)
        assertEquals(LocalTime.of(0, 0), PerfilTrabajoEntity(horaInicioMinutos = -50).horaInicio)
    }

    @Test
    fun `formatear dias ordena y descarta invalidos`() {
        assertEquals("1,2,3", PerfilTrabajoEntity.formatearDias(setOf(3, 1, 2)))
        assertEquals("1,7", PerfilTrabajoEntity.formatearDias(setOf(7, 9, 1)))
        assertEquals("", PerfilTrabajoEntity.formatearDias(emptySet()))
    }
}
