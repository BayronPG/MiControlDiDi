package com.jhon.micontroldidi.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.data.local.database.MiControlDatabase
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.repository.CategoriaGastoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoriaUnicidadTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = Room.inMemoryDatabaseBuilder(context, MiControlDatabase::class.java)
        .addCallback(MiControlDatabase.obtenerCallbackPrepoblar())
        .build()
    private val dao = database.categoriaGastoDao()
    private val repository = CategoriaGastoRepository(dao)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun dao_conflictoRetornaMenosUno_ySoloUnaFila() = runBlocking {
        // "Gasolina" ya existe por el callback (6 categorías iniciales)
        val id1 = dao.insertar(CategoriaGastoEntity(nombre = "Gasolina"))
        val id2 = dao.insertar(CategoriaGastoEntity(nombre = "gasolina"))
        val id3 = dao.insertar(CategoriaGastoEntity(nombre = "GASOLINA"))

        // OnConflictStrategy.IGNORE retorna -1 cuando SQLite ignora el conflicto
        assertEquals("Gasolina repetida → ignorada (-1)", -1L, id1)
        assertEquals("gasolina → COLLATE NOCASE → ignorada (-1)", -1L, id2)
        assertEquals("GASOLINA → COLLATE NOCASE → ignorada (-1)", -1L, id3)

        val total = dao.obtenerActivas().first().size
        assertEquals("Solo 6 iniciales, 0 nuevas", 6, total)
    }

    @Test
    fun dao_nuevoNombre_distintasMayusculas_soloUnaFila() = runBlocking {
        val id1 = dao.insertar(CategoriaGastoEntity(nombre = "NuevaCat"))
        val id2 = dao.insertar(CategoriaGastoEntity(nombre = "nuevacat"))
        val id3 = dao.insertar(CategoriaGastoEntity(nombre = "NUEVACAT"))

        assertTrue("Primera insertada", id1 > 0)
        assertEquals("Segunda ignorada", -1L, id2)
        assertEquals("Tercera ignorada", -1L, id3)

        val total = dao.obtenerActivas().first().size
        assertEquals("6 iniciales + 1 nueva = 7", 7, total)
    }

    @Test
    fun repositorio_rechazaDuplicados_yDevuelveFailure() = runBlocking {
        val r1 = repository.insertar(CategoriaGastoEntity(nombre = "Otros"))
        val r2 = repository.insertar(CategoriaGastoEntity(nombre = "otros"))
        val r3 = repository.insertar(CategoriaGastoEntity(nombre = "OTROS"))

        assertFalse("Otros ya existe → failure", r1.isSuccess)
        assertFalse("otros → failure", r2.isSuccess)
        assertFalse("OTROS → failure", r3.isSuccess)

        val total = dao.obtenerActivas().first().size
        assertEquals("Solo 6 iniciales", 6, total)
    }
}
