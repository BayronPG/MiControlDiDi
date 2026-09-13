package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.dao.TanqueoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TanqueoRepositoryTest {

    private var ultimoGastoCreado: GastoEntity? = null
    private var ultimoTanqueoCreado: TanqueoEntity? = null
    private var tanqueoActualizado: TanqueoEntity? = null
    private var eliminarLlamadas = 0
    private var idGasolina: Long? = 1L

    private val tanqueoDaoFalso = object : TanqueoDao() {
        override suspend fun insertarGasto(gasto: GastoEntity): Long {
            ultimoGastoCreado = gasto
            return 7L
        }

        override suspend fun actualizarGasto(
            id: Long,
            fechaHora: Long,
            categoriaId: Long,
            valor: Long,
            descripcion: String
        ): Int = 1

        override suspend fun eliminarGasto(id: Long): Int = 1

        override suspend fun insertarTanqueo(tanqueo: TanqueoEntity): Long {
            ultimoTanqueoCreado = tanqueo
            return 5L
        }

        override suspend fun actualizarTanqueo(tanqueo: TanqueoEntity): Int {
            tanqueoActualizado = tanqueo
            return 1
        }

        override suspend fun eliminarTanqueo(id: Long): Int {
            eliminarLlamadas++
            return 1
        }

        override fun observarTodos(): Flow<List<TanqueoEntity>> = flowOf(emptyList())

        override fun observarUltimo(): Flow<TanqueoEntity?> = flowOf(null)

        override suspend fun obtenerPorId(id: Long): TanqueoEntity? = TanqueoEntity(
            id = id,
            fechaHora = 5000L,
            odometroMetros = 10_045_000L,
            litrosMililitros = 4000L,
            importePagado = 60000L,
            tipoCombustible = "Extra",
            gastoId = 7L
        )
    }

    private val categoriaDaoFalso = object : CategoriaGastoDao {
        override suspend fun insertar(categoria: CategoriaGastoEntity): Long = 1L

        override suspend fun insertarLista(categorias: List<CategoriaGastoEntity>): List<Long> =
            emptyList()

        override fun obtenerActivas(): Flow<List<CategoriaGastoEntity>> = flowOf(emptyList())

        override suspend fun obtenerPorId(id: Long): CategoriaGastoEntity? = null

        override suspend fun existePorNombre(nombre: String): Boolean = true

        override suspend fun obtenerIdGasolina(): Long? = idGasolina
    }

    private fun repositorio() = TanqueoRepository(tanqueoDaoFalso, categoriaDaoFalso)

    private fun tanqueoValido() = TanqueoEntity(
        fechaHora = 5000L,
        odometroMetros = 10_045_000L,
        litrosMililitros = 4000L,
        importePagado = 60000L,
        esLleno = true,
        tipoCombustible = "Extra",
        observacion = "Tanqueo lleno"
    )

    @Test
    fun `crear un tanqueo valido crea su gasto de gasolina`() = runTest {
        val resultado = repositorio().crear(tanqueoValido())

        assertTrue("El tanqueo válido debe crearse", resultado.isSuccess)
        val gasto = ultimoGastoCreado!!
        assertEquals("El gasto debe valer exactamente el importe pagado", 60000L, gasto.valor)
        assertEquals(1L, gasto.categoriaId)
        assertEquals("Tanqueo lleno", gasto.descripcion)
        assertEquals("El tanqueo queda enlazado a su gasto", 7L, ultimoTanqueoCreado!!.gastoId)
    }

    @Test
    fun `no se puede crear un tanqueo sin litros`() = runTest {
        val resultado = repositorio().crear(tanqueoValido().copy(litrosMililitros = 0L))

        assertTrue("Los litros deben ser mayores que cero", resultado.isFailure)
    }

    @Test
    fun `no se puede crear un tanqueo sin importe`() = runTest {
        val resultado = repositorio().crear(tanqueoValido().copy(importePagado = 0L))

        assertTrue("El importe debe ser mayor que cero", resultado.isFailure)
    }

    @Test
    fun `no se puede crear un tanqueo con odometro negativo`() = runTest {
        val resultado = repositorio().crear(tanqueoValido().copy(odometroMetros = -1L))

        assertTrue("El odómetro no puede ser negativo", resultado.isFailure)
    }

    @Test
    fun `no se puede crear un tanqueo sin tipo de combustible`() = runTest {
        val resultado = repositorio().crear(tanqueoValido().copy(tipoCombustible = " "))

        assertTrue("El tipo de combustible es obligatorio", resultado.isFailure)
    }

    @Test
    fun `sin categoria Gasolina el tanqueo no se crea`() = runTest {
        idGasolina = null

        val resultado = repositorio().crear(tanqueoValido())

        assertTrue("Sin categoría Gasolina no debe crearse", resultado.isFailure)
        assertEquals(0L, ultimoTanqueoCreado?.id ?: 0L)
    }

    @Test
    fun `actualizar exige un tanqueo existente con gasto enlazado`() = runTest {
        val sinId = repositorio().actualizar(tanqueoValido())
        assertTrue("Sin id no se actualiza", sinId.isFailure)

        val sinGasto = repositorio().actualizar(tanqueoValido().copy(id = 3L, gastoId = 0L))
        assertTrue("Sin gasto enlazado no se actualiza", sinGasto.isFailure)

        val correcto = repositorio().actualizar(tanqueoValido().copy(id = 3L, gastoId = 7L))
        assertTrue("Con id y gasto se actualiza", correcto.isSuccess)
        assertEquals(3L, tanqueoActualizado?.id)
    }

    @Test
    fun `eliminar un tanqueo delega en el dao`() = runTest {
        val resultado = repositorio().eliminar(3L)

        assertTrue("El tanqueo existente se elimina", resultado.isSuccess)
        assertEquals(1, eliminarLlamadas)
    }
}
