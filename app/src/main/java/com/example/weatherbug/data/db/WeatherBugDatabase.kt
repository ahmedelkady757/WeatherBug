package com.example.weatherbug.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.models.FavouriteWeatherItem
import com.example.weatherbug.util.AppLogger


@Database(
    entities  = [FavouriteWeatherItem::class, AlertItem::class],
    version   = 1,
    exportSchema = false
)
abstract class WeatherBugDatabase : RoomDatabase() {

    abstract fun weatherBugDao(): WeatherBugDao

    companion object {

        private const val DB_NAME = "weather_bug_db"


        @Volatile
        private var INSTANCE: WeatherBugDatabase? = null


        fun getInstance(context: Context): WeatherBugDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also {
                    AppLogger.d("WeatherBugDatabase: instance created")
                    INSTANCE = it
                }
            }
        }

        private fun buildDatabase(context: Context): WeatherBugDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                WeatherBugDatabase::class.java,
                DB_NAME
            ).build()
        }
    }
}