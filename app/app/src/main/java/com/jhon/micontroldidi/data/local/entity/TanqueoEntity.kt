package com.jhon.micontroldidi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tanqueo de gasolina extra asociado **siempre** a un gasto de categoría Gasolina.
 *
 * - [odometroMetros] va en metros (`Long`) y [litrosMililitros] en mililitros
 *   (`Long`): sin punto flotante en ninguna unidad.
 * - [importePagado] es el valor del gasto vinculado y la única cifra de dinero
 *   persistida; [precioLitro] se calcula y no se guarda.
 * - [gastoId] apunta al gasto que este tanqueo mantiene sincronizado.
 */
@Entity(
    tableName = "tanqueos",
    foreignKeys = [
        ForeignKey(
            entity = GastoEntity::class,
            parentColumns = ["id"],
            childColumns = ["gastoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["fechaHora"]),
        Index(value = ["gastoId"])
    ]
)
data class TanqueoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fechaHora: Long,
    val odometroMetros: Long,
    val litrosMililitros: Long,
    val importePagado: Long,
    val esLleno: Boolean = true,
    val tipoCombustible: String = "",
    val observacion: String = "",
    val gastoId: Long = 0
) {

    /** Litros completos, solo para mostrar. */
    val litros: Long
        get() = litrosMililitros / MILILITROS_POR_LITRO

    /**
     * Precio por litro en pesos enteros.
     * Se calcula a partir del importe y los mililitros; no se persiste.
     */
    val precioLitro: Long
        get() = if (litrosMililitros > 0) {
            (importePagado * MILILITROS_POR_LITRO) / litrosMililitros
        } else {
            0
        }

    companion object {
        const val MILILITROS_POR_LITRO = 1000L
    }
}
