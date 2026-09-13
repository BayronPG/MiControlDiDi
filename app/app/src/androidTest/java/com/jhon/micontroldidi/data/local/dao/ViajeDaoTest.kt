package com.jhon.micontroldidi.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.data.local.database.MiControlDatabase
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViajeDaoTest {

    private lateinit var database: MiControlDatabase
    private lateinit var dao: ViajeDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MiControlDatabase::class.java
        ).build()
        dao = database.viajeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertarViajeValido_retornaIdPositivo() = runBlocking {
        val viaje = ViajeEntity(
            fechaHora = 1000L,
            valor = 15000,
            propina = 3000,
            observacion = "Viaje de prueba"
        )
        val id = dao.insertar(viaje)
        assertTrue("El id generado debe ser positivo", id > 0)
    }

    @Test
    fun insertarViajeYrecuperarlo_datosCoinciden() = runBlocking {
        val viaje = ViajeEntity(
            fechaHora = 1000L,
            valor = 20000,
            propina = 5000,
            observacion = "Centro"
        )
        val id = dao.insertar(viaje)
        val viajes = dao.obtenerTodos().first()
        assertEquals(1, viajes.size)
        val recuperado = viajes[0]
        assertEquals(id, recuperado.id)
        assertEquals(1000L, recuperado.fechaHora)
        assertEquals(20000L, recuperado.valor)
        assertEquals(5000L, recuperado.propina)
        assertEquals("Centro", recuperado.observacion)
        assertEquals(25000L, recuperado.ingresoTotal)
    }

    @Test
    fun obtenerTodos_ordenDescendente() = runBlocking {
        val viaje1 = ViajeEntity(fechaHora = 1000L, valor = 10000)
        val viaje2 = ViajeEntity(fechaHora = 3000L, valor = 20000)
        val viaje3 = ViajeEntity(fechaHora = 2000L, valor = 15000)

        dao.insertar(viaje1)
        dao.insertar(viaje2)
        dao.insertar(viaje3)

        val viajes = dao.obtenerTodos().first()
        assertEquals(3, viajes.size)
        // Debe estar ordenado DESC por fechaHora
        assertTrue("fechaHora[0] >= fechaHora[1]", viajes[0].fechaHora >= viajes[1].fechaHora)
        assertTrue("fechaHora[1] >= fechaHora[2]", viajes[1].fechaHora >= viajes[2].fechaHora)
    }

    @Test
    fun insertarMultiplesViajes_todosRecuperados() = runBlocking {
        val viajes = listOf(
            ViajeEntity(fechaHora = 1000L, valor = 10000),
            ViajeEntity(fechaHora = 2000L, valor = 20000),
            ViajeEntity(fechaHora = 3000L, valor = 30000)
        )
        viajes.forEach { dao.insertar(it) }

        val recuperados = dao.obtenerTodos().first()
        assertEquals(3, recuperados.size)
    }

    @Test
    fun obtenerIngresosPorRango_sinRegistros_retornaCero() = runBlocking {
        val total = dao.obtenerIngresosPorRango(0L, 9999L).first()
        assertEquals(0L, total)
    }

    @Test
    fun obtenerIngresosPorRango_sumaValorYpropina() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000, propina = 2000))
        dao.insertar(ViajeEntity(fechaHora = 2000L, valor = 20000, propina = 3000))

        val total = dao.obtenerIngresosPorRango(0L, 9999L).first()
        // 10000+2000 + 20000+3000 = 35000
        assertEquals(35000L, total)
    }

    @Test
    fun obtenerIngresosPorRango_inicioIncluido() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 5000))

        // inicioInclusivo = 1000 debe incluir el viaje
        val total = dao.obtenerIngresosPorRango(1000L, 9999L).first()
        assertEquals(5000L, total)
    }

    @Test
    fun obtenerIngresosPorRango_finExclusivo_excluye() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 5000))

        // finExclusivo = 1000 debe excluir el viaje (porque fechaHora < finExclusivo es falso)
        val total = dao.obtenerIngresosPorRango(0L, 1000L).first()
        assertEquals(0L, total)
    }

    @Test
    fun obtenerIngresosPorRango_fueraDeRango_excluido() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 5000))
        dao.insertar(ViajeEntity(fechaHora = 5000L, valor = 10000))

        // Solo los viajes en [2000, 6000)
        val total = dao.obtenerIngresosPorRango(2000L, 6000L).first()
        assertEquals(10000L, total)
    }

    @Test
    fun obtenerIngresosPorRango_seActualizaAlInsertar() = runBlocking {
        val flujo = dao.obtenerIngresosPorRango(0L, 9999L)

        val primeraRecibida = CompletableDeferred<Unit>()
        val emisiones = async {
            withTimeout(2000) {
                flujo
                    .onEach { if (!primeraRecibida.isCompleted) primeraRecibida.complete(Unit) }
                    .take(2)
                    .toList()
            }
        }

        primeraRecibida.await()

        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 15000))

        val resultado = emisiones.await()
        assertEquals(listOf(0L, 15000L), resultado)
    }

    @Test
    fun obtenerIngresosPorRango_ignoraRegistrosFueraDelRango() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 5000))   // dentro
        dao.insertar(ViajeEntity(fechaHora = 9999L, valor = 10000))   // fuera

        val total = dao.obtenerIngresosPorRango(0L, 5000L).first()
        assertEquals(5000L, total)
    }

    @Test
    fun obtenerPorRango_sinRegistros_retornaListaVacia() = runBlocking {
        val viajes = dao.obtenerPorRango(0L, 9999L).first()
        assertTrue("Debe retornar lista vacía", viajes.isEmpty())
    }

    @Test
    fun obtenerPorRango_registrosDentroDelRango_aparecen() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000, propina = 2000))
        dao.insertar(ViajeEntity(fechaHora = 2000L, valor = 20000))

        val viajes = dao.obtenerPorRango(0L, 9999L).first()
        assertEquals(2, viajes.size)
    }

    @Test
    fun obtenerPorRango_registrosFueraDelRango_excluidos() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000))  // dentro
        dao.insertar(ViajeEntity(fechaHora = 9999L, valor = 20000))  // fuera

        val viajes = dao.obtenerPorRango(0L, 5000L).first()
        assertEquals(1, viajes.size)
        assertEquals(10000L, viajes[0].valor)
    }

    @Test
    fun obtenerPorRango_inicioIncluido_incluyeViaje() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 5000))

        val viajes = dao.obtenerPorRango(1000L, 9999L).first()
        assertEquals(1, viajes.size)
    }

    @Test
    fun obtenerPorRango_finExclusivo_excluyeViaje() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 5000))

        val viajes = dao.obtenerPorRango(0L, 1000L).first()
        assertTrue("Debe excluir cuando fechaHora == finExclusivo", viajes.isEmpty())
    }

    @Test
    fun obtenerPorRango_ordenDescendente() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000))
        dao.insertar(ViajeEntity(fechaHora = 3000L, valor = 30000))
        dao.insertar(ViajeEntity(fechaHora = 2000L, valor = 20000))

        val viajes = dao.obtenerPorRango(0L, 9999L).first()
        assertEquals(3, viajes.size)
        assertTrue("fechaHora[0] >= fechaHora[1]", viajes[0].fechaHora >= viajes[1].fechaHora)
        assertTrue("fechaHora[1] >= fechaHora[2]", viajes[1].fechaHora >= viajes[2].fechaHora)
    }

    @Test
    fun obtenerPorRango_seActualizaAlInsertar() = runBlocking {
        val flujo = dao.obtenerPorRango(0L, 9999L)

        val primeraRecibida = CompletableDeferred<Unit>()
        val emisiones = async {
            withTimeout(2000) {
                flujo
                    .onEach { if (!primeraRecibida.isCompleted) primeraRecibida.complete(Unit) }
                    .take(2)
                    .toList()
            }
        }

        primeraRecibida.await()

        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 15000))

        val resultado = emisiones.await()
        assertEquals(2, resultado.size)
        assertEquals(0, resultado[0].size)         // vacío inicial
        assertEquals(1, resultado[1].size)         // 1 después de insertar
        assertEquals(15000L, resultado[1][0].valor)
    }

    @Test
    fun obtenerPorRango_insertarFueraDelRango_noActualiza() = runBlocking {
        val flujo = dao.obtenerPorRango(0L, 5000L)

        val primeraRecibida = CompletableDeferred<Unit>()
        val emisiones = async {
            withTimeout(2000) {
                flujo
                    .onEach { if (!primeraRecibida.isCompleted) primeraRecibida.complete(Unit) }
                    .take(2)
                    .toList()
            }
        }

        primeraRecibida.await()

        dao.insertar(ViajeEntity(fechaHora = 9999L, valor = 15000))  // fuera del rango

        val resultado = emisiones.await()
        // Solo debe recibir la emisión vacía inicial; el insert fuera del rango
        // puede o no emitir dependiendo de Room, pero el contenido debe seguir vacío
        assertEquals(0, resultado[0].size)
        if (resultado.size > 1) {
            assertEquals(0, resultado[1].size)  // sigue vacío
        }
    }

    // --- Incremento E: datos de plataforma ---

    @Test
    fun insertarViajeConDatosDePlataforma_losConserva() = runBlocking {
        val id = dao.insertar(
            ViajeEntity(
                fechaHora = 1000L,
                valor = 20000L,
                propina = 1000L,
                observacion = "Con plataforma",
                plataforma = "inDrive",
                zona = "Belén",
                distanciaMetros = 8500L,
                formaPago = "EFECTIVO",
                peaje = 12000L
            )
        )

        val viaje = dao.obtenerPorId(id)
        assertNotNull(viaje)
        assertEquals("inDrive", viaje!!.plataforma)
        assertEquals("Belén", viaje.zona)
        assertEquals(8500L, viaje.distanciaMetros)
        assertEquals("EFECTIVO", viaje.formaPago)
        assertEquals(12000L, viaje.peaje)
        assertEquals(8L, viaje.distanciaKm)
        assertEquals(21000L, viaje.ingresoTotal)
    }

    @Test
    fun insertarViajeSinDatosDePlataforma_usaValoresPorDefecto() = runBlocking {
        val id = dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 15000L))

        val viaje = dao.obtenerPorId(id)!!
        assertEquals("", viaje.plataforma)
        assertEquals("", viaje.zona)
        assertEquals(0L, viaje.distanciaMetros)
        assertEquals("", viaje.formaPago)
        assertEquals(0L, viaje.peaje)
    }

    @Test
    fun actualizarViaje_cambiaTambienLosDatosDePlataforma() = runBlocking {
        val id = dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 15000L))

        val filas = dao.actualizar(
            id = id,
            fechaHora = 1000L,
            valor = 18000L,
            propina = 2000L,
            observacion = "Editado",
            plataforma = "DiDi",
            zona = "Laureles",
            distanciaMetros = 6500L,
            formaPago = "TARJETA",
            peaje = 9000L
        )

        assertEquals(1, filas)
        val viaje = dao.obtenerPorId(id)!!
        assertEquals("DiDi", viaje.plataforma)
        assertEquals("Laureles", viaje.zona)
        assertEquals(6500L, viaje.distanciaMetros)
        assertEquals("TARJETA", viaje.formaPago)
        assertEquals(9000L, viaje.peaje)
        assertEquals(20000L, viaje.ingresoTotal)
    }

    @Test
    fun actualizarViajeMigrado_conCamposVacios_funciona() = runBlocking {
        val id = dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 15000L, propina = 1000L))

        val filas = dao.actualizar(
            id = id,
            fechaHora = 1000L,
            valor = 15000L,
            propina = 1000L,
            observacion = "Viaje migrado",
            plataforma = "",
            zona = "",
            distanciaMetros = 0L,
            formaPago = "",
            peaje = 0L
        )

        assertEquals(1, filas)
        val viaje = dao.obtenerPorId(id)!!
        assertEquals("", viaje.plataforma)
        assertEquals("", viaje.formaPago)
        assertEquals(16000L, viaje.ingresoTotal)
    }

    @Test
    fun elPeajeNoAlteraLosIngresosAgregados() = runBlocking {
        dao.insertar(
            ViajeEntity(
                fechaHora = 1000L,
                valor = 15000L,
                propina = 2000L,
                peaje = 12000L
            )
        )

        val ingresos = dao.obtenerIngresosPorRango(0L, 5000L).first()

        assertEquals("El peaje no debe sumarse a los ingresos", 17000L, ingresos)
    }

    // --- Incremento G: Distancia total por rango ---

    @Test
    fun obtenerDistanciaTotalPorRango_sinRegistros_retornaCero() = runBlocking {
        val total = dao.obtenerDistanciaTotalPorRango(0L, 9999L).first()
        assertEquals(0L, total)
    }

    @Test
    fun obtenerDistanciaTotalPorRango_sumaDistanciasEnRango() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000L, distanciaMetros = 3500L))
        dao.insertar(ViajeEntity(fechaHora = 2000L, valor = 12000L, distanciaMetros = 4200L))

        val total = dao.obtenerDistanciaTotalPorRango(0L, 5000L).first()
        assertEquals(7700L, total)
    }

    @Test
    fun obtenerDistanciaTotalPorRango_inicioIncluido() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000L, distanciaMetros = 5000L))

        val total = dao.obtenerDistanciaTotalPorRango(1000L, 5000L).first()
        assertEquals(5000L, total)
    }

    @Test
    fun obtenerDistanciaTotalPorRango_finExclusivo_excluye() = runBlocking {
        dao.insertar(ViajeEntity(fechaHora = 5000L, valor = 10000L, distanciaMetros = 5000L))

        val total = dao.obtenerDistanciaTotalPorRango(1000L, 5000L).first()
        assertEquals(0L, total)
    }

    @Test
    fun obtenerDistanciaTotalPorRango_viajesEnLimitesDelRango() = runBlocking {
        // En rango [1000, 5000)
        dao.insertar(ViajeEntity(fechaHora = 999L, valor = 10000L, distanciaMetros = 1000L))  // fuera (antes)
        dao.insertar(ViajeEntity(fechaHora = 1000L, valor = 10000L, distanciaMetros = 2000L)) // dentro (límite inferior)
        dao.insertar(ViajeEntity(fechaHora = 4999L, valor = 10000L, distanciaMetros = 3000L)) // dentro (límite superior - 1)
        dao.insertar(ViajeEntity(fechaHora = 5000L, valor = 10000L, distanciaMetros = 4000L)) // fuera (límite superior exclusivo)

        val total = dao.obtenerDistanciaTotalPorRango(1000L, 5000L).first()
        assertEquals(5000L, total)
    }
}
