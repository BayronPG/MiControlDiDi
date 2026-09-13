package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.dao.TanqueoDao
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de tanqueos.
 *
 * Cada tanqueo mantiene sincronizado su gasto de categoría Gasolina: crearlo
 * crea el gasto, editarlo lo actualiza y eliminarlo lo elimina. El gasto vive
 * una sola vez en los gastos, así que el combustible no se cuenta dos veces.
 */
class TanqueoRepository(
    private val tanqueoDao: TanqueoDao,
    private val categoriaGastoDao: CategoriaGastoDao
) {

    fun observarTodos(): Flow<List<TanqueoEntity>> = tanqueoDao.observarTodos()

    fun observarUltimo(): Flow<TanqueoEntity?> = tanqueoDao.observarUltimo()

    suspend fun obtenerPorId(id: Long): TanqueoEntity? = tanqueoDao.obtenerPorId(id)

    suspend fun crear(tanqueo: TanqueoEntity): Result<Long> {
        return try {
            validarTanqueo(tanqueo)
            val categoriaId = resolverCategoriaGasolina()
            Result.success(tanqueoDao.crear(tanqueo, gastoDe(tanqueo, categoriaId)))
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    suspend fun actualizar(tanqueo: TanqueoEntity): Result<Unit> {
        return try {
            validarTanqueo(tanqueo)
            require(tanqueo.id > 0) { "El tanqueo debe existir" }
            require(tanqueo.gastoId > 0) { "El tanqueo debe tener un gasto vinculado" }
            val categoriaId = resolverCategoriaGasolina()
            val filas = tanqueoDao.actualizar(tanqueo, categoriaId)
            if (filas == 0) {
                Result.failure(NoSuchElementException("El tanqueo no existe"))
            } else {
                Result.success(Unit)
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    suspend fun eliminar(id: Long): Result<Unit> {
        return try {
            val filas = tanqueoDao.eliminar(id)
            if (filas == 0) {
                Result.failure(NoSuchElementException("El tanqueo no existe"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolverCategoriaGasolina(): Long =
        categoriaGastoDao.obtenerIdGasolina()
            ?: throw IllegalArgumentException("No existe la categoría Gasolina")

    private fun gastoDe(tanqueo: TanqueoEntity, categoriaId: Long) = GastoEntity(
        fechaHora = tanqueo.fechaHora,
        categoriaId = categoriaId,
        valor = tanqueo.importePagado,
        descripcion = tanqueo.observacion
    )

    private fun validarTanqueo(tanqueo: TanqueoEntity) {
        require(tanqueo.litrosMililitros > 0) { "Los litros deben ser mayores que cero" }
        require(tanqueo.importePagado > 0) { "El importe pagado debe ser mayor que cero" }
        require(tanqueo.odometroMetros >= 0) { "El odómetro no puede ser negativo" }
        require(tanqueo.tipoCombustible.isNotBlank()) { "El tipo de combustible es obligatorio" }
    }
}
