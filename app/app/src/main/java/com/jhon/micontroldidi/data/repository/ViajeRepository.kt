package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.domain.FormaPago
import kotlinx.coroutines.flow.Flow

class ViajeRepository(private val viajeDao: ViajeDao) {

    fun obtenerTodos(): Flow<List<ViajeEntity>> = viajeDao.obtenerTodos()

    /**
     * Obtiene los viajes dentro del rango semiabierto [inicioInclusivo, finExclusivo)
     * ordenados por fecha descendente.
     */
    fun obtenerPorRango(inicioInclusivo: Long, finExclusivo: Long): Flow<List<ViajeEntity>> =
        viajeDao.obtenerPorRango(inicioInclusivo, finExclusivo)

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

    suspend fun obtenerPorId(id: Long): ViajeEntity? = viajeDao.obtenerPorId(id)

    suspend fun actualizar(viaje: ViajeEntity): Result<Unit> {
        return try {
            validarViaje(viaje)
            val filas = viajeDao.actualizar(
                viaje.id, viaje.fechaHora, viaje.valor,
                viaje.propina, viaje.observacion,
                viaje.plataforma, viaje.zona,
                viaje.distanciaMetros, viaje.formaPago, viaje.peaje
            )
            if (filas == 0) {
                Result.failure(NoSuchElementException("El viaje no existe"))
            } else {
                Result.success(Unit)
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    suspend fun eliminar(id: Long): Result<Unit> {
        return try {
            val filas = viajeDao.eliminar(id)
            if (filas == 0) {
                Result.failure(NoSuchElementException("El viaje no existe"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validarViaje(viaje: ViajeEntity) {
        require(viaje.valor > 0) { "El valor del viaje debe ser mayor que cero" }
        require(viaje.propina >= 0) { "La propina no puede ser negativa" }
        require(viaje.distanciaMetros >= 0) { "La distancia no puede ser negativa" }
        require(viaje.peaje >= 0) { "El peaje no puede ser negativo" }
        // Los viajes migrados no tienen forma de pago (cadena vacía): se acepta.
        require(viaje.formaPago.isEmpty() || FormaPago.fromNombre(viaje.formaPago) != null) {
            "La forma de pago no es válida"
        }
    }
}
