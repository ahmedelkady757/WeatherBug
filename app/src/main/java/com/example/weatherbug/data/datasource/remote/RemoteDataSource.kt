package com.example.weatherbug.data.datasource.remote


import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.GeocodingItem
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.data.network.WeatherApiService
import com.example.weatherbug.core.util.AppLogger


class RemoteDataSource(
    private val apiService: WeatherApiService,
    private val proService: WeatherApiService
) : IRemoteDataSource {

    override suspend fun getCurrentWeather(
        lat:   Double,
        lon:   Double,
        units: String,
        lang:  String
    ): WeatherResponse {
        AppLogger.logRepoCall(
            method = "getCurrentWeather",
            params = "lat=$lat lon=$lon units=$units lang=$lang"
        )
        return apiService.getCurrentWeather(
            lat   = lat,
            lon   = lon,
            units = units,
            lang  = lang
        )
    }

    override suspend fun getHourlyForecast(
        lat:   Double,
        lon:   Double,
        cnt:   Int,
        units: String,
        lang:  String
    ): HourlyForecastResponse {
        AppLogger.logRepoCall(
            method = "getHourlyForecast",
            params = "lat=$lat lon=$lon cnt=$cnt units=$units lang=$lang"
        )
        return proService.getHourlyForecast(
            lat   = lat,
            lon   = lon,
            cnt   = cnt,
            units = units,
            lang  = lang
        )
    }

    override suspend fun getDailyForecast(
        lat:   Double,
        lon:   Double,
        cnt:   Int,
        units: String,
        lang:  String
    ): DailyForecastResponse {
        AppLogger.logRepoCall(
            method = "getDailyForecast",
            params = "lat=$lat lon=$lon cnt=$cnt units=$units lang=$lang"
        )
        return apiService.getDailyForecast(
            lat   = lat,
            lon   = lon,
            cnt   = cnt,
            units = units,
            lang  = lang
        )
    }

    override suspend fun getCoordinatesByCity(
        cityName: String,
        limit:    Int
    ): List<GeocodingItem> {
        AppLogger.logRepoCall(
            method = "getCoordinatesByCity",
            params = "cityName=$cityName limit=$limit"
        )
        return apiService.getCoordinatesByCity(
            cityName = cityName,
            limit    = limit
        )
    }

    override suspend fun getCityByCoordinates(
        lat: Double,
        lon: Double
    ): List<GeocodingItem> {
        AppLogger.logRepoCall(
            method = "getCityByCoordinates",
            params = "lat=$lat lon=$lon"
        )
        return apiService.getCityByCoordinates(
            lat = lat,
            lon = lon
        )
    }
}