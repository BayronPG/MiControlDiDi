package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoriaGastoRepositoryTest {

    private var insertarLlamadas = 0
    private var existeNombre = false

    private val daoFalso = object : CategoriaGastoDao {
        override suspend fun obtenerIdGasolina(): Long? = 1L

        override suspend fun insertar(categoria: CategoriaGastoEntity): Long {
            insertarLlamadas++
            return 1L
        }
        override suspend fun insertarLista(categorias: List<CategoriaGastoEntity>): List<Long> {
            return categorias.map { 1L }
        }
        override fun obtenerActivas(): Flow<List<CategoriaGastoEntity>> = flowOf(emptyList())
        override suspend fun obtenerPorId(id: Long): CategoriaGastoEntity? = null
        override suspend fun existePorNombre(nombre: String): Boolean = existeNombre
    }

    private lateinit var repository: CategoriaGastoRepository

    @Before
    fun setUp() {
        insertarLlamadas = 0
        existeNombre = false
        repository = CategoriaGastoRepository(daoFalso)
    }

    @Test
    fun `categoria valida llama una vez al DAO`() = runTest {
        val c = CategoriaGastoEntity(nombre = "Gasolina")
        val resultado = repository.insertar(c)
        assertTrue(resultado.isSuccess)
        assertEquals(1, insertarLlamadas)
    }

    @Test
    fun `categoria con nombre vacio es rechazada`() = runTest {
        val c = CategoriaGastoEntity(nombre = "")
        val resultado = repository.insertar(c)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("vacío") == true)
    }

    @Test
    fun `categoria con solo espacios es rechazada`() = runTest {
        val c = CategoriaGastoEntity(nombre = "   ")
        val resultado = repository.insertar(c)
        assertFalse(resultado.isSuccess)
    }

    @Test
    fun `categoria duplicada es rechazada`() = runTest {
        existeNombre = true
        val c = CategoriaGastoEntity(nombre = "Gasolina")
        val resultado = repository.insertar(c)
        assertFalse(resultado.isSuccess)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("Ya existe") == true)
    }

    @Test
    fun `descripcion vacia es valida`() = runTest {
        val c = CategoriaGastoEntity(nombre = "Parqueadero")
        val resultado = repository.insertar(c)
        assertTrue(resultado.isSuccess)
    }
}
