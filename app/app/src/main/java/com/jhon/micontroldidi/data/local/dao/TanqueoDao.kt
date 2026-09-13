package com.jhon.micontroldidi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO coordinador de tanqueos y su gasto de gasolina.
 *
 * Cada operación de escritura ocurre dentro de una transacción de Room, de modo
 * que el tanqueo y su gasto nunca queden desincronizados. El repositorio no
 * necesita acceso a `RoomDatabase`.
 */
@Dao
abstract class TanqueoDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    abstract suspend fun insertarGasto(gasto: GastoEntity): Long

    @Query("""
        UPDATE gastos SET fechaHora = :fechaHora, categoriaId = :categoriaId,
        valor = :valor, descripcion = :descripcion WHERE id = :id
    """)
    abstract suspend fun actualizarGasto(
        id: Long,
        fechaHora: Long,
        categoriaId: Long,
        valor: Long,
        descripcion: String
    ): Int

    @Query("DELETE FROM gastos WHERE id = :id")
    abstract suspend fun eliminarGasto(id: Long): Int

    @Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    abstract suspend fun insertarTanqueo(tanqueo: TanqueoEntity): Long

    @Update
    abstract suspend fun actualizarTanqueo(tanqueo: TanqueoEntity): Int

    @Query("DELETE FROM tanqueos WHERE id = :id")
    abstract suspend fun eliminarTanqueo(id: Long): Int

    @Query("SELECT * FROM tanqueos ORDER BY fechaHora DESC")
    abstract fun observarTodos(): Flow<List<TanqueoEntity>>

    @Query("SELECT * FROM tanqueos ORDER BY fechaHora DESC LIMIT 1")
    abstract fun observarUltimo(): Flow<TanqueoEntity?>

    @Query("SELECT * FROM tanqueos WHERE id = :id")
    abstract suspend fun obtenerPorId(id: Long): TanqueoEntity?

    /** Crea el gasto y devuelve el tanqueo con su `gastoId` enlazado. */
    @Transaction
    open suspend fun crear(tanqueo: TanqueoEntity, gasto: GastoEntity): Long {
        val gastoId = insertarGasto(gasto)
        return insertarTanqueo(tanqueo.copy(gastoId = gastoId))
    }

    /** Actualiza el tanqueo y su gasto vinculado. */
    @Transaction
    open suspend fun actualizar(tanqueo: TanqueoEntity, categoriaId: Long): Int {
        val filasGasto = actualizarGasto(
            id = tanqueo.gastoId,
            fechaHora = tanqueo.fechaHora,
            categoriaId = categoriaId,
            valor = tanqueo.importePagado,
            descripcion = tanqueo.observacion
        )
        val filasTanqueo = actualizarTanqueo(tanqueo)
        return if (filasGasto > 0 && filasTanqueo > 0) 1 else 0
    }

    /** Elimina primero el tanqueo (hijo) y después su gasto (padre). */
    @Transaction
    open suspend fun eliminar(id: Long): Int {
        val tanqueo = obtenerPorId(id) ?: return 0
        val filasTanqueo = eliminarTanqueo(id)
        val filasGasto = eliminarGasto(tanqueo.gastoId)
        return if (filasTanqueo > 0 && filasGasto > 0) 1 else 0
    }
}
