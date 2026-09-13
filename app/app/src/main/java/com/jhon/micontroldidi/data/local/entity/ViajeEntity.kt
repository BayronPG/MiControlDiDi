package com.jhon.micontroldidi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jhon.micontroldidi.domain.FormaPago

/**
 * Viaje registrado por el conductor.
 *
 * - [valor] es el precio cobrado por el viaje y [propina] la propina; su suma,
 *   [ingresoTotal], se calcula y no se persiste.
 * - [distanciaMetros] se guarda en **metros** (`Long`), la misma unidad que el
 *   resto de distancias del proyecto, y se captura a mano (sin GPS).
 * - [formaPago] se persiste como texto estable del catálogo [FormaPago]; los
 *   viajes migrados desde versiones anteriores quedan con cadena vacía y se
 *   exponen tipados en [formaPagoTipo] (`null` cuando no hay valor válido).
 * - [peaje] es un dato informativo del viaje: no se suma a [ingresoTotal], no se
 *   resta de la ganancia y no crea ningún gasto.
 */
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
    val observacion: String = "",
    val plataforma: String = "",
    val zona: String = "",
    val distanciaMetros: Long = 0,
    val formaPago: String = "",
    val peaje: Long = 0
) {
    /**
     * Ingreso total del viaje = valor + propina.
     * No se persiste como columna porque puede calcularse a partir de otros datos.
     * El peaje no forma parte del ingreso.
     */
    val ingresoTotal: Long
        get() = valor + propina

    /** Forma de pago tipada; `null` en viajes migrados o con valor desconocido. */
    val formaPagoTipo: FormaPago?
        get() = FormaPago.fromNombre(formaPago)

    /** Distancia del viaje en kilómetros, solo para mostrar. */
    val distanciaKm: Long
        get() = distanciaMetros / METROS_POR_KM

    companion object {
        const val METROS_POR_KM = 1000L

        fun kmAMetros(km: Long): Long = km * METROS_POR_KM
    }
}
