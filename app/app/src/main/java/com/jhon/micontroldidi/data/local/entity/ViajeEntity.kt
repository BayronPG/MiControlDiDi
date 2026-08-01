package com.jhon.micontroldidi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "viajes",
    indices = [
        Index(value = ["fechaHora"])
    ]
)
data class ViajeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fechaHora: Long,
    val valor: Long,
    val propina: Long = 0,
    val observacion: String = ""
) {
    /**
     * Ingreso total del viaje = valor + propina.
     * No se persiste como columna porque puede calcularse a partir de otros datos.
     */
    val ingresoTotal: Long
        get() = valor + propina
}
