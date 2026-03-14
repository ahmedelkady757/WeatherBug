package com.example.weatherbug.presentation.home.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherbug.R
import com.example.weatherbug.data.models.WeatherResponse
import com.example.weatherbug.core.util.DateFormatter
import com.example.weatherbug.core.util.WeatherIconMapper
import kotlin.math.roundToInt

@Composable
internal fun CurrentWeatherCard(
    data:             WeatherResponse,
    appLang:          String,
    currentTimeEpoch: Long
) {
    val condition = data.weather.firstOrNull()
    val amLabel   = stringResource(R.string.home_am)
    val pmLabel   = stringResource(R.string.home_pm)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    text       = "${data.name}, ${data.sys.country ?: ""}",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text  = DateFormatter.formatDate(currentTimeEpoch, appLang, amLabel, pmLabel),
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

            // ── Feels like ────────────────────────────────────────────────────
            Text(
                text  = stringResource(R.string.home_feels_like, data.main.feelsLike.roundToInt()),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}
