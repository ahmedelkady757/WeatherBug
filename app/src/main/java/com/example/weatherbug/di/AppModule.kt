package com.example.weatherbug.di

import androidx.datastore.dataStore
import androidx.room.Room
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.data.datasource.local.ILocalDataSource
import com.example.weatherbug.data.datasource.local.LocalDataSource
import com.example.weatherbug.data.datasource.remote.IRemoteDataSource
import com.example.weatherbug.data.datasource.remote.RemoteDataSource
import com.example.weatherbug.data.db.WeatherBugDatabase
import com.example.weatherbug.data.network.WeatherApiService
import com.example.weatherbug.data.repo.WeatherRepo
import com.example.weatherbug.data.repo.WeatherRepoImpl
import com.example.weatherbug.presentation.home.viewmodel.HomeViewModel
import com.example.weatherbug.presentation.splash.viewmodel.SplashViewModel
import com.example.weatherbug.util.AppLogger
import com.example.weatherbug.util.Constants
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// ── App module ────────────────────────────────────────────────────────────────
// DataStore, Database, DAO, FusedLocationProvider

val appModule = module {

    single { AppDataStore(androidContext()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            WeatherBugDatabase::class.java,
            Constants.DB_NAME
        ).build()
    }

    single { get<WeatherBugDatabase>().weatherBugDao() }

    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
}

// ── Network module ────────────────────────────────────────────────────────────
// OkHttp, Retrofit x2, WeatherApiService x2

val networkModule = module {

    // raw HTTP body logger
    single {
        HttpLoggingInterceptor { message ->
            AppLogger.d(message, "WB_OKHTTP")
        }.apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    // structured app-level request/response logger
    single<okhttp3.Interceptor> {
        okhttp3.Interceptor { chain ->
            val request = chain.request()

            AppLogger.logRequest(
                url    = request.url.toString(),
                method = request.method
            )

            val startMs  = System.currentTimeMillis()
            val response = try {
                chain.proceed(request)
            } catch (e: Exception) {
                AppLogger.logNetworkError(request.url.toString(), e)
                throw e
            }
            val durationMs      = System.currentTimeMillis() - startMs
            val responseBody    = response.body
            val responseBodyStr = responseBody?.string()

            AppLogger.logResponse(
                url        = request.url.toString(),
                code       = response.code,
                durationMs = durationMs,
                body       = responseBodyStr
            )

            // rebuild — OkHttp body stream can only be read once
            response.newBuilder()
                .body(
                    ResponseBody.create(
                        responseBody?.contentType(),
                        responseBodyStr ?: ""
                    )
                )
                .build()
        }
    }

    // shared OkHttpClient used by both Retrofit instances
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<okhttp3.Interceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit → api.openweathermap.org
    // used for: current weather, daily forecast, geocoding
    single(named("api")) {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(get())
            .addConverterFactory(
                GsonConverterFactory.create(GsonBuilder().setLenient().create())
            )
            .build()
    }

    // Retrofit → pro.openweathermap.org
    // used for: hourly forecast (PRO plan endpoint)
    single(named("pro")) {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_PRO)
            .client(get())
            .addConverterFactory(
                GsonConverterFactory.create(GsonBuilder().setLenient().create())
            )
            .build()
    }

    // WeatherApiService backed by api subdomain
    single(named("api")) {
        get<Retrofit>(named("api")).create(WeatherApiService::class.java)
    }

    // WeatherApiService backed by pro subdomain
    single(named("pro")) {
        get<Retrofit>(named("pro")).create(WeatherApiService::class.java)
    }
}


val repoModule = module {

    single<IRemoteDataSource> {
        RemoteDataSource(
            apiService = get(named("api")),
            proService = get(named("pro"))
        )
    }

    single<ILocalDataSource> {
        LocalDataSource(dao = get())
    }

    single<WeatherRepo> {
        WeatherRepoImpl(
            remote = get(),
            local  = get()
        )
    }

    // ── ViewModels ────────────────────────────────────────────────────────────
    // Koin injects all constructor params automatically via get()

    viewModel {
        SplashViewModel(
            dataStore = get(),
        )
    }

    viewModel {
        HomeViewModel(
            repo      = get(),
            dataStore = get()
        )
    }

   
}