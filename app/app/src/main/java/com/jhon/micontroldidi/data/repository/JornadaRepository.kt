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
