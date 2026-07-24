package com.jhon.micontroldidi.ui.gasto

import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria

data class GastoUiState(
    val gastos: List<GastoConCategoria> = emptyList(),
    val categorias: List<CategoriaGastoEntity> = emptyList(),
    val cargando: Boolean = true,
    val categoriaSeleccionadaId: Long? = null,
    val valorText: String = "",
    val descripcionText: String = "",
    val errorCategoria: String? = null,
    val errorValor: String? = null,
    val errorGuardado: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false
) {
    val formularioValido: Boolean
        get() = categoriaSeleccionadaId != null
                && errorValor == null
                && valorText.isNotBlank()
                && !guardando
}
