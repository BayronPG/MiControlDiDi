package com.jhon.micontroldidi.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val format: NumberFormat = NumberFormat.getIntegerInstance(Locale.of("es", "CO"))

    fun format(valor: Long): String {
        return "\$ ${format.format(valor)}"
    }
}
