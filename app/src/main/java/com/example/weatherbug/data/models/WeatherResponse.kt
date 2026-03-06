package com.example.weatherbug.data.models

import com.google.gson.annotations.SerializedName


data class WeatherResponse(
    @SerializedName("coord")
    val coord : CoordData,
    @SerializedName("weather")
    val weather: List<WeatherCondition>,
    @SerializedName("base")
    val base:  String,
    @SerializedName("main")
    val main:  MainData,
    @SerializedName("visibility")
    val visibility: Int?          = null,
    @SerializedName("wind")
    val wind: WindData,
    @SerializedName("clouds")
    val clouds: CloudsData,
    @SerializedName("rain")
    val rain: RainData?     = null,
    @SerializedName("snow")
    val snow: SnowData?     = null,
    @SerializedName("dt")
    val dt:  Long,
    @SerializedName("sys")
    val sys: SysData,
    @SerializedName("timezone")
    val timezone: Int,
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("cod")
    val cod: Int
) {


    data class WeatherCondition(
        @SerializedName("id")          val id:          Int,
        @SerializedName("main")        val main:        String,
        @SerializedName("description") val description: String,
        @SerializedName("icon")        val icon:        String
    )

    data class MainData(
        @SerializedName("temp")       val temp:      Double,
        @SerializedName("feels_like") val feelsLike: Double,
        @SerializedName("temp_min")   val tempMin:   Double,
        @SerializedName("temp_max")   val tempMax:   Double,
        @SerializedName("pressure")   val pressure:  Int,
        @SerializedName("humidity")   val humidity:  Int,
        @SerializedName("sea_level")  val seaLevel:  Int?   = null,
        @SerializedName("grnd_level") val grndLevel: Int?   = null
    )

    data class WindData(
        @SerializedName("speed") val speed: Double,
        @SerializedName("deg")   val deg:   Int,
        @SerializedName("gust")  val gust:  Double? = null
    )

    data class CloudsData(
        @SerializedName("all") val all: Int
    )

    data class RainData(
        @SerializedName("1h") val oneHour: Double? = null
    )

    data class SnowData(
        @SerializedName("1h") val oneHour: Double? = null
    )

    data class SysData(
        @SerializedName("country") val country: String? = null,
        @SerializedName("sunrise") val sunrise: Long?   = null,
        @SerializedName("sunset")  val sunset:  Long?   = null
    )
}