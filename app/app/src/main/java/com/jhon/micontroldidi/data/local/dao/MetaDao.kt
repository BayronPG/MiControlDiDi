package com.jhon.micontroldidi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(meta: MetaEntity): Long

    @Query("SELECT * FROM metas WHERE activa = 1 LIMIT 1")
    fun obtenerActiva(): Flow<MetaEntity?>

    @Query("SELECT * FROM metas ORDER BY createdAt DESC LIMIT 1")
    suspend fun obtenerUltima(): MetaEntity?

    @Query("UPDATE metas SET activa = 0 WHERE activa = 1")
    suspend fun desactivarTodas()

    @Query("""
        UPDATE metas SET valorObjetivo = :valorObjetivo, tipoPeriodo = :tipoPeriodo
        WHERE id = :id
    """)
    suspend fun actualizar(id: Long, tipoPeriodo: String, valorObjetivo: Long): Int

    @Query("DELETE FROM metas WHERE id = :id")
    suspend fun eliminar(id: Long): Int
}
