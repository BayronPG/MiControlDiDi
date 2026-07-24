package com.jhon.micontroldidi.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.data.local.database.MiControlDatabase
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
}
