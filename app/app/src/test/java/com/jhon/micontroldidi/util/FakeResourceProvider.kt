package com.jhon.micontroldidi.util

/**
 * ResourceProvider falso para pruebas unitarias.
 * Mapea IDs de recursos a sus valores esperados.
 */
class FakeResourceProvider : ResourceProvider {

    private val map = mapOf(
        com.jhon.micontroldidi.R.string.error_valor_obligatorio to "El valor es obligatorio",
        com.jhon.micontroldidi.R.string.error_valor_numerico to "El valor debe ser numérico",
        com.jhon.micontroldidi.R.string.error_valor_positivo to "El valor debe ser mayor que cero",
        com.jhon.micontroldidi.R.string.error_propina_numerica to "La propina debe ser numérica",
        com.jhon.micontroldidi.R.string.error_propina_negativa to "La propina no puede ser negativa",
        com.jhon.micontroldidi.R.string.error_categoria_obligatoria to "Debes seleccionar una categoría",
        com.jhon.micontroldidi.R.string.gasto_no_encontrado to "Gasto no encontrado",
        com.jhon.micontroldidi.R.string.viajes_sin_resultados to "No hay viajes en el rango seleccionado",
        com.jhon.micontroldidi.R.string.gastos_sin_resultados to "No hay gastos en el rango seleccionado",
        com.jhon.micontroldidi.R.string.dashboard_error to "Error al cargar los datos del dashboard"
    )

    override fun getString(resId: Int): String {
        return map[resId] ?: throw IllegalArgumentException("Resource ID $resId no registrado en FakeResourceProvider")
    }
}
