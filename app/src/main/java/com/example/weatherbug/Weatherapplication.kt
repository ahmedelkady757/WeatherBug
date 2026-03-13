package com.example.weatherbug

import android.app.Application
import androidx.work.Configuration
import com.example.weatherbug.core.di.appModule
import com.example.weatherbug.core.di.networkModule
import com.example.weatherbug.core.di.repoModule
import com.example.weatherbug.core.util.AppLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level



class WeatherApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() {

            val factory = GlobalContext.get().get<KoinWorkerFactory>()
            return Configuration.Builder()
                .setWorkerFactory(factory)
                .build()
        }

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