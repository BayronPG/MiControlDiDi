package com.jhon.micontroldidi.ui.tanqueo

import com.jhon.micontroldidi.data.local.entity.TanqueoEntity

/**
 * Estado del módulo de tanqueos.
 */
data class TanqueoUiState(
    val tanqueos: List<TanqueoEntity> = emptyList(),
    val cargando: Boolean = true,

    // Formulario
    val odometroText: String = "",
    val litrosText: String = "",
    val importeText: String = "",
    val esLleno: Boolean = true,
    val tipoCombustible: String = "",
    val observacionText: String = "",
    val errorOdometro: String? = null,
    val errorLitros: String? = null,
    val errorImporte: String? = null,
    val errorTipoCombustible: String? = null,
    val errorGuardado: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,

    // Edición y eliminación
    val tanqueoEditandoId: Long? = null,
    val editando: Boolean = false,
    val fechaHoraOriginal: Long = 0L,
    val gastoIdOriginal: Long = 0L,
    val eliminando: Boolean = false,
    val tanqueoIdAEliminar: Long? = null,
    val errorEliminacion: String? = null,

    val mensajeError: String? = null
) {

    val hayErrores: Boolean
        get() = errorOdometro != null || errorLitros != null ||
            errorImporte != null || errorTipoCombustible != null

    val formularioValido: Boolean
        get() = !hayErrores && odometroText.isNotBlank() && litrosText.isNotBlank() &&
            importeText.isNotBlank() && tipoCombustible.isNotBlank() && !guardando
}
