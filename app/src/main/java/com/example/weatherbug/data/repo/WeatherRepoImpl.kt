package com.example.weatherbug.data.repo

import com.example.weatherbug.data.datasource.local.ILocalDataSource
import com.example.weatherbug.data.datasource.remote.IRemoteDataSource
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.FavouriteWeatherItem
import com.example.weatherbug.data.models.GeocodingItem
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse

import com.example.weatherbug.core.util.ResponseState
import kotlinx.coroutines.flow.Flow


class WeatherRepoImpl(
    private val remote: IRemoteDataSource,
    private val local:  ILocalDataSource
) : WeatherRepo {

    // ── Remote: Weather ───

    override suspend fun getCurrentWeather(
        lat:   Double,
        lon:   Double,
        units: String,
        lang:  String
    ): ResponseState<WeatherResponse> {

        return try {
            val result = remote.getCurrentWeather(lat, lon, units, lang)

            ResponseState.Success(result)
        } catch (e: Exception) {

            ResponseState.Failure(e.message ?: "Unknown error fetching current weather")
        }
    }

    override suspend fun getHourlyForecast(
        lat:   Double,
        lon:   Double,
        cnt:   Int,
        units: String,
        lang:  String
    ): ResponseState<HourlyForecastResponse> {

        return try {
            val result = remote.getHourlyForecast(lat, lon, cnt, units, lang)

            ResponseState.Success(result)
        } catch (e: Exception) {

            ResponseState.Failure(e.message ?: "Unknown error fetching hourly forecast")
        }
    }

    override suspend fun getDailyForecast(
        lat:   Double,
        lon:   Double,
        cnt:   Int,
        units: String,
        lang:  String
    ): ResponseState<DailyForecastResponse> {

        return try {
            val result = remote.getDailyForecast(lat, lon, cnt, units, lang)

            ResponseState.Success(result)
        } catch (e: Exception) {

            ResponseState.Failure(e.message ?: "Unknown error fetching daily forecast")
        }
    }

    // ── Remote: Geocoding ───

    override suspend fun getCoordinatesByCity(
        cityName: String,
        limit:    Int
    ): ResponseState<List<GeocodingItem>> {

        return try {
            val result = remote.getCoordinatesByCity(cityName, limit)

            ResponseState.Success(result)
        } catch (e: Exception) {

            ResponseState.Failure(e.message ?: "Unknown error fetching coordinates")
        }
    }

    override suspend fun getCityByCoordinates(
        lat: Double,
        lon: Double
    ): ResponseState<List<GeocodingItem>> {

        return try {
            val result = remote.getCityByCoordinates(lat, lon)

            ResponseState.Success(result)
        } catch (e: Exception) {

            ResponseState.Failure(e.message ?: "Unknown error fetching city name")
        }
    }

    // ── Local: Favourites ───

    override fun getAllFavourites(): Flow<List<FavouriteWeatherItem>> =
        local.getAllFavourites()

    override suspend fun insertFavourite(item: FavouriteWeatherItem) =
        local.insertFavourite(item)

    override suspend fun deleteFavourite(item: FavouriteWeatherItem) =
        local.deleteFavourite(item)

    override suspend fun deleteFavouriteById(id: Int) =
        local.deleteFavouriteById(id)

    override suspend fun deleteAllFavourites() =
        local.deleteAllFavourites()

    // ── Local: Alerts ────

    override fun getAllAlerts(): Flow<List<AlertItem>> =
        local.getAllAlerts()

    override suspend fun insertAlert(alert: AlertItem): Long =
        local.insertAlert(alert)

    override suspend fun deleteAlert(alert: AlertItem) =
        local.deleteAlert(alert)

    override suspend fun deleteAlertById(id: Int) =
        local.deleteAlertById(id)

    override suspend fun deleteAllAlerts() =
        local.deleteAllAlerts()

    override suspend fun setAlertActive(id: Int, isActive: Boolean) =
        local.setAlertActive(id, isActive)
}