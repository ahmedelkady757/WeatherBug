package com.example.weatherbug

import android.app.Application
import com.example.weatherbug.util.AppLogger

class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLogger.d("WeatherApplication: onCreate — app started")
    }
}