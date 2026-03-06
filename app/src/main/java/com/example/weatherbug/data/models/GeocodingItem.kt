package com.example.weatherbug.data.models

import com.google.gson.annotations.SerializedName


data class GeocodingItem(
    @SerializedName("name")
    val name: String,
    @SerializedName("local_names")
    val localNames: Map<String, String>? = null,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
    @SerializedName("country")
    val country: String,
    @SerializedName("state")
    val state: String? = null
) {

    fun localizedName(langCode: String): String =
        localNames?.get(langCode) ?: name
}