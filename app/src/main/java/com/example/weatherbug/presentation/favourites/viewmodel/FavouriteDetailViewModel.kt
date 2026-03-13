package com.example.weatherbug.presentation.favourites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.data.repo.WeatherRepo
import com.example.weatherbug.core.util.AppLogger
import com.example.weatherbug.core.util.Constants
import com.example.weatherbug.core.util.ResponseState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class FavouriteDetailViewModel(
    private val lat:       Double,
    private val lon:       Double,
    private val repo:      WeatherRepo,
    private val dataStore: IAppDataStore
) : ViewModel() {


    // Effective language: resolves "device" → real locale code.
    val appLanguage: StateFlow<String> = dataStore.effectiveLangFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Constants.LANG_ENGLISH)

    val tempUnit: StateFlow<String> = dataStore.tempUnitFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Constants.UNIT_METRIC)
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
        loadWeather()

        viewModelScope.launch {
            dataStore.tempUnitFlow
                .combine(dataStore.effectiveLangFlow) { unit, lang -> unit to lang }
                .distinctUntilChanged()
                .drop(1)
                .collect {
                    AppLogger.logVmEvent("FavouriteDetailViewModel", "settings changed → reload")
                    loadWeather()
                }
        }
    }


    fun retry() {
        AppLogger.logVmEvent("FavouriteDetailViewModel", "retry lat=$lat lon=$lon")
        loadWeather()
    }


    private fun loadWeather() {
        viewModelScope.launch {
            val units = dataStore.tempUnitFlow.first()
            val lang  = dataStore.effectiveLangFlow.first()  // resolved lang, never "device"

            AppLogger.logVmEvent(
                "FavouriteDetailViewModel",
                "loadWeather lat=$lat lon=$lon units=$units lang=$lang"
            )

            _currentWeatherState.value = ResponseState.Loading
            _hourlyState.value         = ResponseState.Loading
            _dailyState.value          = ResponseState.Loading

            // Fire all three requests concurrently
            val currentDeferred = async {
                repo.getCurrentWeather(lat, lon, units, lang)
            }
            val hourlyDeferred = async {
                repo.getHourlyForecast(lat, lon, Constants.HOURLY_COUNT, units, lang)
            }
            val dailyDeferred = async {
                repo.getDailyForecast(lat, lon, Constants.DAILY_COUNT, units, lang)
            }

            _currentWeatherState.value = currentDeferred.await().also {
                AppLogger.logVmEvent("FavouriteDetailViewModel", "currentWeather → ${it::class.simpleName}")
            }
            _hourlyState.value = hourlyDeferred.await().also {
                AppLogger.logVmEvent("FavouriteDetailViewModel", "hourly → ${it::class.simpleName}")
            }
            _dailyState.value = dailyDeferred.await().also {
                AppLogger.logVmEvent("FavouriteDetailViewModel", "daily → ${it::class.simpleName}")
            }
        }
    }
}