package com.example.weatherbug.presentation.home.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.presentation.home.viewmodel.HomeViewModel
import com.example.weatherbug.presentation.location.LocationViewModel
import com.example.weatherbug.core.util.ResponseState
import com.example.weatherbug.core.util.NoInternetScreen
import com.example.weatherbug.core.util.isNoInternetError
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeScreen(
    locationViewModel: LocationViewModel
) {
    val viewModel: HomeViewModel = koinViewModel()

    val currentWeather by viewModel.currentWeatherState.collectAsStateWithLifecycle()
    val hourlyState    by viewModel.hourlyState.collectAsStateWithLifecycle()
    val dailyState     by viewModel.dailyState.collectAsStateWithLifecycle()
    val appLang        by viewModel.appLanguage.collectAsStateWithLifecycle()
    val tempUnit       by viewModel.tempUnit.collectAsStateWithLifecycle()
    val windUnit       by viewModel.windUnit.collectAsStateWithLifecycle()

    WeatherContent(
        currentWeatherState = currentWeather,
        hourlyState         = hourlyState,
        dailyState          = dailyState,
        appLanguage         = appLang,
        windUnit            = windUnit,
        onRetry             = { viewModel.retry() }
    )
}


@Composable
fun WeatherContent(
    currentWeatherState: ResponseState<WeatherResponse>,
    hourlyState:         ResponseState<HourlyForecastResponse>,
    dailyState:          ResponseState<DailyForecastResponse>,
    appLanguage:         String,
    windUnit:            String,
    onRetry:             () -> Unit,
    modifier:            Modifier = Modifier
) {
    if (currentWeatherState is ResponseState.Failure &&
        isNoInternetError(currentWeatherState.errorMessage)
    ) {
        NoInternetScreen(modifier = modifier.fillMaxSize(), onRetry = onRetry)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val state = currentWeatherState) {
            is ResponseState.Loading -> LoadingCard(modifier = Modifier.height(320.dp))
            is ResponseState.Failure -> ErrorCard(message = state.errorMessage, onRetry = onRetry)
            is ResponseState.Success -> {
                CurrentWeatherCard(
                    data             = state.data,
                    appLang          = appLanguage,
                    currentTimeEpoch = System.currentTimeMillis() / 1000
                )
                WeatherStatsRow(
                    data     = state.data,
                    windUnit = windUnit
                )
            }
        }

        when (val state = hourlyState) {
            is ResponseState.Loading -> LoadingCard(modifier = Modifier.height(160.dp))
            is ResponseState.Failure -> Unit
            is ResponseState.Success -> HourlyForecastCard(items = state.data.list.take(24))
        }

        when (val state = dailyState) {
            is ResponseState.Loading -> LoadingCard(modifier = Modifier.height(240.dp))
            is ResponseState.Failure -> Unit
            is ResponseState.Success -> DailyForecastCard(items = state.data.list, appLang = appLanguage)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}