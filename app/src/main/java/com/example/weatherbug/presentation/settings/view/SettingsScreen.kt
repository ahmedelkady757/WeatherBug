package com.example.weatherbug.presentation.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherbug.R
import com.example.weatherbug.presentation.location.LocationViewModel
import com.example.weatherbug.presentation.settings.viewmodel.SettingsNavEvent
import com.example.weatherbug.presentation.settings.viewmodel.SettingsViewModel
import com.example.weatherbug.core.util.Constants
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun SettingsScreen(
    locationViewModel:     LocationViewModel,
    onNavigateToMapPicker: () -> Unit
) {
    val viewModel: SettingsViewModel = koinViewModel(
        parameters = { parametersOf(locationViewModel) }
    )

    val theme        by viewModel.theme.collectAsStateWithLifecycle()
    val language     by viewModel.language.collectAsStateWithLifecycle()
    val tempUnit     by viewModel.tempUnit.collectAsStateWithLifecycle()
    val windUnit     by viewModel.windUnit.collectAsStateWithLifecycle()
    val locationMode by viewModel.locationMode.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is SettingsNavEvent.NavigateToMapPicker -> onNavigateToMapPicker()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text       = stringResource(R.string.settings_title),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsCard(title = stringResource(R.string.settings_theme)) {
            SegmentedRow(
                options = listOf(
                    Constants.THEME_LIGHT to stringResource(R.string.settings_theme_light),
                    Constants.THEME_DARK  to stringResource(R.string.settings_theme_dark)
                ),
                selected  = theme,
                onSelect  = viewModel::setTheme
            )
        }

        SettingsCard(title = stringResource(R.string.settings_language)) {
            SegmentedRow(
                options = listOf(
                    Constants.LANG_DEVICE  to stringResource(R.string.settings_lang_device),
                    Constants.LANG_ENGLISH to stringResource(R.string.settings_lang_english),
                    Constants.LANG_ARABIC  to stringResource(R.string.settings_lang_arabic)
                ),
                selected = language,
                onSelect = viewModel::setLanguage
            )
        }

        SettingsCard(title = stringResource(R.string.settings_temp_unit)) {
            SegmentedRow(
                options = listOf(
                    Constants.UNIT_METRIC   to stringResource(R.string.settings_unit_celsius),
                    Constants.UNIT_IMPERIAL to stringResource(R.string.settings_unit_fahrenheit),
                    Constants.UNIT_STANDARD to stringResource(R.string.settings_unit_kelvin)
                ),
                selected = tempUnit,
                onSelect = viewModel::setTempUnit
            )
        }

        SettingsCard(title = stringResource(R.string.settings_wind_unit)) {
            SegmentedRow(
                options = listOf(
                    Constants.WIND_UNIT_MS  to stringResource(R.string.settings_wind_ms),
                    Constants.WIND_UNIT_MPH to stringResource(R.string.settings_wind_mph),
                    Constants.WIND_UNIT_KMH to stringResource(R.string.settings_wind_kmh)
                ),
                selected = windUnit,
                onSelect = viewModel::setWindUnit
            )
        }

        SettingsCard(title = stringResource(R.string.settings_location_mode)) {
            SegmentedRow(
                options = listOf(
                    Constants.LOCATION_GPS to stringResource(R.string.settings_location_gps),
                    Constants.LOCATION_MAP to stringResource(R.string.settings_location_map)
                ),
                selected = locationMode,
                onSelect = viewModel::setLocationMode
            )
        }
    }
}


@Composable
private fun SettingsCard(
    title:   String,
    content: @Composable () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}



@Composable
private fun SegmentedRow(
    options:  List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                modifier  = Modifier.weight(1f),
                selected  = selected == value,
                onClick   = { if (selected != value) onSelect(value) },
                label     = {
                    Text(
                        text      = label,
                        style     = MaterialTheme.typography.labelMedium,
                        maxLines  = 1
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor    = MaterialTheme.colorScheme.primary,
                    selectedLabelColor        = MaterialTheme.colorScheme.onPrimary,
                    containerColor            = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor                = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}