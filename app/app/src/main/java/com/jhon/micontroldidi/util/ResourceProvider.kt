package com.jhon.micontroldidi.util

/**
 * Interfaz para obtener textos de recursos sin acoplar los ViewModel a Android.
 * La implementación concreta usa [android.content.res.Resources.getString].
 */
fun interface ResourceProvider {
    fun getString(resId: Int): String
}
