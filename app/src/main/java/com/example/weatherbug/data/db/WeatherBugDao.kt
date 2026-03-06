package com.example.weatherbug.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.models.FavouriteWeatherItem
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherBugDao {

    // ── Favourites ───

    @Query("SELECT * FROM favourites ORDER BY addedAt DESC")
    fun getAllFavourites(): Flow<List<FavouriteWeatherItem>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(item: FavouriteWeatherItem)


    @Delete
    suspend fun deleteFavourite(item: FavouriteWeatherItem)


    @Query("DELETE FROM favourites WHERE id = :id")
    suspend fun deleteFavouriteById(id: Int)


    @Query("DELETE FROM favourites")
    suspend fun deleteAllFavourites()


    // ── Alerts ───


    @Query("SELECT * FROM alerts ORDER BY startTime ASC")
    fun getAllAlerts(): Flow<List<AlertItem>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertItem): Long


    @Delete
    suspend fun deleteAlert(alert: AlertItem)


    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)


    @Query("DELETE FROM alerts")
    suspend fun deleteAllAlerts()


    @Query("UPDATE alerts SET isActive = :isActive WHERE id = :id")
    suspend fun setAlertActive(id: Int, isActive: Boolean)
}