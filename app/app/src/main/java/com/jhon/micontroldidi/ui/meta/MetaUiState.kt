package com.jhon.micontroldidi.ui.meta

import com.jhon.micontroldidi.data.local.entity.MetaEntity

/**
 * Estado de la pantalla de configuración de meta.
 */
data class MetaUiState(
    // Meta activa existente (si hay)
    val metaActiva: MetaEntity? = null,
    val cargando: Boolean = true,

    // Formulario
    val tipoPeriodo: String = "DIA",
    val valorText: String = "",
    val errorValor: String? = null,
    val errorPeriodo: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val mensajeError: String? = null
) {
    val formularioValido: Boolean
        get() = errorValor == null && valorText.isNotBlank() && !guardando
}
