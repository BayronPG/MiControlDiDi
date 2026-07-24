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
        SELECT g.id, g.fechaHora, g.categoriaId, c.nombre AS nombreCategoria,
               g.valor, g.descripcion
        FROM gastos g
        INNER JOIN categorias_gasto c ON g.categoriaId = c.id
        ORDER BY g.fechaHora DESC
    """)
    fun obtenerTodos(): Flow<List<GastoConCategoria>>
}
