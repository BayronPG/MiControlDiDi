package com.jhon.micontroldidi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ViajeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(viaje: ViajeEntity): Long

    @Query("SELECT * FROM viajes ORDER BY fechaHora DESC")
    fun obtenerTodos(): Flow<List<ViajeEntity>>

    @Query("""
        SELECT * FROM viajes
        WHERE fechaHora >= :inicioInclusivo AND fechaHora < :finExclusivo
        ORDER BY fechaHora DESC
    """)
    fun obtenerPorRango(inicioInclusivo: Long, finExclusivo: Long): Flow<List<ViajeEntity>>

    @Query("""
        SELECT COALESCE(SUM(valor + propina), 0)
        FROM viajes
        WHERE fechaHora >= :inicioInclusivo AND fechaHora < :finExclusivo
    """)
    fun obtenerIngresosPorRango(inicioInclusivo: Long, finExclusivo: Long): Flow<Long>

    @Query("SELECT * FROM viajes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ViajeEntity?

    @Query("""
        UPDATE viajes SET fechaHora = :fechaHora, valor = :valor,
        propina = :propina, observacion = :observacion WHERE id = :id
    """)
    suspend fun actualizar(id: Long, fechaHora: Long, valor: Long, propina: Long, observacion: String): Int

    @Query("DELETE FROM viajes WHERE id = :id")
    suspend fun eliminar(id: Long): Int
}
