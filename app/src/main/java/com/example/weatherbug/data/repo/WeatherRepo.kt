package com.example.weatherbug.data.repo

import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.FavouriteWeatherItem
import com.example.weatherbug.data.models.GeocodingItem
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.core.util.ResponseState
import kotlinx.coroutines.flow.Flow


interface WeatherRepo {

    // ── Remote: Weather ──
    suspend fun getCurrentWeather(
        lat:   Double,
        lon:   Double,
        units: String,
        lang:  String
    ): ResponseState<WeatherResponse>


    suspend fun getHourlyForecast(
        lat:   Double,
        lon:   Double,
        cnt:   Int,
        units: String,
        lang:  String
    ): ResponseState<HourlyForecastResponse>


    suspend fun getDailyForecast(
        lat:   Double,
        lon:   Double,
        cnt:   Int,
        units: String,
        lang:  String
    ): ResponseState<DailyForecastResponse>


    // ── Remote: Geocoding ──
    suspend fun getCoordinatesByCity(
        cityName: String,
        limit:    Int
    ): ResponseState<List<GeocodingItem>>


    suspend fun getCityByCoordinates(
        lat: Double,
        lon: Double
    ): ResponseState<List<GeocodingItem>>


   // ── Local: Favourites ──

    fun getAllFavourites(): Flow<List<FavouriteWeatherItem>>

    suspend fun insertFavourite(item: FavouriteWeatherItem)

    suspend fun deleteFavourite(item: FavouriteWeatherItem)

    suspend fun deleteFavouriteById(id: Int)

    suspend fun deleteAllFavourites()


    fun getAllAlerts(): Flow<List<AlertItem>>





    // ── Local: Alerts ──
    suspend fun insertAlert(alert: AlertItem): Long

    suspend fun deleteAlert(alert: AlertItem)

    suspend fun deleteAlertById(id: Int)

    suspend fun deleteAllAlerts()

    suspend fun setAlertActive(id: Int, isActive: Boolean)
}