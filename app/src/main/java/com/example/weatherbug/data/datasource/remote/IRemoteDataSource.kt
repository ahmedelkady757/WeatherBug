package com.example.weatherbug.data.datasource.remote

import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.GeocodingItem
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse


interface IRemoteDataSource {


    suspend fun getCurrentWeather(
        lat:   Double,
        lon:   Double,
        units: String,
        lang:  String
    ): WeatherResponse


    suspend fun getHourlyForecast(
        lat:   Double,
        lon:   Double,
        cnt:   Int,
        units: String,
        lang:  String
    ): HourlyForecastResponse


    suspend fun getDailyForecast(
        lat:   Double,
        lon:   Double,
        cnt:   Int,
        units: String,
        lang:  String
    ): DailyForecastResponse


    suspend fun getCoordinatesByCity(
        cityName: String,
        limit:    Int
    ): List<GeocodingItem>


    suspend fun getCityByCoordinates(
        lat: Double,
        lon: Double
    ): List<GeocodingItem>
}