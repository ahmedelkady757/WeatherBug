package com.example.weatherbug

import android.app.Application
import androidx.work.Configuration
import com.example.weatherbug.di.appModule
import com.example.weatherbug.di.networkModule
import com.example.weatherbug.di.repoModule
import com.example.weatherbug.util.AppLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class WeatherApplication : Application(), Configuration.Provider, KoinComponent {

    private val workerFactory: KoinWorkerFactory by inject()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        AppLogger.d("WeatherApplication: onCreate", "WB_GENERAL")

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@WeatherApplication)
            workManagerFactory()
            modules(
                appModule,
                networkModule,
                repoModule
            )
        }
    }
}