package com.example.weatherbug.presentation.home.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weatherbug.R
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.core.util.Constants

private const val STAT_CARD_HEIGHT_DP = 100

@Composable
internal fun WeatherStatsRow(
    data:     WeatherResponse,
    windUnit: String
) {
    val (windValue, windLabel) = when (windUnit) {
        Constants.WIND_UNIT_MPH -> {
            val mph = data.wind.speed * 2.237
            "%.1f".format(mph) to stringResource(R.string.settings_wind_mph)
        }
        Constants.WIND_UNIT_KMH -> {
            val kmh = data.wind.speed * 3.6
            "%.1f".format(kmh) to stringResource(R.string.settings_wind_kmh)
        }
        else -> {
            "%.1f".format(data.wind.speed) to stringResource(R.string.settings_wind_ms)
        }
    }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.Top
    ) {
        StatCard(
            modifier = Modifier.weight(1f).height(STAT_CARD_HEIGHT_DP.dp),
            icon     = R.drawable.ic_stat_humidity,
            label    = stringResource(R.string.home_humidity),
            value    = "${data.main.humidity}%"
        )
        StatCard(
            modifier = Modifier.weight(1f).height(STAT_CARD_HEIGHT_DP.dp),
            icon     = R.drawable.ic_stat_wind,
            label    = stringResource(R.string.home_wind),
            value    = "$windValue $windLabel"
        )
        val pressureIcon = if (data.main.pressure < 1030) {
            R.drawable.ic_stat_pressure_low
        } else {
            R.drawable.ic_stat_pressure_high
        }
        StatCard(
            modifier = Modifier.weight(1f).height(STAT_CARD_HEIGHT_DP.dp),
            icon     = pressureIcon,
            label    = stringResource(R.string.home_pressure),
            value    = "${data.main.pressure} hPa"
        )
        StatCard(
            modifier = Modifier.weight(1f).height(STAT_CARD_HEIGHT_DP.dp),
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
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter            = painterResource(icon),
                    contentDescription = label,
                    modifier           = Modifier.size(32.dp)
                )
                Text(
                    text       = value,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1
                )
                Text(
                    text     = label,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
