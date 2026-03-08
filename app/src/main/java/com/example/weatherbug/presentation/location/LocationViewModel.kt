package com.example.weatherbug.presentation.location

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class LocationViewModel(
    private val dataStore:   IAppDataStore,
    private val fusedClient: FusedLocationProviderClient
) : ViewModel() {

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Idle)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _shouldRequestPermission = MutableSharedFlow<Boolean>()
    val shouldRequestPermission: SharedFlow<Boolean> = _shouldRequestPermission.asSharedFlow()

    init {
        viewModelScope.launch {
            val gpsEnabled = dataStore.gpsEnabledFlow.first()
            _permissionState.value = if (gpsEnabled) PermissionState.Granted
            else            PermissionState.Denied
            AppLogger.logVmEvent(
                "LocationViewModel",
                "init: gpsEnabled=$gpsEnabled → ${_permissionState.value}"
            )
        }
    }


    fun checkAndRequestOnLaunch() {
        viewModelScope.launch {
            val isFirstLaunch = dataStore.isFirstLaunchFlow.first()
            val locationMode  = dataStore.locationModeFlow.first()

            AppLogger.logVmEvent(
                "LocationViewModel",
                "checkAndRequestOnLaunch firstLaunch=$isFirstLaunch locationMode=$locationMode"
            )

            if (locationMode == Constants.LOCATION_MAP && !isFirstLaunch) {
                val lat = dataStore.savedLatFlow.first()
                val lon = dataStore.savedLonFlow.first()
                AppLogger.logVmEvent(
                    "LocationViewModel",
                    "map mode → emit saved coords lat=$lat lon=$lon"
                )
                _locationState.value = LocationState.Success(lat, lon)
                return@launch
            }

            // GPS mode behaviour: always ask until user grants at OS level.
            AppLogger.logVmEvent(
                "LocationViewModel",
                "GPS mode → request permission (firstLaunch=$isFirstLaunch)"
            )
            _shouldRequestPermission.emit(true)
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            AppLogger.logVmEvent("LocationViewModel", "onPermissionGranted()")
            dataStore.saveGpsEnabled(true)
            _permissionState.value = PermissionState.Granted
            fetchAndSaveLocation()

            val isFirstLaunch = dataStore.isFirstLaunchFlow.first()
            if (isFirstLaunch) {
                AppLogger.logVmEvent("LocationViewModel", "onPermissionGranted → mark first launch done")
                dataStore.setFirstLaunchDone()
            }
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            AppLogger.logVmEvent("LocationViewModel", "onPermissionDenied()")
            dataStore.saveGpsEnabled(false)
            _permissionState.value = PermissionState.Denied
            val isFirstLaunch = dataStore.isFirstLaunchFlow.first()

            if (isFirstLaunch) {
                // First launch & permission denied → use fallback Cairo coords and persist them
                val fallbackLat = Constants.FALLBACK_LAT
                val fallbackLon = Constants.FALLBACK_LON
                AppLogger.logVmEvent(
                    "LocationViewModel",
                    "first launch denied → fallback lat=$fallbackLat lon=$fallbackLon"
                )
                dataStore.saveLocation(fallbackLat, fallbackLon)
                _locationState.value = LocationState.Success(fallbackLat, fallbackLon)
                dataStore.setFirstLaunchDone()
            } else {
                // Not first launch → keep and reuse last stored values
                val savedLat = dataStore.savedLatFlow.first()
                val savedLon = dataStore.savedLonFlow.first()
                AppLogger.logVmEvent(
                    "LocationViewModel",
                    "denied (not first launch) → using saved lat=$savedLat lon=$savedLon"
                )
                _locationState.value = LocationState.Success(savedLat, savedLon)
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            AppLogger.logVmEvent("LocationViewModel", "refreshLocation() → request permission")
            _shouldRequestPermission.emit(true)
        }
    }


    @SuppressLint("MissingPermission")
    private suspend fun fetchAndSaveLocation() {
        _locationState.value = LocationState.Loading
        AppLogger.logVmEvent("LocationViewModel", "fetchAndSaveLocation()")

        val lastLocation = suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener {
                    AppLogger.logVmError("LocationViewModel", "lastLocation failed: ${it.message}")
                    cont.resume(null)
                }
        }

        if (lastLocation != null) {
            saveAndEmit(lastLocation.latitude, lastLocation.longitude)
            return
        }

        AppLogger.logVmEvent("LocationViewModel", "lastLocation null → fresh fix")
        val cts = CancellationTokenSource()
        val freshLocation = suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener {
                    AppLogger.logVmError("LocationViewModel", "freshLocation failed: ${it.message}")
                    cont.resume(null)
                }
            cont.invokeOnCancellation { cts.cancel() }
        }

        if (freshLocation != null) {
            saveAndEmit(freshLocation.latitude, freshLocation.longitude)
        } else {
            AppLogger.logVmEvent("LocationViewModel", "both failed → DataStore fallback")
            val savedLat = dataStore.savedLatFlow.first()
            val savedLon = dataStore.savedLonFlow.first()
            saveAndEmit(savedLat, savedLon)
        }
    }

    private suspend fun saveAndEmit(lat: Double, lon: Double) {
        AppLogger.logVmEvent("LocationViewModel", "saveAndEmit lat=$lat lon=$lon")
        dataStore.saveLocation(lat, lon)
        _locationState.value = LocationState.Success(lat, lon)
    }
}