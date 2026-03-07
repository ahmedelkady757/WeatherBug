package com.example.weatherbug.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.models.FavouriteWeatherItem


@Database(
    entities     = [FavouriteWeatherItem::class, AlertItem::class],
    version      = 1,
    exportSchema = false
)
abstract class WeatherBugDatabase : RoomDatabase() {
    abstract fun weatherBugDao(): WeatherBugDao
}