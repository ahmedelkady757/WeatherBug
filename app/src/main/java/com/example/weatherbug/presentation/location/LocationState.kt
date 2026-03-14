package com.example.weatherbug.presentation.location


sealed class PermissionState {
    data object Idle : PermissionState()
    data object Granted : PermissionState()
    data object Denied : PermissionState()
}


sealed class LocationState {
    data object Idle : LocationState()
    data object Loading : LocationState()
    data class Success(val lat: Double, val lon: Double) : LocationState()
    data class Error(val message: String) : LocationState()
}