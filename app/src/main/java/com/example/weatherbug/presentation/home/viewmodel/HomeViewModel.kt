package com.example.weatherbug.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.data.repo.WeatherRepo
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import com.example.weatherbug.util.ResponseState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class HomeViewModel(
    private val repo:      WeatherRepo,
    private val dataStore: AppDataStore
) : ViewModel() {



    private val _currentWeatherState =
        MutableStateFlow<ResponseState<WeatherResponse>>(ResponseState.Loading)
    val currentWeatherState: StateFlow<ResponseState<WeatherResponse>> =
        _currentWeatherState.asStateFlow()

    private val _hourlyState =
        MutableStateFlow<ResponseState<HourlyForecastResponse>>(ResponseState.Loading)
    val hourlyState: StateFlow<ResponseState<HourlyForecastResponse>> =
        _hourlyState.asStateFlow()

    private val _dailyState =
        MutableStateFlow<ResponseState<DailyForecastResponse>>(ResponseState.Loading)
    val dailyState: StateFlow<ResponseState<DailyForecastResponse>> =
        _dailyState.asStateFlow()

    init {

        viewModelScope.launch {
            dataStore.savedLatFlow
                .combine(dataStore.savedLonFlow) { lat, lon -> lat to lon }
                .distinctUntilChanged()
                .collectLatest { (lat, lon) ->
                    AppLogger.logVmEvent("HomeViewModel", "coords lat=$lat lon=$lon")
                    loadWeather(lat, lon)
                }
        }
    }

    private suspend fun loadWeather(lat: Double, lon: Double) {
        val units = dataStore.tempUnitFlow.first()
        val lang  = dataStore.languageFlow.first()

        AppLogger.logVmEvent(
            "HomeViewModel",
            "loadWeather lat=$lat lon=$lon units=$units lang=$lang"
        )

        _currentWeatherState.value = ResponseState.Loading
        _hourlyState.value         = ResponseState.Loading
        _dailyState.value          = ResponseState.Loading

        val currentDeferred = viewModelScope.async {
            repo.getCurrentWeather(lat, lon, units, lang)
        }
        val hourlyDeferred = viewModelScope.async {
            repo.getHourlyForecast(lat, lon, Constants.HOURLY_COUNT, units, lang)
        }
        val dailyDeferred = viewModelScope.async {
            repo.getDailyForecast(lat, lon, Constants.DAILY_COUNT, units, lang)
        }

        _currentWeatherState.value = currentDeferred.await().also {
            AppLogger.logVmEvent("HomeViewModel", "currentWeather → ${it::class.simpleName}")
        }
        _hourlyState.value = hourlyDeferred.await().also {
            AppLogger.logVmEvent("HomeViewModel", "hourly → ${it::class.simpleName}")
        }
        _dailyState.value = dailyDeferred.await().also {
            AppLogger.logVmEvent("HomeViewModel", "daily → ${it::class.simpleName}")
        }
    }

    fun retry() {
        viewModelScope.launch {
            val lat = dataStore.savedLatFlow.first()
            val lon = dataStore.savedLonFlow.first()
            AppLogger.logVmEvent("HomeViewModel", "retry lat=$lat lon=$lon")
            loadWeather(lat, lon)
        }
    }
}