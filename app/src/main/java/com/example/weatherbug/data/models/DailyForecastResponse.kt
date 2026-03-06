package com.example.weatherbug.data.models

import com.google.gson.annotations.SerializedName


data class DailyForecastResponse(
    @SerializedName("city")    val city:    CityData,
    @SerializedName("cod")     val cod:     String,
    @SerializedName("message") val message: Double,
    @SerializedName("cnt")     val cnt:     Int,
    @SerializedName("list")    val list:    List<DailyItem>
) {


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



    data class DailyItem(
        @SerializedName("dt")         val dt:        Long,
        @SerializedName("sunrise")    val sunrise:   Long,
        @SerializedName("sunset")     val sunset:    Long,
        @SerializedName("temp")       val temp:      DailyTempData,
        @SerializedName("feels_like") val feelsLike: DailyFeelsLikeData,
        @SerializedName("pressure")   val pressure:  Int,
        @SerializedName("humidity")   val humidity:  Int,
        @SerializedName("weather")    val weather:   List<WeatherCondition>,
        @SerializedName("speed")      val speed:     Double,
        @SerializedName("deg")        val deg:       Int,
        @SerializedName("gust")       val gust:      Double?  = null,
        @SerializedName("clouds")     val clouds:    Int,
        @SerializedName("rain")       val rain:      Double?  = null,
        @SerializedName("snow")       val snow:      Double?  = null
    )



    data class DailyTempData(
        @SerializedName("day")   val day:   Double,
        @SerializedName("min")   val min:   Double,
        @SerializedName("max")   val max:   Double,
        @SerializedName("night") val night: Double,
        @SerializedName("eve")   val eve:   Double,
        @SerializedName("morn")  val morn:  Double
    )


    data class DailyFeelsLikeData(
        @SerializedName("day")   val day:   Double,
        @SerializedName("night") val night: Double,
        @SerializedName("eve")   val eve:   Double,
        @SerializedName("morn")  val morn:  Double
    )


    data class WeatherCondition(
        @SerializedName("id")          val id:          Int,
        @SerializedName("main")        val main:        String,
        @SerializedName("description") val description: String,
        @SerializedName("icon")        val icon:        String
    )
}