package com.jhon.micontroldidi.ui.gasto

import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria

enum class ModoFormulario {
    CREACION,
    CARGANDO_EDICION,
    EDICION,
    ERROR_EDICION
}

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
    val guardadoExitoso: Boolean = false,
    val gastoEditandoId: Long? = null,
    val modoFormulario: ModoFormulario = ModoFormulario.CREACION,
    val fechaHoraOriginal: Long = 0L,
    val errorEdicion: String? = null,
    val eliminando: Boolean = false,
    val errorEliminacion: String? = null,
    val gastoIdAEliminar: Long? = null,

    // Estado del filtro por fecha y categoría
    val filtroActivo: Boolean = false,
    val filtroInicio: Long? = null,
    val filtroFin: Long? = null,
    val filtroCategoriaId: Long? = null,
    val mostrarSelectorFecha: Boolean = false,
    val mensajeFiltroVacio: String? = null,

    // Error global de carga de la lista
    val mensajeErrorCarga: String? = null
) {
    val editando: Boolean
        get() = modoFormulario == ModoFormulario.EDICION

    val formularioValido: Boolean
        get() = (modoFormulario == ModoFormulario.CREACION || modoFormulario == ModoFormulario.EDICION)
                && categoriaSeleccionadaId != null
                && errorValor == null
                && valorText.isNotBlank()
                && !guardando
}
