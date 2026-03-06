package com.example.weatherbug.data.models

import com.google.gson.annotations.SerializedName


data class CoordData(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)
