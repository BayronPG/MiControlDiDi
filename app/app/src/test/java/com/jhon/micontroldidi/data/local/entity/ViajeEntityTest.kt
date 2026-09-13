package com.jhon.micontroldidi.data.local.entity

import com.jhon.micontroldidi.domain.FormaPago
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ViajeEntityTest {

    @Test
    fun `viajeValido calcula ingresoTotal correctamente`() {
        val viaje = ViajeEntity(
            id = 1,
            fechaHora = 1000L,
            valor = 15000,
            propina = 3000,
            observacion = "Buen viaje"
        )
        assertEquals(18000, viaje.ingresoTotal)
    }

    @Test
    fun `viajeSinPropina ingresoTotal es igual al valor`() {
        val viaje = ViajeEntity(
            fechaHora = 1000L,
            valor = 12000,
            propina = 0
        )
        assertEquals(12000, viaje.ingresoTotal)
    }

    @Test
    fun `viajeSinObservacion tiene observacion vacia`() {
        val viaje = ViajeEntity(
            fechaHora = 1000L,
            valor = 10000
        )
        assertEquals("", viaje.observacion)
    }

    @Test
    fun `ingresoTotal no es una columna persistida`() {
        // ingresoTotal es una propiedad calculada (val con getter),
        // no una columna de Room. Room solo persiste propiedades
        // del constructor primario. Esta prueba verifica que no
        // esté en el constructor.
        val campos = ViajeEntity::class.java.declaredFields
            .map { it.name }
            .toSet()
        assertFalse(
            "ingresoTotal no debería ser un campo declarado de la clase",
            campos.contains("ingresoTotal")
        )
    }

    @Test
    fun `propina por defecto es cero`() {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = 8000)
        assertEquals(0, viaje.propina)
    }

    @Test
    fun `dosViajes con mismos datos tienen ingresoTotal igual`() {
        val viaje1 = ViajeEntity(fechaHora = 1000L, valor = 20000, propina = 5000)
        val viaje2 = ViajeEntity(fechaHora = 2000L, valor = 20000, propina = 5000)
        assertEquals(viaje1.ingresoTotal, viaje2.ingresoTotal)
    }

    @Test
    fun `los datos de plataforma vienen vacios por defecto`() {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = 10000)

        assertEquals("", viaje.plataforma)
        assertEquals("", viaje.zona)
        assertEquals(0L, viaje.distanciaMetros)
        assertEquals("", viaje.formaPago)
        assertEquals(0L, viaje.peaje)
        assertNull("Un viaje sin forma de pago no tiene tipo", viaje.formaPagoTipo)
    }

    @Test
    fun `el peaje no forma parte del ingreso total`() {
        val viaje = ViajeEntity(
            fechaHora = 1000L,
            valor = 15000,
            propina = 3000,
            peaje = 12000
        )

        assertEquals(18000, viaje.ingresoTotal)
    }

    @Test
    fun `formaPagoTipo reconoce una forma valida del catalogo`() {
        val viaje = ViajeEntity(
            fechaHora = 1000L,
            valor = 15000,
            formaPago = FormaPago.TRANSFERENCIA.nombre
        )

        assertEquals(FormaPago.TRANSFERENCIA, viaje.formaPagoTipo)
    }

    @Test
    fun `formaPagoTipo es null con un valor desconocido`() {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = 15000, formaPago = "BITCOIN")

        assertNull(viaje.formaPagoTipo)
    }

    @Test
    fun `la distancia se guarda en metros y se muestra en kilometros`() {
        val viaje = ViajeEntity(fechaHora = 1000L, valor = 15000, distanciaMetros = 8500L)

        assertEquals(8L, viaje.distanciaKm)
        assertEquals(8500L, ViajeEntity.kmAMetros(8L) + 500L)
    }

    @Test
    fun `kmAMetros convierte kilometros a metros`() {
        assertEquals(0L, ViajeEntity.kmAMetros(0L))
        assertEquals(12000L, ViajeEntity.kmAMetros(12L))
    }
}
