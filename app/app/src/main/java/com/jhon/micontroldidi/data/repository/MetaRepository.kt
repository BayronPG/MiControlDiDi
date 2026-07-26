package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.MetaDao
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import kotlinx.coroutines.flow.Flow

class MetaRepository(private val metaDao: MetaDao) {

    /**
     * Obtiene la meta activa como Flow reactivo.
     */
    fun obtenerActiva(): Flow<MetaEntity?> = metaDao.obtenerActiva()

    /**
     * Guarda una nueva meta, desactivando cualquier meta activa anterior.
     * Solo puede existir una meta activa a la vez.
     */
    suspend fun guardar(tipoPeriodo: String, valorObjetivo: Long): Result<Long> {
        return try {
            validar(tipoPeriodo, valorObjetivo)
            metaDao.desactivarTodas()
            val id = metaDao.insertar(
                MetaEntity(
                    tipoPeriodo = tipoPeriodo,
                    valorObjetivo = valorObjetivo,
                    activa = true
                )
            )
            Result.success(id)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la meta existente con nuevos valores.
     */
    suspend fun actualizar(id: Long, tipoPeriodo: String, valorObjetivo: Long): Result<Unit> {
        return try {
            validar(tipoPeriodo, valorObjetivo)
            val filas = metaDao.actualizar(id, tipoPeriodo, valorObjetivo)
            if (filas == 0) {
                Result.failure(NoSuchElementException("La meta no existe"))
            } else {
                Result.success(Unit)
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una meta por su ID.
     */
    suspend fun eliminar(id: Long): Result<Unit> {
        return try {
            val filas = metaDao.eliminar(id)
            if (filas == 0) {
                Result.failure(NoSuchElementException("La meta no existe"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validar(tipoPeriodo: String, valorObjetivo: Long) {
        require(tipoPeriodo == "DIA" || tipoPeriodo == "MES") {
            "El periodo debe ser 'DIA' o 'MES'"
        }
        require(valorObjetivo > 0) { "El valor objetivo debe ser mayor que cero" }
    }
}
