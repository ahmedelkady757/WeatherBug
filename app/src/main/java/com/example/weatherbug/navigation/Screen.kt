package com.example.weatherbug.navigation


sealed class Screen(val route: String) {

    data object Home        : Screen("home")
    data object Favourites  : Screen("favourites")
    data object Alerts      : Screen("alerts")
    data object Settings    : Screen("settings")
    data object MapPicker : Screen("map_picker")
}
