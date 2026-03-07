package com.example.weatherbug.presentation.location

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.AppDataStore
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
    private val dataStore:   AppDataStore,
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
            val locationMode = dataStore.locationModeFlow.first()
            val gpsEnabled   = dataStore.gpsEnabledFlow.first()

            AppLogger.logVmEvent(
                "LocationViewModel",
                "checkAndRequestOnLaunch locationMode=$locationMode gpsEnabled=$gpsEnabled"
            )

            when {
                locationMode == Constants.LOCATION_MAP -> {
                    AppLogger.logVmEvent("LocationViewModel", "map mode → emit saved coords")
                    _locationState.value = LocationState.Success(
                        lat = dataStore.savedLatFlow.first(),
                        lon = dataStore.savedLonFlow.first()
                    )
                }
                !gpsEnabled -> {
                    AppLogger.logVmEvent("LocationViewModel", "gpsEnabled=false → request permission")
                    _shouldRequestPermission.emit(true)
                }
                else -> {
                    AppLogger.logVmEvent("LocationViewModel", "gpsEnabled=true → fetch location")
                    fetchAndSaveLocation()
                }
            }
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            AppLogger.logVmEvent("LocationViewModel", "onPermissionGranted()")
            dataStore.saveGpsEnabled(true)
            _permissionState.value = PermissionState.Granted
            fetchAndSaveLocation()
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            AppLogger.logVmEvent("LocationViewModel", "onPermissionDenied()")
            dataStore.saveGpsEnabled(false)
            _permissionState.value = PermissionState.Denied

            // fall back to whatever coords are already saved
            val savedLat = dataStore.savedLatFlow.first()
            val savedLon = dataStore.savedLonFlow.first()
            AppLogger.logVmEvent("LocationViewModel", "denied → using lat=$savedLat lon=$savedLon")
            _locationState.value = LocationState.Success(savedLat, savedLon)
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val gpsEnabled = dataStore.gpsEnabledFlow.first()
            AppLogger.logVmEvent("LocationViewModel", "refreshLocation() gpsEnabled=$gpsEnabled")
            if (!gpsEnabled) {
                _shouldRequestPermission.emit(true)
                return@launch
            }
            fetchAndSaveLocation()
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