package com.example.weatherbug.presentation.favourites.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherbug.R
import com.example.weatherbug.presentation.favourites.viewmodel.FavouriteDetailViewModel
import com.example.weatherbug.presentation.home.view.WeatherContent
import com.example.weatherbug.util.Constants
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteDetailScreen(
    lat:            Double,
    lon:            Double,
    cityName:       String,
    onNavigateBack: () -> Unit
) {
    val viewModel: FavouriteDetailViewModel = koinViewModel(
        parameters = { parametersOf(lat, lon) }
    )

    val appLanguage    by viewModel.appLanguage.collectAsStateWithLifecycle()
    val tempUnit       by viewModel.tempUnit.collectAsStateWithLifecycle()
    val currentWeather by viewModel.currentWeatherState.collectAsStateWithLifecycle()
    val hourlyState    by viewModel.hourlyState.collectAsStateWithLifecycle()
    val dailyState     by viewModel.dailyState.collectAsStateWithLifecycle()
    val windUnitLabel = if (tempUnit == Constants.UNIT_IMPERIAL)
        Constants.WIND_IMPERIAL_LABEL
    else
        Constants.WIND_METRIC_IMPERIAL_LABEL
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = cityName.ifBlank { stringResource(R.string.nav_favourites) },
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.map_picker_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.retry() }) {
                        Icon(
                            imageVector        = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.home_retry)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            WeatherContent(
                currentWeatherState = currentWeather,
                hourlyState         = hourlyState,
                dailyState          = dailyState,
                appLanguage         = appLanguage,
                windUnitLabel       = windUnitLabel,
                onRetry             = { viewModel.retry() }
            )
        }
    }
}
