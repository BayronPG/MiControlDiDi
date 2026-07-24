package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import kotlinx.coroutines.flow.Flow

class CategoriaGastoRepository(
    private val categoriaGastoDao: CategoriaGastoDao
) {

    fun obtenerActivas(): Flow<List<CategoriaGastoEntity>> =
        categoriaGastoDao.obtenerActivas()

    suspend fun obtenerPorId(id: Long): CategoriaGastoEntity? =
        categoriaGastoDao.obtenerPorId(id)

    suspend fun existePorNombre(nombre: String): Boolean =
        categoriaGastoDao.existePorNombre(nombre)

    suspend fun insertar(categoria: CategoriaGastoEntity): Result<Long> {
        return try {
            val nombreNormalizado = categoria.nombre.trim()
            val categoriaNormalizada = categoria.copy(nombre = nombreNormalizado)
            validarCategoria(categoriaNormalizada)
            if (categoriaGastoDao.existePorNombre(nombreNormalizado)) {
                return Result.failure(
                    IllegalArgumentException("Ya existe una categoría con el nombre '$nombreNormalizado'")
                )
            }
            val id = categoriaGastoDao.insertar(categoriaNormalizada)
            // Con OnConflictStrategy.IGNORE, retorna -1 si SQLite ignoró el duplicado
            if (id == -1L) {
                return Result.failure(
                    IllegalArgumentException("Ya existe una categoría con el nombre '$nombreNormalizado'")
                )
            }
            Result.success(id)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    private fun validarCategoria(categoria: CategoriaGastoEntity) {
        require(categoria.nombre.isNotBlank()) {
            "El nombre de la categoría no puede estar vacío"
        }
    }

    /**
     * Normaliza el nombre: sin espacios al inicio/final.
     */
    fun normalizarNombre(nombre: String): String = nombre.trim()
}
