package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.JornadaDao
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de jornadas.
 *
 * La jornada se registra al iniciar el día; el cierre con el odómetro final
 * llega con el cálculo de kilómetros.
 */
class JornadaRepository(private val jornadaDao: JornadaDao) {

    fun observarTodas(): Flow<List<JornadaEntity>> = jornadaDao.observarTodas()

    fun observarUltima(): Flow<JornadaEntity?> = jornadaDao.observarUltima()

    /**
     * Registra una jornada validando las reglas de negocio.
     * Retorna [Result.success] con el id generado o [Result.failure].
     */
    suspend fun insertar(jornada: JornadaEntity): Result<Long> {
        return try {
            validar(jornada)
            Result.success(jornadaDao.insertar(jornada))
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra la jornada con la hora de fin y el odómetro final.
     *
     * Rechaza cerrar una jornada inexistente o ya cerrada, una hora de fin
     * anterior al inicio y un odómetro final menor que el inicial.
     */
    suspend fun cerrar(id: Long, fechaHoraFin: Long, kilometrajeFinalMetros: Long): Result<Unit> {
        return try {
            val jornada = jornadaDao.obtenerPorId(id)
                ?: return Result.failure(NoSuchElementException("La jornada no existe"))
            require(fechaHoraFin >= jornada.fechaHoraInicio) {
                "La hora de fin no puede ser anterior al inicio"
            }
            require(kilometrajeFinalMetros >= jornada.kilometrajeInicialMetros) {
                "El odómetro final no puede ser menor que el inicial"
            }
            val filas = jornadaDao.cerrar(id, fechaHoraFin, kilometrajeFinalMetros)
            if (filas == 0) {
                Result.failure(IllegalStateException("La jornada ya está cerrada"))
            } else {
                Result.success(Unit)
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    private fun validar(jornada: JornadaEntity) {
        require(jornada.kilometrajeInicialMetros >= 0) {
            "El kilometraje inicial no puede ser negativo"
        }
        require(jornada.precioGalonExtra > 0) {
            "El precio del galón debe ser mayor que cero"
        }
        require(jornada.metaBrutaDia > 0) {
            "La meta bruta del día debe ser mayor que cero"
        }
        require(jornada.nivelEnergia in 0..10) {
            "El nivel de energía debe estar entre 0 y 10"
        }
        require(jornada.plataforma.isNotBlank()) {
            "La plataforma es obligatoria"
        }
    }
}
