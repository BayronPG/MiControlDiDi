package com.jhon.micontroldidi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una meta de ganancia configurada por el usuario.
 *
 * - Solo una meta puede estar activa a la vez (se desactiva la anterior al crear una nueva).
 * - [tipoPeriodo] puede ser "DIA" o "MES".
 * - [valorObjetivo] es la cantidad en pesos colombianos enteros que se espera ganar.
 */
@Entity(tableName = "metas")
data class MetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipoPeriodo: String, // "DIA" o "MES"
    val valorObjetivo: Long,
    val activa: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
