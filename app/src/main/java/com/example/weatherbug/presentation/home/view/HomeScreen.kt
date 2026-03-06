package com.example.weatherbug.presentation.home.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbug.MainActivity
import com.example.weatherbug.R
import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.presentation.home.viewmodel.HomeViewModel
import com.example.weatherbug.presentation.home.viewmodel.HomeViewModelFactory
import com.example.weatherbug.presentation.location.LocationState
import com.example.weatherbug.util.ResponseState
import com.example.weatherbug.util.WeatherIconMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen() {
    val context  = LocalContext.current
    val activity = context as MainActivity

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(context)
    )

    val locationState    by activity.locationViewModel.locationState.collectAsStateWithLifecycle()
    val currentWeather   by homeViewModel.currentWeatherState.collectAsStateWithLifecycle()
    val hourlyState      by homeViewModel.hourlyState.collectAsStateWithLifecycle()
    val dailyState       by homeViewModel.dailyState.collectAsStateWithLifecycle()

    LaunchedEffect(locationState) {
        if (locationState is LocationState.Success) {
            val loc = locationState as LocationState.Success
            homeViewModel.loadWeather(loc.lat, loc.lon)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        when (val state = currentWeather) {
            is ResponseState.Loading -> LoadingCard(modifier = Modifier.height(320.dp))
            is ResponseState.Failure -> ErrorCard(
                message = state.errorMessage,
                onRetry = { homeViewModel.retry() }
            )
            is ResponseState.Success -> {
                CurrentWeatherCard(data = state.data)
                StatsRow(data = state.data)
            }
        }

        when (val state = hourlyState) {
            is ResponseState.Loading -> LoadingCard(modifier = Modifier.height(120.dp))
            is ResponseState.Failure -> Unit // silent fail — not critical
            is ResponseState.Success -> HourlyForecastRow(items = state.data.list.take(24))
        }

        when (val state = dailyState) {
            is ResponseState.Loading -> LoadingCard(modifier = Modifier.height(240.dp))
            is ResponseState.Failure -> Unit // silent fail — not critical
            is ResponseState.Success -> DailyForecastList(items = state.data.list)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
private fun CurrentWeatherCard(data: WeatherResponse) {
    val condition = data.weather.firstOrNull()

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // location + date row
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint               = Color.White.copy(alpha = 0.9f),
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text  = "${data.name}, ${data.sys.country ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text  = formatDate(data.dt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                condition?.icon?.let { iconCode ->
                    Image(
                        painter            = painterResource(WeatherIconMapper.getIcon(iconCode)),
                        contentDescription = condition.description,
                        modifier           = Modifier.size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text       = "${data.main.temp.roundToInt()}°",
                    fontSize   = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    lineHeight = 80.sp
                )

                Text(
                    text       = condition?.description?.replaceFirstChar { it.uppercase() } ?: "",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text  = stringResource(
                        R.string.home_feels_like,
                        data.main.feelsLike.roundToInt()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}


@Composable
private fun StatsRow(data: WeatherResponse) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon     = R.drawable.ic_stat_humidity,
            label    = stringResource(R.string.home_humidity),
            value    = "${data.main.humidity}%"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon     = R.drawable.ic_stat_wind,
            label    = stringResource(R.string.home_wind),
            value    = "${data.wind.speed} m/s"
        )
        data.main.pressure?.let { pressure ->
            if (pressure>1030)
            StatCard(
                modifier = Modifier.weight(1f),
                icon     = R.drawable.ic_stat_pressure_high,
                label    = stringResource(R.string.home_pressure),
                value    = "$pressure hPa"
            )
            else{
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon     = R.drawable.ic_stat_pressure_low,
                    label    = stringResource(R.string.home_pressure),
                    value    = "$pressure mmHg"
                )
            }
        }
        StatCard(
            modifier = Modifier.weight(1f),
            icon     = R.drawable.ic_stat_clouds,
            label    = stringResource(R.string.home_clouds),
            value    = "${data.clouds.all}%"
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon:     Int,
    label:    String,
    value:    String
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter            = painterResource(icon),
                contentDescription = label,
                modifier           = Modifier.size(32.dp)
            )
            Text(
                text      = value,
                style     = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color     = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun HourlyForecastRow(items: List<HourlyForecastResponse.HourlyItem>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text       = stringResource(R.string.home_hourly_forecast),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding      = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    HourlyItem(item = item)
                }
            }
        }
    }
}

@Composable
private fun HourlyItem(item: HourlyForecastResponse.HourlyItem) {
    val iconCode  = item.weather.firstOrNull()?.icon ?: "01d"
    val timeLabel = item.dtTxt.substring(11, 16) // "09:00"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier            = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text  = timeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Image(
            painter            = painterResource(WeatherIconMapper.getIcon(iconCode)),
            contentDescription = null,
            modifier           = Modifier.size(36.dp)
        )
        Text(
            text       = "${item.main.temp.roundToInt()}°",
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun DailyForecastList(items: List<DailyForecastResponse.DailyItem>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = stringResource(R.string.home_daily_forecast),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, item ->
                DailyItem(item = item)
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier  = Modifier.padding(vertical = 8.dp),
                        color     = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyItem(item: DailyForecastResponse.DailyItem) {
    val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
    val dayName  = formatDayName(item.dt)

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // day name
        Text(
            text      = dayName,
            style     = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier  = Modifier.width(80.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // weather icon
        Image(
            painter            = painterResource(WeatherIconMapper.getIcon(iconCode)),
            contentDescription = null,
            modifier           = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // min ~ max temp
        Text(
            text      = "${item.temp.min.roundToInt()}°",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier  = Modifier.width(36.dp)
        )
        Text(
            text      = " ~ ",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text      = "${item.temp.max.roundToInt()}°",
            style     = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier  = Modifier.width(36.dp)
        )
    }
}


@Composable
private fun LoadingCard(modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}


@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            IconButton(onClick = onRetry) {
                Icon(
                    imageVector        = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.home_retry),
                    tint               = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}


private fun formatDate(dt: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM d • HH:mm", Locale.getDefault())
    return sdf.format(Date(dt * 1000))
}

private fun formatDayName(dt: Long): String {
    val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
    return sdf.format(Date(dt * 1000))
}