package com.example.weatherbug.data.datasource.local

import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.models.FavouriteWeatherItem
import kotlinx.coroutines.flow.Flow

interface ILocalDataSource {

    // ── Favourites ───

    fun getAllFavourites(): Flow<List<FavouriteWeatherItem>>

    suspend fun insertFavourite(item: FavouriteWeatherItem)

    suspend fun deleteFavourite(item: FavouriteWeatherItem)

    suspend fun deleteFavouriteById(id: Int)

    suspend fun deleteAllFavourites()

    // ── Alerts ───

    fun getAllAlerts(): Flow<List<AlertItem>>

    suspend fun insertAlert(alert: AlertItem): Long

    suspend fun deleteAlert(alert: AlertItem)

    suspend fun deleteAlertById(id: Int)

    suspend fun deleteAllAlerts()

    suspend fun setAlertActive(id: Int, isActive: Boolean)
}