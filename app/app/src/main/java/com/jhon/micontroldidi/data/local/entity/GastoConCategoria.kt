package com.jhon.micontroldidi.data.local.entity

/**
 * POJO que empareja un gasto con el nombre de su categoría.
 */
data class GastoConCategoria(
    val id: Long,
    val fechaHora: Long,
    val categoriaId: Long,
    val nombreCategoria: String,
    val valor: Long,
    val descripcion: String
) {
    val ingresoTotal: Long
        get() = valor
}
