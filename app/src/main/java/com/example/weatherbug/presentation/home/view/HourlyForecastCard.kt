package com.example.weatherbug.presentation.home.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.weatherbug.data.models.HourlyForecastResponse
import com.example.weatherbug.core.util.DateFormatter
import com.example.weatherbug.core.util.WeatherIconMapper
import kotlin.math.roundToInt

@Composable
internal fun HourlyForecastCard(items: List<HourlyForecastResponse.HourlyItem>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text       = stringResource(R.string.home_hourly_forecast),
                style      = MaterialTheme.typography.titleMedium,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
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
    val timeLabel = DateFormatter.formatHourly(
        item.dtTxt,
        am = stringResource(R.string.home_am),
        pm = stringResource(R.string.home_pm)
    )

    Card(
        shape     = RoundedCornerShape(20.dp),
        border    = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
        ),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f)
        ),
        modifier  = Modifier.width(90.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier            = Modifier.padding(horizontal = 14.dp, vertical = 18.dp)
        ) {
            Text(
                text       = timeLabel,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Image(
                painter            = painterResource(WeatherIconMapper.getIcon(iconCode)),
                contentDescription = null,
                modifier           = Modifier.size(56.dp)
            )
            Text(
                text       = "${item.main.temp.roundToInt()}°",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
