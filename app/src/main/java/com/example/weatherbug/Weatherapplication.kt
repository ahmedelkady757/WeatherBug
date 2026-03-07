package com.example.weatherbug

import android.app.Application
import com.example.weatherbug.di.appModule
import com.example.weatherbug.di.networkModule
import com.example.weatherbug.di.repoModule
import com.example.weatherbug.util.AppLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppLogger.d("WeatherApplication: onCreate", "WB_GENERAL")

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@WeatherApplication)
            modules(
                appModule,
                networkModule,
                repoModule
            )
        }
    }
}