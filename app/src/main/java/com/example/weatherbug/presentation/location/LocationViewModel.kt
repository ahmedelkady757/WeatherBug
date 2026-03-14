package com.example.weatherbug.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.core.location.LocationProvider

import com.example.weatherbug.core.util.Constants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class LocationViewModel(
    private val dataStore:        IAppDataStore,
    private val locationProvider: LocationProvider
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

        }
    }


    fun checkAndRequestOnLaunch() {
        viewModelScope.launch {
            val isFirstLaunch = dataStore.isFirstLaunchFlow.first()
            val locationMode  = dataStore.locationModeFlow.first()



            if (locationMode == Constants.LOCATION_MAP && !isFirstLaunch) {
                val lat = dataStore.savedLatFlow.first()
                val lon = dataStore.savedLonFlow.first()

                _locationState.value = LocationState.Success(lat, lon)
                return@launch
            }


            _shouldRequestPermission.emit(true)
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {

            dataStore.saveGpsEnabled(true)
            _permissionState.value = PermissionState.Granted
            fetchAndSaveLocation()

            val isFirstLaunch = dataStore.isFirstLaunchFlow.first()
            if (isFirstLaunch) {

                dataStore.setFirstLaunchDone()
            }
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {

            dataStore.saveGpsEnabled(false)
            _permissionState.value = PermissionState.Denied
            val isFirstLaunch = dataStore.isFirstLaunchFlow.first()

            if (isFirstLaunch) {
                val fallbackLat = Constants.FALLBACK_LAT
                val fallbackLon = Constants.FALLBACK_LON

                dataStore.saveLocation(fallbackLat, fallbackLon)
                _locationState.value = LocationState.Success(fallbackLat, fallbackLon)
                dataStore.setFirstLaunchDone()
            } else {
                val savedLat = dataStore.savedLatFlow.first()
                val savedLon = dataStore.savedLonFlow.first()

                _locationState.value = LocationState.Success(savedLat, savedLon)
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {

            _shouldRequestPermission.emit(true)
        }
    }


    private suspend fun fetchAndSaveLocation() {
        _locationState.value = LocationState.Loading


        // Try cached location first (fast path)
        val last = locationProvider.getLastLocation()
        if (last != null) {
            saveAndEmit(last.first, last.second)
            return
        }


        val fresh = locationProvider.getCurrentLocation()
        if (fresh != null) {
            saveAndEmit(fresh.first, fresh.second)
        } else {

            val savedLat = dataStore.savedLatFlow.first()
            val savedLon = dataStore.savedLonFlow.first()
            saveAndEmit(savedLat, savedLon)
        }
    }

    private suspend fun saveAndEmit(lat: Double, lon: Double) {

        dataStore.saveLocation(lat, lon)
        _locationState.value = LocationState.Success(lat, lon)
    }
}