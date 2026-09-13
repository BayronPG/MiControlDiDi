package com.jhon.micontroldidi.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.of("es", "CO"))
    private val formatHora = SimpleDateFormat("HH:mm", Locale.of("es", "CO"))

    fun format(timestamp: Long): String {
        return format.format(Date(timestamp))
    }

    /** Solo la hora, en formato HH:mm. */
    fun formatHora(timestamp: Long): String {
        return formatHora.format(Date(timestamp))
    }
}
