package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.GastoDao
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.Flow

class GastoRepository(
    private val gastoDao: GastoDao
) {

    fun obtenerTodos(): Flow<List<GastoConCategoria>> = gastoDao.obtenerTodos()

    suspend fun insertar(gasto: GastoEntity): Result<Long> {
        return try {
            validarGasto(gasto)
            val id = gastoDao.insertar(gasto)
            Result.success(id)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    private fun validarGasto(gasto: GastoEntity) {
        require(gasto.valor > 0) { "El valor del gasto debe ser mayor que cero" }
        require(gasto.categoriaId > 0) { "La categoría es obligatoria" }
    }
}
