package com.example.weatherbug.data.datasource.local

import com.example.weatherbug.data.db.WeatherBugDao
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.models.FavouriteWeatherItem
import com.example.weatherbug.core.util.AppLogger
import kotlinx.coroutines.flow.Flow


class LocalDataSource(private val dao: WeatherBugDao) : ILocalDataSource {

    // ── Favourites ───

    override fun getAllFavourites(): Flow<List<FavouriteWeatherItem>> {
        AppLogger.logDbQuery("favourites", "observing all favourites")
        return dao.getAllFavourites()
    }

    override suspend fun insertFavourite(item: FavouriteWeatherItem) {
        AppLogger.logDbInsert("favourites", item)
        dao.insertFavourite(item)
    }

    override suspend fun deleteFavourite(item: FavouriteWeatherItem) {
        AppLogger.logDbDelete("favourites", item.id)
        dao.deleteFavourite(item)
    }

    override suspend fun deleteFavouriteById(id: Int) {
        AppLogger.logDbDelete("favourites", id)
        dao.deleteFavouriteById(id)
    }

    override suspend fun deleteAllFavourites() {
        AppLogger.logDbDeleteAll("favourites")
        dao.deleteAllFavourites()
    }


    // ── Alerts ───

    override fun getAllAlerts(): Flow<List<AlertItem>> {
        AppLogger.logDbQuery("alerts", "observing all alerts")
        return dao.getAllAlerts()
    }

    override suspend fun insertAlert(alert: AlertItem): Long {
        AppLogger.logDbInsert("alerts", alert)
        return dao.insertAlert(alert)
    }

    override suspend fun deleteAlert(alert: AlertItem) {
        AppLogger.logDbDelete("alerts", alert.id)
        dao.deleteAlert(alert)
    }

    override suspend fun deleteAlertById(id: Int) {
        AppLogger.logDbDelete("alerts", id)
        dao.deleteAlertById(id)
    }

    override suspend fun deleteAllAlerts() {
        AppLogger.logDbDeleteAll("alerts")
        dao.deleteAllAlerts()
    }

    override suspend fun setAlertActive(id: Int, isActive: Boolean) {
        AppLogger.logDbQuery("alerts", "setAlertActive id=$id isActive=$isActive")
        dao.setAlertActive(id, isActive)
    }
}