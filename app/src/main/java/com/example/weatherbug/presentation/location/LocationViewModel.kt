package com.example.weatherbug.presentation.location

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.WeatherApplication
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.util.AppLogger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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


    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()


    private val _shouldRequestPermission = MutableSharedFlow<Boolean>()
    val shouldRequestPermission: SharedFlow<Boolean> = _shouldRequestPermission.asSharedFlow()


    init {
        viewModelScope.launch {
            val gpsEnabled = dataStore.gpsEnabledFlow.first()
            _permissionState.value = if (gpsEnabled) {
                PermissionState.Granted
            } else {
                PermissionState.Denied
            }
            AppLogger.logVmEvent(
                "LocationViewModel",
                "init: gpsEnabled=$gpsEnabled permissionState=${_permissionState.value}"
            )
        }
    }


    fun requestPermission() {
        viewModelScope.launch {
            AppLogger.logVmEvent("LocationViewModel", "requestPermission()")
            _shouldRequestPermission.emit(true)
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
            _locationState.value = LocationState.Error("Location permission denied")
        }
    }


    fun refreshLocation() {
        viewModelScope.launch {
            val gpsEnabled = dataStore.gpsEnabledFlow.first()
            AppLogger.logVmEvent("LocationViewModel", "refreshLocation() gpsEnabled=$gpsEnabled")

            if (!gpsEnabled) {
                _locationState.value = LocationState.Error("GPS disabled — using saved coords")
                return@launch
            }

            fetchAndSaveLocation()
        }
    }


    @SuppressLint("MissingPermission")
    private suspend fun fetchAndSaveLocation() {
        _locationState.value = LocationState.Loading
        AppLogger.logVmEvent("LocationViewModel", "fetchAndSaveLocation() — trying lastLocation")

        // 1. try last known location (fast, no battery cost)
        val lastLocation = suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { loc ->
                    AppLogger.logVmEvent("LocationViewModel", "lastLocation=$loc")
                    cont.resume(loc)
                }
                .addOnFailureListener { e ->
                    AppLogger.logVmError("LocationViewModel", "lastLocation failed: ${e.message}")
                    cont.resume(null)
                }
        }

        if (lastLocation != null) {
            saveAndEmit(lastLocation.latitude, lastLocation.longitude)
            return
        }

        AppLogger.logVmEvent("LocationViewModel", "lastLocation null → requesting fresh fix")
        val cancellationTokenSource = CancellationTokenSource()

        val freshLocation = suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
                .addOnSuccessListener { loc ->
                    AppLogger.logVmEvent("LocationViewModel", "freshLocation=$loc")
                    cont.resume(loc)
                }
                .addOnFailureListener { e ->
                    AppLogger.logVmError("LocationViewModel", "freshLocation failed: ${e.message}")
                    cont.resume(null)
                }

            cont.invokeOnCancellation { cancellationTokenSource.cancel() }
        }

        if (freshLocation != null) {
            saveAndEmit(freshLocation.latitude, freshLocation.longitude)
        } else {
            // 3. both attempts failed — fall back to DataStore saved coords
            AppLogger.logVmEvent("LocationViewModel", "both attempts failed → using DataStore coords")
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



class LocationViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            val app = context.applicationContext as WeatherApplication
            return LocationViewModel(
                dataStore   = app.dataStore,
                fusedClient = LocationServices.getFusedLocationProviderClient(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}