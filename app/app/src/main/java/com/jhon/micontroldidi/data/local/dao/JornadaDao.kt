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
}
