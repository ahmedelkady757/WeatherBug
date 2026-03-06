package com.example.weatherbug.presentation.splash.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.WeatherApplication
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


sealed class SplashDestination {
    data object Idle : SplashDestination()
    data object NavigateToHome : SplashDestination()
    data object NavigateToMapPicker : SplashDestination()
}


class SplashViewModel(
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Idle)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()


    fun decideDestination() {
        viewModelScope.launch {
            AppLogger.logVmEvent("SplashViewModel", "decideDestination()")

            val locationMode = dataStore.locationModeFlow.first()
            AppLogger.logVmEvent("SplashViewModel", "locationMode=$locationMode")

            _destination.value = when (locationMode) {
                Constants.LOCATION_MAP -> {
                    AppLogger.logVmEvent("SplashViewModel", "→ NavigateToMapPicker")
                    SplashDestination.NavigateToMapPicker
                }
                else -> {
                    AppLogger.logVmEvent("SplashViewModel", "→ NavigateToHome")
                    SplashDestination.NavigateToHome
                }
            }
        }
    }
}


class SplashViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            val app = context.applicationContext as WeatherApplication
            return SplashViewModel(dataStore = app.dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}