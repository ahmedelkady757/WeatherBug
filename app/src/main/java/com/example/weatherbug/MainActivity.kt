package com.example.weatherbug

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.core.navigation.NavGraph
import com.example.weatherbug.core.navigation.Screen
import com.example.weatherbug.presentation.location.LocationViewModel
import com.example.weatherbug.ui.theme.WeatherBugTheme
import com.example.weatherbug.core.util.AppLogger
import com.example.weatherbug.core.util.Constants
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.LaunchedEffect
import com.example.weatherbug.core.alerts.AlarmSoundPlayer
import com.example.weatherbug.core.alerts.WeatherAlertWorker
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val _navigateToHome = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val navigateToHomeFlow = _navigateToHome.asSharedFlow()

    private var isLaunchedFromNotification = false
    
    val locationViewModel: LocationViewModel by viewModel()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        AppLogger.logVmEvent("MainActivity", "POST_NOTIFICATIONS granted=$granted")
    }

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


    @SuppressLint("LocalContextConfigurationRead")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLogger.logVmEvent("MainActivity", "onCreate")

        observePermissionRequests()
        requestNotificationPermissionIfNeeded()
        checkExactAlarmPermission()
        handleIntent(intent)

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            if (isLaunchedFromNotification) {
                splashScreenView.remove()
                return@setOnExitAnimationListener
            }
            try {
                val iconView = splashScreenView.iconView
                if (iconView != null) {
                    iconView
                        .animate()
                        .setDuration(500L)
                        .alpha(0f)
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .withEndAction {
                            splashScreenView.remove()
                        }
                } else {
                    splashScreenView.remove()
                }
            } catch (e: Exception) {
                splashScreenView.remove()
            }
        }
        setContent {
            val dataStore: IAppDataStore = koinInject()
            val themeValue by dataStore.themeFlow.collectAsState(initial = Constants.THEME_LIGHT)
            val darkTheme  = themeValue == Constants.THEME_DARK
            val appLang    by dataStore.languageFlow.collectAsState(initial = Constants.LANG_ENGLISH)

            val baseContext = LocalContext.current

            val locale = when (appLang) {
                Constants.LANG_ARABIC -> Locale("ar")
                Constants.LANG_DEVICE -> Resources.getSystem().configuration.locales[0]
                else                  -> Locale.ENGLISH
            }

            // 1. Set JVM default locale so formatters like SimpleDateFormat use it natively
            Locale.setDefault(locale)

            val config = Configuration(baseContext.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)

            // 2. Force the Activity's resources to use this config.
            // THIS is the secret for Dialogs: Compose Dialogs spawn a new Window using the 
            // base Activity context. If we don't update it at the resource level, 
            // the Dialog ignores our LocalContext and flips back to English!
            @Suppress("DEPRECATION")
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

            val localizedContext = baseContext.createConfigurationContext(config)

            val isArabic = locale.language == "ar"
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalContext        provides localizedContext
            ) {
                WeatherBugTheme(darkTheme = darkTheme) {
                    val navController = rememberNavController()
                    WeatherBugApp(
                        navController      = navController,
                        locationViewModel  = locationViewModel,
                        navigateToHomeFlow = navigateToHomeFlow
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(currentIntent: Intent?) {
        if (currentIntent?.action == WeatherAlertWorker.ACTION_FROM_NOTIFICATION) {
            isLaunchedFromNotification = true
            AppLogger.logVmEvent("MainActivity", "Launched from notification -> forcing Home screen")
            AlarmSoundPlayer.stop() // Stop background ringtone
            _navigateToHome.tryEmit(Unit)
            currentIntent.action = null // Clear so it doesn't trigger again on rotation
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
            locationViewModel.onPermissionDenied()
        } else {
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


    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Exact Alarms Needed")
                    .setMessage("Weather alerts need exact alarm permission to fire at your chosen time. Please enable 'Alarms & Reminders' for this app.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                        )
                    }
                    .setNegativeButton("Not now", null)
                    .show()
            }
        }
    }
}



@Composable
fun WeatherBugApp(
    navController:      NavHostController,
    locationViewModel:  LocationViewModel,
    navigateToHomeFlow: SharedFlow<Unit>
) {
    LaunchedEffect(Unit) {
        navigateToHomeFlow.collect {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomNav) {
                WeatherBugBottomNav(
                    currentRoute  = currentRoute,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            com.example.weatherbug.ui.components.AnimatedGradientBackground()
            NavGraph(
                navController     = navController,
                modifier          = Modifier.padding(innerPadding),
                locationViewModel = locationViewModel
            )
        }
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

    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) {
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