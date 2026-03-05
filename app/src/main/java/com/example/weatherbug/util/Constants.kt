package com.example.weatherbug.util


object Constants {

    const val API_KEY          = "a50b3547c713e7be1ec57c696006497f"
    const val BASE_URL         = "https://api.openweathermap.org/"
    const val BASE_URL_PRO     = "https://pro.openweathermap.org/"

    const val DATASTORE_NAME   = "weather_bug_prefs"

    const val KEY_THEME           = "theme"
    const val KEY_LANGUAGE        = "language"
    const val KEY_TEMP_UNIT       = "temp_unit"
    const val KEY_LOCATION_MODE   = "location_mode"
    const val KEY_SAVED_LAT       = "saved_lat"
    const val KEY_SAVED_LON       = "saved_lon"
    const val KEY_IS_FIRST_LAUNCH = "is_first_launch"

    const val THEME_LIGHT  = "light"
    const val THEME_DARK   = "dark"

    const val LANG_ENGLISH = "en"
    const val LANG_ARABIC  = "ar"

    const val UNIT_METRIC   = "metric"    // Celsius,    m/s
    const val UNIT_IMPERIAL = "imperial"  // Fahrenheit, mph
    const val UNIT_STANDARD = "standard"  // Kelvin,     m/s

    const val SYMBOL_CELSIUS    = "°C"
    const val SYMBOL_FAHRENHEIT = "°F"
    const val SYMBOL_KELVIN     = "K"

    const val WIND_METRIC_IMPERIAL_LABEL = "m/s"
    const val WIND_IMPERIAL_LABEL        = "mph"

    const val LOCATION_GPS = "gps"
    const val LOCATION_MAP = "map"

    const val HOURLY_COUNT  = 24
    const val DAILY_COUNT   = 7
    const val GEO_LIMIT     = 5

    const val FALLBACK_LAT  = 30.0444
    const val FALLBACK_LON  = 31.2357
}