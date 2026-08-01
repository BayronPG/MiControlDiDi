package com.jhon.micontroldidi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jhon.micontroldidi.domain.PeriodoMeta

/**
 * Representa una meta de ganancia configurada por el usuario.
 *
 * - Solo una meta puede estar activa a la vez (se desactiva la anterior al crear una nueva).
 * - [tipoPeriodo] se persiste como texto "DIA" o "MES" (contrato de BD);
 *   usa la propiedad [periodo] para trabajar con el enum [PeriodoMeta] en código.
 * - [valorObjetivo] es la cantidad en pesos colombianos enteros que se espera ganar.
 */
@Entity(tableName = "metas")
data class MetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipoPeriodo: String, // "DIA" o "MES" (persistido)
    val valorObjetivo: Long,
    val activa: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Periodo tipado para usar en código. Si el valor persistido no es
     * reconocido (datos corruptos), se degrada a [PeriodoMeta.DIA].
     */
    val periodo: PeriodoMeta
        get() = PeriodoMeta.fromNombre(tipoPeriodo) ?: PeriodoMeta.DIA
}
