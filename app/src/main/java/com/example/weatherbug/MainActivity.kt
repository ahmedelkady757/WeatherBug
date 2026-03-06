package com.example.weatherbug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherbug.navigation.NavGraph
import com.example.weatherbug.navigation.Screen
import com.example.weatherbug.ui.theme.WeatherBugTheme
import com.example.weatherbug.util.AppLogger

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLogger.logVmEvent("MainActivity", "onCreate")

        setContent {
            WeatherBugTheme {
                val navController = rememberNavController()
                WeatherBugApp(navController = navController)
            }
        }
    }
}


@Composable
fun WeatherBugApp(navController: NavHostController) {

    val bottomNavScreens = listOf(
        Screen.Home.route,
        Screen.Favourites.route,
        Screen.Alerts.route,
        Screen.Settings.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in bottomNavScreens

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                WeatherBugBottomNav(
                    currentRoute  = currentRoute,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier      = Modifier.padding(innerPadding)
        )
    }
}



data class BottomNavItem(
    val route:          String,
    val labelRes:       Int,
    val selectedIcon:   ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun WeatherBugBottomNav(
    currentRoute:  String?,
    navController: NavHostController
) {
    val items = listOf(
        BottomNavItem(
            route          = Screen.Home.route,
            labelRes       = R.string.nav_home,
            selectedIcon   = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route          = Screen.Favourites.route,
            labelRes       = R.string.nav_favourites,
            selectedIcon   = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder
        ),
        BottomNavItem(
            route          = Screen.Alerts.route,
            labelRes       = R.string.nav_alerts,
            selectedIcon   = Icons.Filled.Notifications,
            unselectedIcon = Icons.Outlined.Notifications
        ),
        BottomNavItem(
            route          = Screen.Settings.route,
            labelRes       = R.string.nav_settings,
            selectedIcon   = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    NavigationBar {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick  = {
                    if (!isSelected) {
                        AppLogger.logNavigation("BottomNav", item.route)
                        navController.navigate(item.route) {
                            // pop up to Home to avoid building a large back stack
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon  = {
                    Icon(
                        imageVector        = if (isSelected) item.selectedIcon
                        else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = {
                    Text(text = stringResource(item.labelRes))
                }
            )
        }
    }
}