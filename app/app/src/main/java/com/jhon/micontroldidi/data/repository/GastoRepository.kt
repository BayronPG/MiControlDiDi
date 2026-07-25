package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.GastoDao
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.Flow

class GastoRepository(
    private val gastoDao: GastoDao
) {

    fun obtenerTodos(): Flow<List<GastoConCategoria>> = gastoDao.obtenerTodos()

    /**
     * Obtiene la suma de gastos en el rango semiabierto
     * [inicioInclusivo, finExclusivo) mediante consulta agregada en SQLite.
     */
    fun obtenerTotalGastosPorRango(inicioInclusivo: Long, finExclusivo: Long): Flow<Long> =
        gastoDao.obtenerTotalGastosPorRango(inicioInclusivo, finExclusivo)

    suspend fun obtenerPorId(id: Long): GastoConCategoria? = gastoDao.obtenerPorId(id)

    suspend fun insertar(gasto: GastoEntity): Result<Long> {
        return try {
            validarGasto(gasto)
            val id = gastoDao.insertar(gasto)
            Result.success(id)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    suspend fun actualizar(gasto: GastoEntity): Result<Unit> {
        return try {
            validarGasto(gasto)
            val filas = gastoDao.actualizar(
                gasto.id, gasto.fechaHora, gasto.categoriaId,
                gasto.valor, gasto.descripcion
            )
            if (filas == 0) {
                Result.failure(NoSuchElementException("El gasto no existe"))
            } else {
                Result.success(Unit)
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    suspend fun eliminar(id: Long): Result<Unit> {
        return try {
            val filas = gastoDao.eliminar(id)
            if (filas == 0) {
                Result.failure(NoSuchElementException("El gasto no existe"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validarGasto(gasto: GastoEntity) {
        require(gasto.valor > 0) { "El valor del gasto debe ser mayor que cero" }
        require(gasto.categoriaId > 0) { "La categoría es obligatoria" }
    }
}
