package com.example.weatherbug.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {

    fun formatDate(dt: Long, lang: String, am: String, pm: String): String {
        val appLocale     = if (lang == "ar") Locale("ar") else Locale.ENGLISH
        val date          = Date(dt * 1000)
        val dateFormatter = SimpleDateFormat("EEE, MMM d • hh:mm", appLocale)
        val amPmFormatter = SimpleDateFormat("a", Locale.ENGLISH) // always EN to compare
        val period        = if (amPmFormatter.format(date) == "AM") am else pm
        return "${dateFormatter.format(date)} $period"
    }

    fun formatHourly(dtTxt: String, am: String, pm: String): String {
        return try {
            val input         = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val date          = input.parse(dtTxt) ?: return dtTxt
            val hourFormatter = SimpleDateFormat("hh:mm", Locale.ENGLISH)
            val amPmFormatter = SimpleDateFormat("a", Locale.ENGLISH)
            val period        = if (amPmFormatter.format(date) == "AM") am else pm
            "${hourFormatter.format(date)} $period"
        } catch (e: Exception) {
            dtTxt
        }
    }

    fun formatDayName(dt: Long, lang: String): String {
        val appLocale = if (lang == "ar") Locale("ar") else Locale.ENGLISH
        val sdf       = SimpleDateFormat("EEEE", appLocale)
        return sdf.format(Date(dt * 1000))
    }
}