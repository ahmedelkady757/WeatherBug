package com.example.weatherbug

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.navigation.NavGraph
import com.example.weatherbug.navigation.Screen
import com.example.weatherbug.presentation.location.LocationViewModel
import com.example.weatherbug.ui.theme.WeatherBugTheme
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject
import java.util.Locale

class MainActivity : ComponentActivity() {


    val locationViewModel: LocationViewModel by viewModel()


    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        AppLogger.logVmEvent("MainActivity", "permission result: granted=$granted")
        if (granted) {
            locationViewModel.onPermissionGranted()
        } else {
            handlePermissionDeniedWithSettingsOption()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLogger.logVmEvent("MainActivity", "onCreate")

        observePermissionRequests()
        locationViewModel.checkAndRequestOnLaunch()

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val iconView = splashScreenView.iconView
            iconView
                .animate()
                .setDuration(500L)
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .withEndAction {
                    splashScreenView.remove()
                }
        }
        setContent {
            val dataStore: AppDataStore = koinInject()
            val themeValue by dataStore.themeFlow.collectAsState(initial = Constants.THEME_LIGHT)
            val darkTheme  = themeValue == Constants.THEME_DARK
            val appLang    by dataStore.languageFlow.collectAsState(initial = Constants.LANG_ENGLISH)

            val baseContext = LocalContext.current
            val locale      = if (appLang == Constants.LANG_ARABIC) Locale("ar") else Locale.ENGLISH
            val localizedContext = run {
                val config = Configuration(baseContext.resources.configuration)
                config.setLocale(locale)
                baseContext.createConfigurationContext(config)
            }

            val layoutDirection = if (appLang == Constants.LANG_ARABIC) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalContext        provides localizedContext
            ) {
                WeatherBugTheme(darkTheme = darkTheme) {
                    val navController = rememberNavController()
                    WeatherBugApp(
                        navController     = navController,
                        locationViewModel = locationViewModel
                    )
                }
            }
        }
    }


    private fun observePermissionRequests() {
        lifecycleScope.launch {
            locationViewModel.shouldRequestPermission.collect { should ->
                if (should) {
                    AppLogger.logVmEvent("MainActivity", "launching permission dialog")
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }

    private fun handlePermissionDeniedWithSettingsOption() {
        val showRationaleFine =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        val showRationaleCoarse =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (showRationaleFine || showRationaleCoarse) {
            // User just denied without "Don't ask again" → let ViewModel handle fallback logic
            locationViewModel.onPermissionDenied()
        } else {
            // "Don't ask again" or permanently denied → show dialog that leads to app settings
            AlertDialog.Builder(this)
                .setTitle(R.string.location_permission_title)
                .setMessage(R.string.location_permission_settings_message)
                .setPositiveButton(R.string.location_permission_open_settings) { _, _ ->
                    openAppSettings()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    locationViewModel.onPermissionDenied()
                }
                .show()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }
}


@Composable
fun WeatherBugApp(
    navController:     NavHostController,
    locationViewModel: LocationViewModel
) {
    val bottomNavScreens = listOf(
        Screen.Home.route,
        Screen.Favourites.route,
        Screen.Alerts.route,
        Screen.Settings.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route
    val showBottomNav     = currentRoute in bottomNavScreens

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
            navController     = navController,
            modifier          = Modifier.padding(innerPadding),
            locationViewModel = locationViewModel
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
                            popUpTo(Screen.Home.route) { saveState = true }
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
                label = { Text(text = stringResource(item.labelRes)) }
            )
        }
    }
}