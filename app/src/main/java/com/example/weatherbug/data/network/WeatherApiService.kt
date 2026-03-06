package com.example.weatherbug.data.network

import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.GeocodingItem
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.util.Constants
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiService {


    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat")   lat:    Double,
        @Query("lon")   lon:    Double,
        @Query("units") units:  String,
        @Query("lang")  lang:   String,
        @Query("appid") apiKey: String = Constants.API_KEY
    ): WeatherResponse


    @GET("data/2.5/forecast/hourly")
    suspend fun getHourlyForecast(
        @Query("lat")   lat:    Double,
        @Query("lon")   lon:    Double,
        @Query("cnt")   cnt:    Int    = Constants.HOURLY_COUNT,
        @Query("units") units:  String,
        @Query("lang")  lang:   String,
        @Query("appid") apiKey: String = Constants.API_KEY
    ): HourlyForecastResponse


    @GET("data/2.5/forecast/daily")
    suspend fun getDailyForecast(
        @Query("lat")   lat:    Double,
        @Query("lon")   lon:    Double,
        @Query("cnt")   cnt:    Int    = Constants.DAILY_COUNT,
        @Query("units") units:  String,
        @Query("lang")  lang:   String,
        @Query("appid") apiKey: String = Constants.API_KEY
    ): DailyForecastResponse


    @GET("geo/1.0/direct")
    suspend fun getCoordinatesByCity(
        @Query("q")     cityName: String,
        @Query("limit") limit:    Int    = Constants.GEO_LIMIT,
        @Query("appid") apiKey:   String = Constants.API_KEY
    ): List<GeocodingItem>


    @GET("geo/1.0/reverse")
    suspend fun getCityByCoordinates(
        @Query("lat")   lat:    Double,
        @Query("lon")   lon:    Double,
        @Query("limit") limit:  Int    = 1,
        @Query("appid") apiKey: String = Constants.API_KEY
    ): List<GeocodingItem>
}