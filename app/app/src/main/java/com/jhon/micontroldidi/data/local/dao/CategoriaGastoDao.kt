package com.jhon.micontroldidi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaGastoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(categoria: CategoriaGastoEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarLista(categorias: List<CategoriaGastoEntity>): List<Long>

    @Query("SELECT * FROM categorias_gasto WHERE activa = 1 ORDER BY nombre ASC")
    fun obtenerActivas(): Flow<List<CategoriaGastoEntity>>

    @Query("SELECT * FROM categorias_gasto WHERE id = :id")
    suspend fun obtenerPorId(id: Long): CategoriaGastoEntity?

    @Query("SELECT COUNT(*) > 0 FROM categorias_gasto WHERE LOWER(nombre) = LOWER(:nombre)")
    suspend fun existePorNombre(nombre: String): Boolean

    /** Identificador de la categoría Gasolina, que ya viene sembrada. */
    @Query("SELECT id FROM categorias_gasto WHERE LOWER(nombre) = 'gasolina' LIMIT 1")
    suspend fun obtenerIdGasolina(): Long?
}
