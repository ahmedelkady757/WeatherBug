package com.example.weatherbug.location


interface LocationProvider {

    suspend fun getLastLocation(): Pair<Double, Double>?

    suspend fun getCurrentLocation(): Pair<Double, Double>?
}
