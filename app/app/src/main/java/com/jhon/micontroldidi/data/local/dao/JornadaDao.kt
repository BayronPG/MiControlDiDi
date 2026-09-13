package com.jhon.micontroldidi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JornadaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(jornada: JornadaEntity): Long

    @Query("SELECT * FROM jornadas ORDER BY fechaHoraInicio DESC")
    fun observarTodas(): Flow<List<JornadaEntity>>

    @Query("SELECT * FROM jornadas ORDER BY fechaHoraInicio DESC LIMIT 1")
    fun observarUltima(): Flow<JornadaEntity?>

    @Query("SELECT * FROM jornadas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): JornadaEntity?

    /**
     * Cierra la jornada solo si sigue abierta (`fechaHoraFin = 0`).
     * Devuelve 0 si ya estaba cerrada, lo que impide cerrarla dos veces.
     */
    @Query("""
        UPDATE jornadas SET fechaHoraFin = :fechaHoraFin,
        kilometrajeFinalMetros = :kilometrajeFinalMetros
        WHERE id = :id AND fechaHoraFin = 0
    """)
    suspend fun cerrar(id: Long, fechaHoraFin: Long, kilometrajeFinalMetros: Long): Int
}
