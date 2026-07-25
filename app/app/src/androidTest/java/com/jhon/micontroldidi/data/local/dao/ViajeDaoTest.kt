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


}
