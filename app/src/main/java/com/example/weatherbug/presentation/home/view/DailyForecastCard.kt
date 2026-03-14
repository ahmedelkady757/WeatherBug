package com.example.weatherbug.presentation.home.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.weatherbug.R
import com.example.weatherbug.data.models.DailyForecastResponse
import com.example.weatherbug.core.util.DateFormatter
import com.example.weatherbug.core.util.WeatherIconMapper
import kotlin.math.roundToInt

@Composable
internal fun DailyForecastCard(
    items:   List<DailyForecastResponse.DailyItem>,
    appLang: String
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = stringResource(R.string.home_daily_forecast),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,

                )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, item ->
                DailyItem(item = item, appLang = appLang)
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
private fun DailyItem(
    item:    DailyForecastResponse.DailyItem,
    appLang: String
) {
    val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
    val dayName  = DateFormatter.formatDayName(item.dt, appLang)

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = dayName,
            style      = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,

            fontWeight = FontWeight.Medium,
            modifier   = Modifier.width(80.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter            = painterResource(WeatherIconMapper.getIcon(iconCode)),
            contentDescription = null,
            modifier           = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text      = "${item.temp.min.roundToInt()}°",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier  = Modifier.width(36.dp)
        )
        Text(
            text  = " ~ ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = "${item.temp.max.roundToInt()}°",
            style      = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,

            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.width(36.dp)
        )
    }
}
