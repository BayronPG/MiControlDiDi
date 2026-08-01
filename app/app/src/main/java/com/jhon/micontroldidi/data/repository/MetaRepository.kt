package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.MetaDao
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import com.jhon.micontroldidi.domain.PeriodoMeta
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
    suspend fun guardar(periodo: PeriodoMeta, valorObjetivo: Long): Result<Long> {
        return try {
            validar(valorObjetivo)
            metaDao.desactivarTodas()
            val id = metaDao.insertar(
                MetaEntity(
                    tipoPeriodo = periodo.nombre,
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
    suspend fun actualizar(id: Long, periodo: PeriodoMeta, valorObjetivo: Long): Result<Unit> {
        return try {
            validar(valorObjetivo)
            val filas = metaDao.actualizar(id, periodo.nombre, valorObjetivo)
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

    private fun validar(valorObjetivo: Long) {
        require(valorObjetivo > 0) { "El valor objetivo debe ser mayor que cero" }
    }
}
