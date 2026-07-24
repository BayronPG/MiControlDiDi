package com.jhon.micontroldidi.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.data.local.database.MiControlDatabase
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
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
class CategoriaGastoDaoTest {

    private lateinit var database: MiControlDatabase
    private lateinit var dao: CategoriaGastoDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MiControlDatabase::class.java)
            .build()
        dao = database.categoriaGastoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertarYrecuperarCategoria() = runBlocking {
        val id = dao.insertar(CategoriaGastoEntity(nombre = "Gasolina"))
        val recuperada = dao.obtenerPorId(id)
        assertNotNull(recuperada)
        assertEquals("Gasolina", recuperada?.nombre)
    }

    @Test
    fun categoriasActivas_ordenadasPorNombre() = runBlocking {
        dao.insertarLista(listOf(
            CategoriaGastoEntity(nombre = "Zeta"),
            CategoriaGastoEntity(nombre = "Alfa"),
            CategoriaGastoEntity(nombre = "Beta")
        ))
        val activas = dao.obtenerActivas().first()
        assertEquals(3, activas.size)
        assertEquals("Alfa", activas[0].nombre)
        assertEquals("Beta", activas[1].nombre)
        assertEquals("Zeta", activas[2].nombre)
    }

    @Test
    fun existePorNombre_insensibleAMayusculas() = runBlocking {
        dao.insertar(CategoriaGastoEntity(nombre = "Gasolina"))
        assertTrue(dao.existePorNombre("gasolina"))
        assertTrue(dao.existePorNombre("GASOLINA"))
        assertTrue(dao.existePorNombre("Gasolina"))
    }
}
