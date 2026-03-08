package com.example.weatherbug.presentation.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.data.models.FavouriteWeatherItem
import com.example.weatherbug.data.models.GeocodingItem
import com.example.weatherbug.data.repo.WeatherRepo
import com.example.weatherbug.navigation.Screen
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import com.example.weatherbug.util.ResponseState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


sealed class MapPickerEvent {
    data object NavigateBack : MapPickerEvent()
    data class ShowError(val message: String) : MapPickerEvent()
}

class MapPickerViewModel(
    private val repo:      WeatherRepo,
    private val dataStore: AppDataStore
) : ViewModel() {


    private val _selectedLatLng = MutableStateFlow<LatLng?>(null)
    val selectedLatLng: StateFlow<LatLng?> = _selectedLatLng.asStateFlow()


    private val _resolvedCityName = MutableStateFlow("")
    val resolvedCityName: StateFlow<String> = _resolvedCityName.asStateFlow()

    private val _isGeocodingName = MutableStateFlow(false)
    val isGeocodingName: StateFlow<Boolean> = _isGeocodingName.asStateFlow()


    private val searchQueries = MutableSharedFlow<String>(extraBufferCapacity = 64)

    private val _searchResults = MutableStateFlow<List<GeocodingItem>>(emptyList())
    val searchResults: StateFlow<List<GeocodingItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()


    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()


    private val _events = MutableSharedFlow<MapPickerEvent>()
    val events: SharedFlow<MapPickerEvent> = _events.asSharedFlow()


    init {
        observeSearchQueries()
    }


    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            searchQueries.emit(query)
        }
    }


    fun onMapTapped(lat: Double, lon: Double) {
        AppLogger.logVmEvent("MapPickerViewModel", "onMapTapped lat=$lat lon=$lon")
        _selectedLatLng.value   = LatLng(lat, lon)
        _resolvedCityName.value = ""
        resolveCityName(lat, lon)
    }


    fun onPlaceSelected(item: GeocodingItem, appLanguage: String) {
        val localizedName = item.localizedName(appLanguage)
        AppLogger.logVmEvent(
            "MapPickerViewModel",
            "onPlaceSelected lat=${item.lat} lon=${item.lon} city=$localizedName"
        )
        _selectedLatLng.value   = LatLng(item.lat, item.lon)
        _resolvedCityName.value = localizedName
    }


    fun confirmPick(mode: String) {
        if (_isConfirming.value) return
        val latLng = _selectedLatLng.value ?: run {
            AppLogger.logVmError("MapPickerViewModel", "confirmPick called with no pin placed")
            return
        }

        AppLogger.logVmEvent(
            "MapPickerViewModel",
            "confirmPick mode=$mode lat=${latLng.latitude} lon=${latLng.longitude}"
        )

        viewModelScope.launch {
            _isConfirming.value = true
            when (mode) {
                Screen.MapPicker.MODE_SETTINGS  -> confirmSettings(latLng)
                Screen.MapPicker.MODE_FAVOURITE -> confirmFavourite(latLng)
                else -> {
                    AppLogger.logVmError("MapPickerViewModel", "unknown mode: $mode")
                    _isConfirming.value = false
                }
            }
        }
    }

    private suspend fun confirmSettings(latLng: LatLng) {
        dataStore.saveLocation(latLng.latitude, latLng.longitude)
        // Also persist location mode as "map" so the app reopens correctly next launch
        dataStore.saveLocationMode("map")
        AppLogger.logVmEvent(
            "MapPickerViewModel",
            "settings confirmed: saved lat=${latLng.latitude} lon=${latLng.longitude}"
        )
        _isConfirming.value = false
        _events.emit(MapPickerEvent.NavigateBack)
    }


    private suspend fun confirmFavourite(latLng: LatLng) {
        val units = dataStore.tempUnitFlow.first()
        val lang  = dataStore.effectiveLangFlow.first()  // resolved lang, never "device"

        when (val result = repo.getCurrentWeather(latLng.latitude, latLng.longitude, units, lang)) {

            is ResponseState.Success -> {
                val weather  = result.data
                val cityName = _resolvedCityName.value
                    .ifBlank { weather.name }
                    .ifBlank { "%.4f, %.4f".format(latLng.latitude, latLng.longitude) }

                val fav = FavouriteWeatherItem(
                    cityName    = cityName,
                    country     = weather.sys?.country ?: "",
                    lat         = latLng.latitude,
                    lon         = latLng.longitude,
                    temp        = weather.main.temp,
                    icon        = weather.weather.firstOrNull()?.icon ?: "",
                    description = weather.weather.firstOrNull()?.description ?: ""
                )

                repo.insertFavourite(fav)
                AppLogger.logVmEvent("MapPickerViewModel", "favourite saved: ${fav.cityName}")
                _isConfirming.value = false
                _events.emit(MapPickerEvent.NavigateBack)
            }

            is ResponseState.Failure -> {
                AppLogger.logVmError(
                    "MapPickerViewModel",
                    "confirmFavourite failed: ${result.errorMessage}"
                )
                _isConfirming.value = false
                _events.emit(MapPickerEvent.ShowError(result.errorMessage))
            }

            ResponseState.Loading -> Unit
        }
    }

    private fun resolveCityName(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isGeocodingName.value = true
            when (val result = repo.getCityByCoordinates(lat, lon)) {
                is ResponseState.Success -> {
                    val lang  = dataStore.effectiveLangFlow.first()  // resolved lang
                    val first = result.data.firstOrNull()
                    val name  = first?.localizedName(lang) ?: first?.name.orEmpty()
                    AppLogger.logVmEvent("MapPickerViewModel", "geocoded city: $name")
                    _resolvedCityName.value = name
                }
                is ResponseState.Failure -> {
                    AppLogger.logVmError(
                        "MapPickerViewModel",
                        "reverse-geocode failed: ${result.errorMessage}"
                    )
                }
                ResponseState.Loading -> Unit
            }
            _isGeocodingName.value = false
        }
    }


    private fun observeSearchQueries() {
        viewModelScope.launch {
            searchQueries
                .debounce(400)
                .map { it.trim() }
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }


    private suspend fun performSearch(rawQuery: String) {
        val query = rawQuery.trim()
        if (query.length < 2) {
            _searchResults.value = emptyList()
            _searchError.value   = null
            _isSearching.value   = false
            return
        }

        // Heuristic: for Arabic-only queries without explicit country, bias to Egypt ("EG")
        val effectiveQuery = if (query.any { it in '\u0600'..'\u06FF' } && !query.contains(',')) {
            "$query,EG"
        } else {
            query
        }

        AppLogger.logVmEvent("MapPickerViewModel", "performSearch query=$effectiveQuery")
        _isSearching.value = true

        when (val result = repo.getCoordinatesByCity(effectiveQuery, Constants.GEO_LIMIT)) {

            is ResponseState.Success -> {
                val lang          = dataStore.effectiveLangFlow.first()  // resolved lang
                val normalizedQ   = query.lowercase()
                val prioritized   = result.data.sortedWith(
                    compareByDescending<GeocodingItem> { item ->
                        val name = item.localizedName(lang).lowercase()
                        when {
                            name == normalizedQ              -> 3
                            name.startsWith(normalizedQ)     -> 2
                            name.contains(normalizedQ)       -> 1
                            else                             -> 0
                        }
                    }
                )
                _searchResults.value = prioritized
                _searchError.value   = null
                AppLogger.logVmEvent(
                    "MapPickerViewModel",
                    "search success: results=${result.data.size}"
                )
            }

            is ResponseState.Failure -> {
                _searchResults.value = emptyList()
                _searchError.value   = result.errorMessage
                AppLogger.logVmError(
                    "MapPickerViewModel",
                    "search failed: ${result.errorMessage}"
                )
            }

            ResponseState.Loading -> Unit
        }

        _isSearching.value = false
    }
}