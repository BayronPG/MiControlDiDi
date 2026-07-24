package com.jhon.micontroldidi.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categorias_gasto",
    indices = [
        Index(value = ["nombre"], unique = true)
    ]
)
data class CategoriaGastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val nombre: String,
    val activa: Boolean = true
)
