package com.example.weatherbug.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "favourites")
data class FavouriteWeatherItem(
    @PrimaryKey(autoGenerate = true)
    val id:          Int    = 0,
    val cityName:    String,
    val country:     String,
    val lat:         Double,
    val lon:         Double,
    val temp:        Double,
    val icon:        String,
    val description: String,
    val addedAt:     Long   = System.currentTimeMillis()
)