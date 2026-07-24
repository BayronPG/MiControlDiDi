package com.jhon.micontroldidi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(gasto: GastoEntity): Long

    @Query("""
        UPDATE gastos SET fechaHora = :fechaHora, categoriaId = :categoriaId,
        valor = :valor, descripcion = :descripcion WHERE id = :id
    """)
    suspend fun actualizar(id: Long, fechaHora: Long, categoriaId: Long, valor: Long, descripcion: String): Int

    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun eliminar(id: Long): Int

    @Query("""
        SELECT g.id, g.fechaHora, g.categoriaId, c.nombre AS nombreCategoria,
               g.valor, g.descripcion
        FROM gastos g
        INNER JOIN categorias_gasto c ON g.categoriaId = c.id
        ORDER BY g.fechaHora DESC
    """)
    fun obtenerTodos(): Flow<List<GastoConCategoria>>

    @Query("""
        SELECT g.id, g.fechaHora, g.categoriaId, c.nombre AS nombreCategoria,
               g.valor, g.descripcion
        FROM gastos g
        INNER JOIN categorias_gasto c ON g.categoriaId = c.id
        WHERE g.id = :id
    """)
    suspend fun obtenerPorId(id: Long): GastoConCategoria?
}
