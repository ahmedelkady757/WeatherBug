package com.example.weatherbug.data.datasource.local

import com.example.weatherbug.data.db.WeatherBugDao
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.models.FavouriteWeatherItem

import kotlinx.coroutines.flow.Flow


class LocalDataSource(private val dao: WeatherBugDao) : ILocalDataSource {

    // ── Favourites ───

    override fun getAllFavourites(): Flow<List<FavouriteWeatherItem>> {

        return dao.getAllFavourites()
    }

    override suspend fun insertFavourite(item: FavouriteWeatherItem) {

        dao.insertFavourite(item)
    }

    override suspend fun deleteFavourite(item: FavouriteWeatherItem) {

        dao.deleteFavourite(item)
    }

    override suspend fun deleteFavouriteById(id: Int) {

        dao.deleteFavouriteById(id)
    }

    override suspend fun deleteAllFavourites() {

        dao.deleteAllFavourites()
    }


    // ── Alerts ───

    override fun getAllAlerts(): Flow<List<AlertItem>> {

        return dao.getAllAlerts()
    }

    override suspend fun insertAlert(alert: AlertItem): Long {

        return dao.insertAlert(alert)
    }

    override suspend fun deleteAlert(alert: AlertItem) {

        dao.deleteAlert(alert)
    }

    override suspend fun deleteAlertById(id: Int) {

        dao.deleteAlertById(id)
    }

    override suspend fun deleteAllAlerts() {

        dao.deleteAllAlerts()
    }

    override suspend fun setAlertActive(id: Int, isActive: Boolean) {

        dao.setAlertActive(id, isActive)
    }
}