package com.example.weatherbug.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {

    fun formatDate(dt: Long, am: String, pm: String): String {
        val date = Date(dt * 1000)

        val dateFormatter = SimpleDateFormat("EEE, MMM d • hh:mm", Locale.ENGLISH)
        val amPmFormatter = SimpleDateFormat("a", Locale.ENGLISH)

        val time = dateFormatter.format(date)
        val period = if (amPmFormatter.format(date) == "AM") am else pm

        return "$time $period"
    }

    fun formatHourly(dtTxt: String, am: String, pm: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val date = input.parse(dtTxt) ?: return dtTxt

            val hourFormatter = SimpleDateFormat("hh:mm", Locale.ENGLISH)
            val amPmFormatter = SimpleDateFormat("a", Locale.ENGLISH)

            val time = hourFormatter.format(date)
            val period = if (amPmFormatter.format(date) == "AM") am else pm

            "$time $period"

        } catch (e: Exception) {
            dtTxt
        }
    }

    fun formatDayName(dt: Long): String {
        val date = Date(dt * 1000)
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(date)
    }
}