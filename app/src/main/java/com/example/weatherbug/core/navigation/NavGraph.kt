package com.example.weatherbug.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weatherbug.presentation.alerts.view.AlertsScreen
import com.example.weatherbug.presentation.favourites.view.FavouriteDetailScreen
import com.example.weatherbug.presentation.favourites.view.FavouritesScreen
import com.example.weatherbug.presentation.home.view.HomeScreen
import com.example.weatherbug.presentation.location.LocationViewModel
import com.example.weatherbug.presentation.map.view.MapPickerScreen
import com.example.weatherbug.presentation.settings.view.SettingsScreen
import com.example.weatherbug.presentation.splash.view.SplashScreen



@Composable
fun NavGraph(
    navController:     NavHostController,
    locationViewModel: LocationViewModel,
    modifier:          Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
        modifier         = modifier
    ) {

        composable(route = Screen.Splash.route) {

            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                    // Ask for location AFTER splash is gone, not during animation
                    locationViewModel.checkAndRequestOnLaunch()
                },
            )
        }

        composable(route = Screen.Home.route) {

            HomeScreen(locationViewModel = locationViewModel)
        }

        composable(route = Screen.Favourites.route) {

            FavouritesScreen(
                onAddFavourite = {

                    navController.navigate(
                        Screen.MapPicker.createRoute(Screen.MapPicker.MODE_FAVOURITE)
                    )
                },
                onOpenFavouriteDetail = { lat, lon, cityName ->

                    navController.navigate(
                        Screen.FavouriteDetail.createRoute(lat, lon, cityName)
                    )
                }
            )
        }

        composable(route = Screen.Alerts.route) {

            AlertsScreen()
        }

        composable(route = Screen.Settings.route) {

            SettingsScreen(
                locationViewModel     = locationViewModel,
                onNavigateToMapPicker = {

                    navController.navigate(Screen.MapPicker.createRoute(Screen.MapPicker.MODE_SETTINGS))
                }
            )
        }

        composable(route = Screen.MapPicker.route) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode")
                ?: Screen.MapPicker.MODE_SETTINGS

            MapPickerScreen(
                mode           = mode,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.FavouriteDetail.route) { backStackEntry ->
            val latArg  = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lonArg  = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()
            val rawName = backStackEntry.arguments?.getString("cityName").orEmpty()
            val city    = Screen.FavouriteDetail.decodeCityName(rawName)

            if (latArg == null || lonArg == null) {

                navController.popBackStack()
            } else {

                FavouriteDetailScreen(
                    lat            = latArg,
                    lon            = lonArg,
                    cityName       = city,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}