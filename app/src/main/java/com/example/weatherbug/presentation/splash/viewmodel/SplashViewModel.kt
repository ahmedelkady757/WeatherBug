package com.example.weatherbug.presentation.splash.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


sealed class SplashNavEvent {
    data object Idle              : SplashNavEvent()
    data object NavigateToHome    : SplashNavEvent()
    data object RequestPermission : SplashNavEvent()
}


class SplashViewModel(
    private val appDataStore: AppDataStore,
    private val fusedClient:  FusedLocationProviderClient
) : ViewModel() {

    private val _navEvent = MutableStateFlow<SplashNavEvent>(SplashNavEvent.Idle)
    val navEvent: StateFlow<SplashNavEvent> = _navEvent.asStateFlow()


    fun decideNavigation() {
        viewModelScope.launch {
            AppLogger.logVmEvent("SplashViewModel", "decideNavigation()")
            val gpsEnabled = appDataStore.gpsEnabledFlow.first()
            AppLogger.logVmEvent("SplashViewModel", "gpsEnabled=$gpsEnabled")

            if (gpsEnabled) {
                fetchLocationAndNavigate()
            } else {
                AppLogger.logVmEvent("SplashViewModel", "→ RequestPermission")
                _navEvent.value = SplashNavEvent.RequestPermission
            }
        }
    }


    fun onPermissionGranted() {
        viewModelScope.launch {
            AppLogger.logVmEvent("SplashViewModel", "permission GRANTED")
            appDataStore.saveGpsEnabled(true)
            fetchLocationAndNavigate()
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            AppLogger.logVmEvent("SplashViewModel", "permission DENIED")
            navigateWithSavedOrFallback()
        }
    }


    @SuppressLint("MissingPermission")
    private fun fetchLocationAndNavigate() {
        viewModelScope.launch {
            val location = getLastLocation() ?: requestFreshLocation()
            if (location != null) {
                AppLogger.logVmEvent(
                    "SplashViewModel",
                    "location ok lat=${location.latitude} lon=${location.longitude}"
                )
                appDataStore.saveLocation(location.latitude, location.longitude)
                appDataStore.setFirstLaunchDone()
                _navEvent.value = SplashNavEvent.NavigateToHome
            } else {
                AppLogger.logVmEvent("SplashViewModel", "location null → saved/fallback")
                navigateWithSavedOrFallback()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener {
                    AppLogger.logVmError("SplashViewModel", "lastLocation failed: ${it.message}")
                    cont.resume(null)
                }
        }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener {
                    AppLogger.logVmError("SplashViewModel", "freshLocation failed: ${it.message}")
                    cont.resume(null)
                }
            cont.invokeOnCancellation { cts.cancel() }
        }

    private suspend fun navigateWithSavedOrFallback() {
        val lat = appDataStore.savedLatFlow.first()
        val lon = appDataStore.savedLonFlow.first()
        AppLogger.logVmEvent("SplashViewModel", "navigating with lat=$lat lon=$lon")
        appDataStore.setFirstLaunchDone()
        _navEvent.value = SplashNavEvent.NavigateToHome
    }
}