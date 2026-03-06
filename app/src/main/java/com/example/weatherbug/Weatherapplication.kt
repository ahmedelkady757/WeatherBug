package com.example.weatherbug

import android.app.Application
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.data.datasource.local.LocalDataSource
import com.example.weatherbug.data.datasource.remote.RemoteDataSource
import com.example.weatherbug.data.db.WeatherBugDatabase
import com.example.weatherbug.data.network.RetrofitClient
import com.example.weatherbug.data.repo.WeatherRepo
import com.example.weatherbug.data.repo.WeatherRepoImpl
import com.example.weatherbug.util.AppLogger


class WeatherApplication : Application() {


    val dataStore: AppDataStore by lazy {
        AppLogger.d("WeatherApplication: creating AppDataStore", "WB_GENERAL")
        AppDataStore(this)
    }

    val repo: WeatherRepo by lazy {
        AppLogger.d("WeatherApplication: creating WeatherRepoImpl", "WB_GENERAL")
        WeatherRepoImpl(remote, local)
    }


    private val database by lazy {
        AppLogger.d("WeatherApplication: creating WeatherBugDatabase", "WB_GENERAL")
        WeatherBugDatabase.getInstance(this)
    }

    private val dao by lazy {
        database.weatherBugDao()
    }

    private val local by lazy {
        AppLogger.d("WeatherApplication: creating LocalDataSource", "WB_GENERAL")
        LocalDataSource(dao)
    }

    private val remote by lazy {
        AppLogger.d("WeatherApplication: creating RemoteDataSource", "WB_GENERAL")
        RemoteDataSource(
            apiService = RetrofitClient.apiService,
            proService = RetrofitClient.proService
        )
    }


    override fun onCreate() {
        super.onCreate()
        AppLogger.d("WeatherApplication: onCreate — app process started", "WB_GENERAL")
    }
}