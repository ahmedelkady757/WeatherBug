package com.example.weatherbug.navigation

import java.net.URLDecoder
import java.net.URLEncoder


sealed class Screen(val route: String) {

    data object Home        : Screen("home")
    data object Favourites  : Screen("favourites")
    data object Alerts      : Screen("alerts")
    data object Settings    : Screen("settings")

    data object MapPicker : Screen("map_picker")

    data object FavouriteDetail : Screen("favourite_detail/{lat}/{lon}/{cityName}") {
        fun createRoute(lat: Double, lon: Double, cityName: String): String {
            val encoded = URLEncoder.encode(cityName, "UTF-8")
            return "favourite_detail/$lat/$lon/$encoded"
        }

        fun decodeCityName(raw: String): String =
            URLDecoder.decode(raw, "UTF-8")
    }


}
