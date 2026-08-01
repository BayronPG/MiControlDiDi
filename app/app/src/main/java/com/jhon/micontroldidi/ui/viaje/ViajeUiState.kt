package com.jhon.micontroldidi.ui.viaje

import com.jhon.micontroldidi.data.local.entity.ViajeEntity

/**
 * Estado completo del módulo de viajes.
 */
data class ViajeUiState(
    val viajes: List<ViajeEntity> = emptyList(),
    val cargando: Boolean = true,

    // Estado del formulario
    val valorText: String = "",
    val propinaText: String = "",
    val observacionText: String = "",
    val errorValor: String? = null,
    val errorPropina: String? = null,
    val errorGuardado: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,

    // Estado del filtro por fecha
    val filtroActivo: Boolean = false,
    val filtroInicio: Long? = null,
    val filtroFin: Long? = null,
    val mostrarSelectorFecha: Boolean = false,
    val mensajeFiltroVacio: String? = null,

    // Estado de edición y eliminación
    val viajeEditandoId: Long? = null,
    val editando: Boolean = false,
    val fechaHoraOriginal: Long = 0L,
    val eliminando: Boolean = false,
    val viajeIdAEliminar: Long? = null,
    val errorEliminacion: String? = null,

    // Estado de error global
    val mensajeError: String? = null
) {
    val hayErrores: Boolean
        get() = errorValor != null || errorPropina != null

    val formularioValido: Boolean
        get() = !hayErrores && valorText.isNotBlank() && !guardando
}
