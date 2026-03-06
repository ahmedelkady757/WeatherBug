package com.example.weatherbug.data.models

import com.google.gson.annotations.SerializedName


data class HourlyForecastResponse(
    @SerializedName("cod")     val cod:     String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt")     val cnt:     Int,
    @SerializedName("list")    val list:    List<HourlyItem>,
    @SerializedName("city")    val city:    CityData
) {

    data class HourlyItem(
        @SerializedName("dt")         val dt:         Long,
        @SerializedName("main")       val main:       MainData,
        @SerializedName("weather")    val weather:    List<WeatherCondition>,
        @SerializedName("clouds")     val clouds:     CloudsData,
        @SerializedName("wind")       val wind:       WindData,
        @SerializedName("visibility") val visibility: Int?       = null,
        @SerializedName("rain")       val rain:       RainData?  = null,
        @SerializedName("snow")       val snow:       SnowData?  = null,
        @SerializedName("dt_txt")     val dtTxt:      String
    )

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

    data class CityData(
        @SerializedName("id")         val id:         Int,
        @SerializedName("name")       val name:       String,
        @SerializedName("coord")      val coord:      CoordData,
        @SerializedName("country")    val country:    String,
        @SerializedName("population") val population: Int?  = null,
        @SerializedName("timezone")   val timezone:   Int,
        @SerializedName("sunrise")    val sunrise:    Long,
        @SerializedName("sunset")     val sunset:     Long
    )
}