package com.example.weatherbug.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed class SettingsNavEvent {
    data object NavigateToMapPicker : SettingsNavEvent()
}



class SettingsViewModel(
    private val dataStore:      IAppDataStore,
    private val onRefreshGps:  () -> Unit        // injected action — decoupled from LocationViewModel
) : ViewModel() {


    val theme: StateFlow<String> = dataStore.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Constants.THEME_LIGHT)

    val language: StateFlow<String> = dataStore.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Constants.LANG_ENGLISH)

    val tempUnit: StateFlow<String> = dataStore.tempUnitFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Constants.UNIT_METRIC)

    val windUnit: StateFlow<String> = dataStore.windUnitFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Constants.WIND_UNIT_MS)

    val locationMode: StateFlow<String> = dataStore.locationModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Constants.LOCATION_GPS)


    private val _navEvent = MutableSharedFlow<SettingsNavEvent>()
    val navEvent: SharedFlow<SettingsNavEvent> = _navEvent.asSharedFlow()


    fun setTheme(theme: String) {
        viewModelScope.launch {
            AppLogger.logVmEvent("SettingsViewModel", "setTheme → $theme")
            dataStore.saveTheme(theme)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            AppLogger.logVmEvent("SettingsViewModel", "setLanguage → $lang")
            dataStore.saveLanguage(lang)
        }
    }

    fun setTempUnit(unit: String) {
        viewModelScope.launch {
            AppLogger.logVmEvent("SettingsViewModel", "setTempUnit → $unit")
            dataStore.saveTempUnit(unit)
        }
    }

    fun setWindUnit(unit: String) {
        viewModelScope.launch {
            AppLogger.logVmEvent("SettingsViewModel", "setWindUnit → $unit")
            dataStore.saveWindUnit(unit)
        }
    }


    fun setLocationMode(mode: String) {
        viewModelScope.launch {
            AppLogger.logVmEvent("SettingsViewModel", "setLocationMode → $mode")
            dataStore.saveLocationMode(mode)

            when (mode) {
                Constants.LOCATION_MAP -> {
                    AppLogger.logVmEvent("SettingsViewModel", "mode=map → NavigateToMapPicker")
                    _navEvent.emit(SettingsNavEvent.NavigateToMapPicker)
                }
                Constants.LOCATION_GPS -> {
                    AppLogger.logVmEvent("SettingsViewModel", "mode=gps → refreshLocation")
                    onRefreshGps()
                }
            }
        }
    }
}