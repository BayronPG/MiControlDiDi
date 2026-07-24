package com.jhon.micontroldidi.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.of("es", "CO"))

    fun format(timestamp: Long): String {
        return format.format(Date(timestamp))
    }
}
