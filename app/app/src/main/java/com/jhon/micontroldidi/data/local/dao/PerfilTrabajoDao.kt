package com.jhon.micontroldidi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso al perfil de trabajo. La tabla tiene una sola fila ([PerfilTrabajoEntity.ID_UNICO]),
 * por lo que las consultas reciben el id explícitamente.
 */
@Dao
interface PerfilTrabajoDao {

    @Query("SELECT * FROM perfil_trabajo WHERE id = :id LIMIT 1")
    fun observar(id: Int): Flow<PerfilTrabajoEntity?>

    @Query("SELECT * FROM perfil_trabajo WHERE id = :id LIMIT 1")
    suspend fun obtener(id: Int): PerfilTrabajoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(perfil: PerfilTrabajoEntity)
}
