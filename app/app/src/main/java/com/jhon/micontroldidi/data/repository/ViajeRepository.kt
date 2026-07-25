package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import kotlinx.coroutines.flow.Flow

class ViajeRepository(private val viajeDao: ViajeDao) {

    fun obtenerTodos(): Flow<List<ViajeEntity>> = viajeDao.obtenerTodos()

    /**
     * Obtiene la suma de ingresos (valor + propina) en el rango semiabierto
     * [inicioInclusivo, finExclusivo) mediante consulta agregada en SQLite.
     */
    fun obtenerIngresosPorRango(inicioInclusivo: Long, finExclusivo: Long): Flow<Long> =
        viajeDao.obtenerIngresosPorRango(inicioInclusivo, finExclusivo)

    /**
     * Inserta un viaje validando las reglas de negocio.
     * Retorna [Result.success] con el id generado si los datos son válidos,
     * o [Result.failure] con la excepción correspondiente.
     */
    suspend fun insertar(viaje: ViajeEntity): Result<Long> {
        return try {
            validarViaje(viaje)
            val id = viajeDao.insertar(viaje)
            Result.success(id)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    private fun validarViaje(viaje: ViajeEntity) {
        require(viaje.valor > 0) { "El valor del viaje debe ser mayor que cero" }
        require(viaje.propina >= 0) { "La propina no puede ser negativa" }
    }
}
