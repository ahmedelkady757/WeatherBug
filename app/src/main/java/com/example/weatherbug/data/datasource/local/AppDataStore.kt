package com.example.weatherbug.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import com.example.weatherbug.core.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = Constants.DATASTORE_NAME)


class AppDataStore(private val context: Context) : IAppDataStore {

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

    override val themeFlow: Flow<String> = context.dataStore.data
        .catch { e ->

        }
        .map { prefs ->
            val value = prefs[KEY_THEME] ?: Constants.THEME_LIGHT

            value
        }

    override val languageFlow: Flow<String> = context.dataStore.data
        .catch { e ->

        }
        .map { prefs ->
            val value = prefs[KEY_LANGUAGE] ?: Constants.LANG_DEVICE

            value
        }

    override val effectiveLangFlow: Flow<String> = languageFlow.map { stored ->
        if (stored == Constants.LANG_DEVICE) {
            val deviceLang = context.resources.configuration.locales[0].language

            deviceLang.ifBlank { Constants.LANG_ENGLISH }
        } else {
            stored
        }
    }

    override  val tempUnitFlow: Flow<String> = context.dataStore.data
        .catch { e ->

        }
        .map { prefs ->
            val value = prefs[KEY_TEMP_UNIT] ?: Constants.UNIT_METRIC

            value
        }

    override val locationModeFlow: Flow<String> = context.dataStore.data
        .catch { e ->

        }
        .map { prefs ->
            val value = prefs[KEY_LOCATION_MODE] ?: Constants.LOCATION_GPS

            value
        }

    override val savedLatFlow: Flow<Double> = context.dataStore.data
        .catch { e ->

        }
        .map { prefs ->
            val value = prefs[KEY_SAVED_LAT] ?: Constants.FALLBACK_LAT

            value
        }

    override val savedLonFlow: Flow<Double> = context.dataStore.data
        .catch { e ->

        }
        .map { prefs ->
            val value = prefs[KEY_SAVED_LON] ?: Constants.FALLBACK_LON

            value
        }

    override val windUnitFlow: Flow<String> = context.dataStore.data
        .catch {  }
        .map { prefs ->
            val value = prefs[KEY_WIND_UNIT] ?: Constants.WIND_UNIT_MS

            value
        }

    override val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data
        .catch { e ->

        }
        .map { prefs ->
            val value = prefs[KEY_IS_FIRST_LAUNCH] ?: true

            value
        }


    override suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme

        }
    }

    override suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language

        }
    }

    override  suspend fun saveTempUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TEMP_UNIT] = unit

        }
    }

    override suspend fun saveLocationMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOCATION_MODE] = mode

        }
    }

    override suspend fun saveLocation(lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_LAT] = lat
            prefs[KEY_SAVED_LON] = lon


        }
    }

    override suspend fun setFirstLaunchDone() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_FIRST_LAUNCH] = false

        }
    }

    override suspend fun saveWindUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIND_UNIT] = unit

        }
    }

    override  val gpsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch {  }
        .map { prefs ->
            val value = prefs[KEY_GPS_ENABLED] ?: false  // default false = not yet granted

            value
        }


    override suspend fun saveGpsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GPS_ENABLED] = enabled

        }
    }
}