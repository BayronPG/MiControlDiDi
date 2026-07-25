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
        SELECT COALESCE(SUM(valor + propina), 0)
        FROM viajes
        WHERE fechaHora >= :inicioInclusivo AND fechaHora < :finExclusivo
    """)
    fun obtenerIngresosPorRango(inicioInclusivo: Long, finExclusivo: Long): Flow<Long>
}
