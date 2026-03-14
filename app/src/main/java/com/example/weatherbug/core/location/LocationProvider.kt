package com.example.weatherbug.core.location


interface LocationProvider {

    suspend fun getLastLocation(): Pair<Double, Double>?

    suspend fun getCurrentLocation(): Pair<Double, Double>?
}
