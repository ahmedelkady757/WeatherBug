package com.example.weatherbug.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = Constants.DATASTORE_NAME)


class AppDataStore(private val context: Context) {

    // ── Keys ───

    private companion object {
        val KEY_THEME          = stringPreferencesKey(Constants.KEY_THEME)
        val KEY_LANGUAGE       = stringPreferencesKey(Constants.KEY_LANGUAGE)
        val KEY_TEMP_UNIT      = stringPreferencesKey(Constants.KEY_TEMP_UNIT)
        val KEY_WIND_UNIT      = stringPreferencesKey(Constants.KEY_WIND_UNIT)
        val KEY_LOCATION_MODE  = stringPreferencesKey(Constants.KEY_LOCATION_MODE)
        val KEY_SAVED_LAT      = doublePreferencesKey(Constants.KEY_SAVED_LAT)
        val KEY_SAVED_LON      = doublePreferencesKey(Constants.KEY_SAVED_LON)
        val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey(Constants.KEY_IS_FIRST_LAUNCH)

        private val KEY_GPS_ENABLED = booleanPreferencesKey(Constants.KEY_GPS_ENABLED)
    }


    // ── Flows ────

    val themeFlow: Flow<String> = context.dataStore.data
        .catch { e ->
            AppLogger.logDataStoreError(Constants.KEY_THEME, e)
        }
        .map { prefs ->
            val value = prefs[KEY_THEME] ?: Constants.THEME_LIGHT
            AppLogger.logDataStoreRead(Constants.KEY_THEME, value)
            value
        }

    val languageFlow: Flow<String> = context.dataStore.data
        .catch { e ->
            AppLogger.logDataStoreError(Constants.KEY_LANGUAGE, e)
        }
        .map { prefs ->
            val value = prefs[KEY_LANGUAGE] ?: Constants.LANG_DEVICE
            AppLogger.logDataStoreRead(Constants.KEY_LANGUAGE, value)
            value
        }

    /**
     * Resolves the stored language preference to a concrete BCP-47 language tag
     * that the OpenWeatherMap API (and date formatters) understand.
     *
     * - "device" → reads the current OS locale and extracts its language code
     *              (e.g. "ar", "en", "fr"). Falls back to "en" if unrecognised.
     * - "en" / "ar" / any explicit code → returned as-is.
     *
     * **Always use this flow when making API calls or formatting dates.**
     * Use the raw [languageFlow] only to read which chip is selected in Settings.
     */
    val effectiveLangFlow: Flow<String> = languageFlow.map { stored ->
        if (stored == Constants.LANG_DEVICE) {
            val deviceLang = context.resources.configuration.locales[0].language
            AppLogger.logDataStoreRead("effectiveLang", "device → $deviceLang")
            // OWM supports many codes; pass through whatever the device reports.
            // For our supported set, "ar" and "en" are the relevant ones.
            deviceLang.ifBlank { Constants.LANG_ENGLISH }
        } else {
            stored
        }
    }

    val tempUnitFlow: Flow<String> = context.dataStore.data
        .catch { e ->
            AppLogger.logDataStoreError(Constants.KEY_TEMP_UNIT, e)
        }
        .map { prefs ->
            val value = prefs[KEY_TEMP_UNIT] ?: Constants.UNIT_METRIC
            AppLogger.logDataStoreRead(Constants.KEY_TEMP_UNIT, value)
            value
        }

    val locationModeFlow: Flow<String> = context.dataStore.data
        .catch { e ->
            AppLogger.logDataStoreError(Constants.KEY_LOCATION_MODE, e)
        }
        .map { prefs ->
            val value = prefs[KEY_LOCATION_MODE] ?: Constants.LOCATION_GPS
            AppLogger.logDataStoreRead(Constants.KEY_LOCATION_MODE, value)
            value
        }

    val savedLatFlow: Flow<Double> = context.dataStore.data
        .catch { e ->
            AppLogger.logDataStoreError(Constants.KEY_SAVED_LAT, e)
        }
        .map { prefs ->
            val value = prefs[KEY_SAVED_LAT] ?: Constants.FALLBACK_LAT
            AppLogger.logDataStoreRead(Constants.KEY_SAVED_LAT, value)
            value
        }

    val savedLonFlow: Flow<Double> = context.dataStore.data
        .catch { e ->
            AppLogger.logDataStoreError(Constants.KEY_SAVED_LON, e)
        }
        .map { prefs ->
            val value = prefs[KEY_SAVED_LON] ?: Constants.FALLBACK_LON
            AppLogger.logDataStoreRead(Constants.KEY_SAVED_LON, value)
            value
        }

    val windUnitFlow: Flow<String> = context.dataStore.data
        .catch { e -> AppLogger.logDataStoreError(Constants.KEY_WIND_UNIT, e) }
        .map { prefs ->
            val value = prefs[KEY_WIND_UNIT] ?: Constants.WIND_UNIT_MS
            AppLogger.logDataStoreRead(Constants.KEY_WIND_UNIT, value)
            value
        }

    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data
        .catch { e ->
            AppLogger.logDataStoreError(Constants.KEY_IS_FIRST_LAUNCH, e)
        }
        .map { prefs ->
            val value = prefs[KEY_IS_FIRST_LAUNCH] ?: true
            AppLogger.logDataStoreRead(Constants.KEY_IS_FIRST_LAUNCH, value)
            value
        }


    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme
            AppLogger.logDataStoreWrite(Constants.KEY_THEME, theme)
        }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
            AppLogger.logDataStoreWrite(Constants.KEY_LANGUAGE, language)
        }
    }

    suspend fun saveTempUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TEMP_UNIT] = unit
            AppLogger.logDataStoreWrite(Constants.KEY_TEMP_UNIT, unit)
        }
    }

    suspend fun saveLocationMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOCATION_MODE] = mode
            AppLogger.logDataStoreWrite(Constants.KEY_LOCATION_MODE, mode)
        }
    }

    suspend fun saveLocation(lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_LAT] = lat
            prefs[KEY_SAVED_LON] = lon
            AppLogger.logDataStoreWrite(Constants.KEY_SAVED_LAT, lat)
            AppLogger.logDataStoreWrite(Constants.KEY_SAVED_LON, lon)
        }
    }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_FIRST_LAUNCH] = false
            AppLogger.logDataStoreWrite(Constants.KEY_IS_FIRST_LAUNCH, false)
        }
    }

    suspend fun saveWindUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIND_UNIT] = unit
            AppLogger.logDataStoreWrite(Constants.KEY_WIND_UNIT, unit)
        }
    }

    val gpsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { e -> AppLogger.logDataStoreError(Constants.KEY_GPS_ENABLED, e) }
        .map { prefs ->
            val value = prefs[KEY_GPS_ENABLED] ?: false  // default false = not yet granted
            AppLogger.logDataStoreRead(Constants.KEY_GPS_ENABLED, value)
            value
        }

    // write
    suspend fun saveGpsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GPS_ENABLED] = enabled
            AppLogger.logDataStoreWrite(Constants.KEY_GPS_ENABLED, enabled)
        }
    }
}