package com.jhon.micontroldidi.data.local.entity

/**
 * POJO que empareja un gasto con el nombre de su categoría.
 *
 * [esTanqueo] indica que el gasto procede de un tanqueo: ese gasto no se edita
 * ni se elimina desde la pantalla general de Gastos.
 */
data class GastoConCategoria(
    val id: Long,
    val fechaHora: Long,
    val categoriaId: Long,
    val nombreCategoria: String,
    val valor: Long,
    val descripcion: String,
    val esTanqueo: Boolean = false
) {
    val ingresoTotal: Long
        get() = valor
}
