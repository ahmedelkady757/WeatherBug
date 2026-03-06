package com.example.weatherbug.util

import com.example.weatherbug.R


object WeatherIconMapper {


    fun getIcon(owmCode: String): Int = when (owmCode.trim().lowercase()) {

        "01d" -> R.drawable.ic_owm_01d
        "01n" -> R.drawable.ic_owm_01n

        "02d" -> R.drawable.ic_owm_02d
        "02n" -> R.drawable.ic_owm_02n

        "03d" -> R.drawable.ic_owm_03d
        "03n" -> R.drawable.ic_owm_03n

        "04d" -> R.drawable.ic_owm_04d
        "04n" -> R.drawable.ic_owm_04n

        "09d" -> R.drawable.ic_owm_09d
        "09n" -> R.drawable.ic_owm_09n

        "10d" -> R.drawable.ic_owm_10d
        "10n" -> R.drawable.ic_owm_10n

        "11d" -> R.drawable.ic_owm_11d
        "11n" -> R.drawable.ic_owm_11n

        "13d" -> R.drawable.ic_owm_13d
        "13n" -> R.drawable.ic_owm_13n

        "50d" -> R.drawable.ic_owm_50d
        "50n" -> R.drawable.ic_owm_50n

        else  -> R.drawable.ic_owm_01d
    }
}