package com.example.weatherbug.di

import androidx.room.Room
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.data.datasource.local.ILocalDataSource
import com.example.weatherbug.data.datasource.local.LocalDataSource
import com.example.weatherbug.data.datasource.remote.IRemoteDataSource
import com.example.weatherbug.data.datasource.remote.RemoteDataSource
import com.example.weatherbug.data.db.WeatherBugDatabase
import com.example.weatherbug.data.network.RetrofitClient
import com.example.weatherbug.data.repo.WeatherRepo
import com.example.weatherbug.data.repo.WeatherRepoImpl
import com.example.weatherbug.presentation.favourites.viewmodel.FavouriteDetailViewModel
import com.example.weatherbug.presentation.favourites.viewmodel.FavouritesViewModel
import com.example.weatherbug.presentation.home.viewmodel.HomeViewModel
import com.example.weatherbug.presentation.location.LocationViewModel
import com.example.weatherbug.presentation.map.viewmodel.MapPickerViewModel
import com.example.weatherbug.presentation.settings.viewmodel.SettingsViewModel
import com.example.weatherbug.presentation.splash.viewmodel.SplashViewModel
import com.example.weatherbug.util.Constants
import com.example.weatherbug.location.FusedLocationProvider
import com.example.weatherbug.location.LocationProvider
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {

    single<IAppDataStore> { AppDataStore(androidContext()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            WeatherBugDatabase::class.java,
            Constants.DB_NAME
        ).build()
    }

    single { get<WeatherBugDatabase>().weatherBugDao() }

    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    single<LocationProvider> { FusedLocationProvider(client = get()) }
}


val networkModule = module {


    single(named("api")) {
        RetrofitClient.apiService
    }

    single(named("pro")) {
        RetrofitClient.proService
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


    viewModel {
        SplashViewModel(appDataStore = get())
    }

    viewModel {
        LocationViewModel(
            dataStore        = get(),
            locationProvider = get()
        )
    }

    viewModel {
        HomeViewModel(
            repo      = get(),
            dataStore = get()
        )
    }

    viewModel { (locationViewModel: LocationViewModel) ->
        SettingsViewModel(
            dataStore    = get(),
            onRefreshGps = locationViewModel::refreshLocation
        )
    }


    viewModel {
        FavouritesViewModel(repo = get())
    }
    viewModel { (lat: Double, lon: Double) ->
        FavouriteDetailViewModel(
            lat = lat,
            lon = lon,
            repo = get(),
            dataStore = get()
        )
    }

    viewModel {
        MapPickerViewModel(
            repo = get(),
            dataStore = get()
        )
    }
}