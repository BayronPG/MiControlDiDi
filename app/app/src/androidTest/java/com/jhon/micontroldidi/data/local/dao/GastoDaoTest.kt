package com.jhon.micontroldidi.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.data.local.database.MiControlDatabase
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GastoDaoTest {

    private lateinit var database: MiControlDatabase
    private lateinit var gastoDao: GastoDao
    private lateinit var categoriaDao: CategoriaGastoDao
    private var categoriaId = 0L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MiControlDatabase::class.java)
            .build()
        gastoDao = database.gastoDao()
        categoriaDao = database.categoriaGastoDao()
        runBlocking {
            categoriaId = categoriaDao.insertar(CategoriaGastoEntity(nombre = "Gasolina"))
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertarYrecuperarGasto() = runBlocking {
        val g = GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 15000)
        val id = gastoDao.insertar(g)
        assertTrue("El id debe ser positivo", id > 0)
    }

    @Test
    fun gastoConCategoriaCorrecta() = runBlocking {
        val id = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 20000)
        )
        val gastos = gastoDao.obtenerTodos().first()
        assertEquals(1, gastos.size)
        assertEquals(id, gastos[0].id)
        assertEquals("Gasolina", gastos[0].nombreCategoria)
        assertEquals(20000L, gastos[0].valor)
    }

    @Test
    fun gastosOrdenadosDescendente() = runBlocking {
        gastoDao.insertar(GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 10000))
        gastoDao.insertar(GastoEntity(fechaHora = 3000L, categoriaId = categoriaId, valor = 30000))
        gastoDao.insertar(GastoEntity(fechaHora = 2000L, categoriaId = categoriaId, valor = 20000))

        val gastos = gastoDao.obtenerTodos().first()
        assertEquals(3, gastos.size)
        assertTrue(
            "Orden descendente",
            gastos[0].fechaHora >= gastos[1].fechaHora &&
            gastos[1].fechaHora >= gastos[2].fechaHora
        )
    }

    @Test
    fun categoriaInexistente_lanzaExcepcion() {
        assertThrows(Exception::class.java) {
            runBlocking {
                gastoDao.insertar(
                    GastoEntity(fechaHora = 1000L, categoriaId = 9999, valor = 5000)
                )
            }
        }
    }

    @Test
    fun obtenerPorId_devuelveGastoConCategoria() = runBlocking {
        val id = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 12000, descripcion = "Test")
        )
        val resultado = gastoDao.obtenerPorId(id)
        assertEquals(id, resultado?.id)
        assertEquals("Gasolina", resultado?.nombreCategoria)
        assertEquals(12000L, resultado?.valor)
        assertEquals("Test", resultado?.descripcion)
    }

    @Test
    fun actualizarCategoria_cambiaNombreCategoria() = runBlocking {
        val id = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 5000)
        )
        val cat2Id = categoriaDao.insertar(CategoriaGastoEntity(nombre = "Lavado"))

        val filas = gastoDao.actualizar(id, 1000L, cat2Id, 5000, "")
        assertEquals(1, filas)

        val actualizado = gastoDao.obtenerPorId(id)
        assertEquals("Lavado", actualizado?.nombreCategoria)
    }

    @Test
    fun actualizarValor_modificaElValor() = runBlocking {
        val id = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 5000)
        )
        gastoDao.actualizar(id, 1000L, categoriaId, 9999, "")
        val actualizado = gastoDao.obtenerPorId(id)
        assertEquals(9999L, actualizado?.valor)
    }

    @Test
    fun actualizarDescripcion_modificaLaDescripcion() = runBlocking {
        val id = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 5000)
        )
        gastoDao.actualizar(id, 1000L, categoriaId, 5000, "Nueva desc")
        val actualizado = gastoDao.obtenerPorId(id)
        assertEquals("Nueva desc", actualizado?.descripcion)
    }

    @Test
    fun actualizarConservaIdYFechaHora() = runBlocking {
        val id = gastoDao.insertar(
            GastoEntity(fechaHora = 2000L, categoriaId = categoriaId, valor = 5000)
        )
        gastoDao.actualizar(id, 2000L, categoriaId, 8888, "Cambiado")
        val actualizado = gastoDao.obtenerPorId(id)
        assertEquals(id, actualizado?.id)
        assertEquals(2000L, actualizado?.fechaHora)
        assertEquals(8888L, actualizado?.valor)
        assertEquals("Cambiado", actualizado?.descripcion)
    }

    @Test
    fun eliminarGasto_loRemueve() = runBlocking {
        val id = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 5000)
        )
        val filas = gastoDao.eliminar(id)
        assertEquals(1, filas)
        val resultado = gastoDao.obtenerPorId(id)
        assertEquals(null, resultado)
    }

    @Test
    fun eliminarIdInexistente_retornaCero() = runBlocking {
        val filas = gastoDao.eliminar(99999L)
        assertEquals(0, filas)
    }

    @Test
    fun noAfectaOtrosGastos_alActualizar() = runBlocking {
        val id1 = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 5000)
        )
        val id2 = gastoDao.insertar(
            GastoEntity(fechaHora = 2000L, categoriaId = categoriaId, valor = 7000)
        )
        gastoDao.actualizar(id1, 1000L, categoriaId, 9999, "X")

        val g2 = gastoDao.obtenerPorId(id2)
        assertEquals(7000L, g2?.valor)
        assertEquals("", g2?.descripcion)
    }

    @Test
    fun noAfectaOtrosGastos_alEliminar() = runBlocking {
        val id1 = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 5000)
        )
        val id2 = gastoDao.insertar(
            GastoEntity(fechaHora = 2000L, categoriaId = categoriaId, valor = 7000)
        )
        gastoDao.eliminar(id1)

        val g2 = gastoDao.obtenerPorId(id2)
        assertTrue(g2 != null)
        assertEquals(7000L, g2?.valor)
    }

    @Test
    fun eliminacionBienAislada_porGastoId() = runBlocking {
        val id1 = gastoDao.insertar(
            GastoEntity(fechaHora = 1000L, categoriaId = categoriaId, valor = 5000)
        )
        val id2 = gastoDao.insertar(
            GastoEntity(fechaHora = 2000L, categoriaId = categoriaId, valor = 7000)
        )
        gastoDao.eliminar(id1)
        val g2 = gastoDao.obtenerPorId(id2)
        assertTrue(g2 != null)
        assertEquals(7000L, g2?.valor)
    }
}
