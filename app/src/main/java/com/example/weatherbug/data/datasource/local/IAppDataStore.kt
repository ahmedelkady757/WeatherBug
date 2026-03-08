package com.example.weatherbug.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface IAppDataStore {

    val themeFlow:         Flow<String>

    val languageFlow:      Flow<String>

    val effectiveLangFlow: Flow<String>

    val tempUnitFlow:      Flow<String>
    val windUnitFlow:      Flow<String>
    val locationModeFlow:  Flow<String>
    val savedLatFlow:      Flow<Double>
    val savedLonFlow:      Flow<Double>
    val isFirstLaunchFlow: Flow<Boolean>
    val gpsEnabledFlow:    Flow<Boolean>

    // ── Mutate ────────────────────────────────────────────────────────────────

    suspend fun saveTheme(theme: String)
    suspend fun saveLanguage(language: String)
    suspend fun saveTempUnit(unit: String)
    suspend fun saveWindUnit(unit: String)
    suspend fun saveLocationMode(mode: String)
    suspend fun saveLocation(lat: Double, lon: Double)
    suspend fun setFirstLaunchDone()
    suspend fun saveGpsEnabled(enabled: Boolean)
}
