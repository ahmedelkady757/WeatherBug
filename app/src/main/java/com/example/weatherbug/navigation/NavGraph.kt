package com.example.weatherbug.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weatherbug.util.AppLogger


@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
        modifier         = modifier
    ) {

        composable(route = Screen.Splash.route) {
            AppLogger.logNavigation("NavGraph", "Splash")
            PlaceholderScreen(name = "Splash")
        }

        composable(route = Screen.Home.route) {
            AppLogger.logNavigation("NavGraph", "Home")
            PlaceholderScreen(name = "Home")
        }

        composable(route = Screen.Favourites.route) {
            AppLogger.logNavigation("NavGraph", "Favourites")
            PlaceholderScreen(name = "Favourites")
        }

        composable(route = Screen.Alerts.route) {
            AppLogger.logNavigation("NavGraph", "Alerts")
            PlaceholderScreen(name = "Alerts")
        }

        composable(route = Screen.Settings.route) {
            AppLogger.logNavigation("NavGraph", "Settings")
            PlaceholderScreen(name = "Settings")
        }

        composable(route = Screen.MapPicker.route) {
            AppLogger.logNavigation("NavGraph", "MapPicker")
            PlaceholderScreen(name = "Map Picker")
        }
    }
}