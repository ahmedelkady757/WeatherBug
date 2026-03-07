package com.example.weatherbug.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weatherbug.presentation.favourites.view.FavouritesScreen
import com.example.weatherbug.presentation.home.view.HomeScreen
import com.example.weatherbug.presentation.location.LocationViewModel
import com.example.weatherbug.presentation.settings.view.SettingsScreen
import com.example.weatherbug.util.AppLogger


@Composable
fun NavGraph(
    navController:     NavHostController,
    locationViewModel: LocationViewModel,
    modifier:          Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route,
        modifier         = modifier
    ) {

        composable(route = Screen.Home.route) {
            AppLogger.logNavigation("NavGraph", "Home")
            HomeScreen(locationViewModel = locationViewModel)
        }

        composable(route = Screen.Favourites.route) {
            AppLogger.logNavigation("NavGraph", "Favourites")
            FavouritesScreen(
                onAddFavourite = {
                    AppLogger.logNavigation("NavGraph", "Favourites → MapPicker(favourite)")
               //     navController.navigate(Screen.MapPicker.createRoute(Screen.MapPicker.MODE_FAVOURITE))
                },
                onOpenFavouriteDetail = { lat, lon, cityName ->
                    AppLogger.logNavigation("NavGraph", "Favourites → FavouriteDetail", "city=$cityName")
                 //   navController.navigate(Screen.FavouriteDetail.createRoute(lat, lon, cityName))
                }
            )
        }

        composable(route = Screen.Alerts.route) {
            AppLogger.logNavigation("NavGraph", "Alerts")
            PlaceholderScreen(name = "Alerts")
        }

        composable(route = Screen.Settings.route) {
            AppLogger.logNavigation("NavGraph", "Settings")
            SettingsScreen(
                locationViewModel     = locationViewModel,
                onNavigateToMapPicker = {
                    AppLogger.logNavigation("NavGraph", "Settings → MapPicker")
                    navController.navigate(Screen.MapPicker.route)
                }
            )
        }

        composable(route = Screen.MapPicker.route) {
            AppLogger.logNavigation("NavGraph", "MapPicker")
            PlaceholderScreen(name = "Map Picker")
        }
    }
}