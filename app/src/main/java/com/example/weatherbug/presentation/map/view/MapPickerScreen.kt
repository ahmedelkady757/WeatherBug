package com.example.weatherbug.presentation.map.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherbug.R
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.data.models.GeocodingItem
import com.example.weatherbug.core.navigation.Screen
import com.example.weatherbug.presentation.map.viewmodel.MapPickerEvent
import com.example.weatherbug.presentation.map.viewmodel.MapPickerViewModel
import com.example.weatherbug.core.util.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    mode:            String,
    onNavigateBack:  () -> Unit
) {
    val viewModel: MapPickerViewModel = koinViewModel()
    val dataStore: IAppDataStore       = koinInject()

    val selectedLatLng  by viewModel.selectedLatLng.collectAsStateWithLifecycle()
    val resolvedCity    by viewModel.resolvedCityName.collectAsStateWithLifecycle()
    val isGeocoding     by viewModel.isGeocodingName.collectAsStateWithLifecycle()
    val isConfirming    by viewModel.isConfirming.collectAsStateWithLifecycle()
    val searchResults   by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching     by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchError     by viewModel.searchError.collectAsStateWithLifecycle()
    val appLanguage     by dataStore.languageFlow.collectAsStateWithLifecycle(
        initialValue = Constants.LANG_ENGLISH
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MapPickerEvent.NavigateBack -> onNavigateBack()
                is MapPickerEvent.ShowError    -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val initialLatLng = selectedLatLng ?: LatLng(Constants.FALLBACK_LAT, Constants.FALLBACK_LON)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 4f)
    }

    LaunchedEffect(selectedLatLng) {
        selectedLatLng?.let { latLng ->
            cameraPositionState.animate(
                update     = CameraUpdateFactory.newLatLngZoom(latLng, 8f),
                durationMs = 600
            )
        }
    }

    val title = when (mode) {
        Screen.MapPicker.MODE_FAVOURITE -> stringResource(R.string.map_picker_title_favourite)
        else                            -> stringResource(R.string.map_picker_title_settings)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = title,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            GoogleMap(
                modifier            = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties          = MapProperties(),
                uiSettings          = MapUiSettings(
                    zoomControlsEnabled      = false,
                    myLocationButtonEnabled  = false,
                    compassEnabled           = true
                ),
                onMapClick          = { latLng ->
                    viewModel.onMapTapped(latLng.latitude, latLng.longitude)
                }
            ) {
                selectedLatLng?.let { latLng ->
                    Marker(
                        state = MarkerState(position = latLng)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var query by rememberSaveable { mutableStateOf("") }
                var showSuggestions by rememberSaveable { mutableStateOf(true) }

                SearchTextField(
                    query          = query,
                    onQueryChange  = { newValue ->
                        query = newValue
                        showSuggestions = true
                        viewModel.onSearchQueryChanged(newValue)
                    }
                )

                if (showSuggestions &&
                    (searchResults.isNotEmpty() || isSearching || searchError != null)
                ) {
                    SearchResultsCard(
                        items        = searchResults,
                        appLanguage  = appLanguage,
                        isSearching  = isSearching,
                        errorMessage = searchError,
                        onItemClick  = { item ->
                            val label = buildPlaceTitle(item, appLanguage)
                            viewModel.onPlaceSelected(item, appLanguage)
                            query           = label
                            showSuggestions = false
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = stringResource(R.string.map_picker_tap_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                if (resolvedCity.isNotBlank()) {
                    Text(
                        text       = resolvedCity,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else if (isGeocoding) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(top = 4.dp),
                        strokeWidth = 2.dp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                MapConfirmButton(
                    enabled      = selectedLatLng != null && !isConfirming,
                    isLoading    = isConfirming,
                    onClick      = { viewModel.confirmPick(mode) }
                )
            }
        }
    }
}


@Composable
private fun SearchTextField(
    query:         String,
    onQueryChange: (String) -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    ) {
        OutlinedTextField(
            value         = query,
            onValueChange = onQueryChange,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            singleLine    = true,
            leadingIcon   = {
                Icon(
                    imageVector        = Icons.Filled.Search,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            placeholder   = {
                Text(
                    text  = stringResource(R.string.map_picker_search_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            textStyle     = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}


@Composable
private fun SearchResultsCard(
    items:        List<GeocodingItem>,
    appLanguage:  String,
    isSearching:  Boolean,
    errorMessage: String?,
    onItemClick:  (GeocodingItem) -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth(),
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            if (isSearching) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else if (errorMessage != null) {
                Text(
                    text      = errorMessage,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.error,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (items.isNotEmpty()) {
                LazyColumn {
                    items(items) { item ->
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text       = buildPlaceTitle(item, appLanguage),
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
                                val subtitle = buildPlaceSubtitle(item)
                                if (subtitle.isNotBlank()) {
                                    Text(
                                        text      = subtitle,
                                        style     = MaterialTheme.typography.bodySmall,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines  = 1,
                                        overflow  = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun MapConfirmButton(
    enabled:   Boolean,
    isLoading: Boolean,
    onClick:   () -> Unit
) {
    androidx.compose.material3.Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(22.dp),
                color       = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text       = stringResource(R.string.map_picker_confirm),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


private fun buildPlaceTitle(item: GeocodingItem, lang: String): String {
    val localized = item.localizedName(lang)
    return if (item.country.isNotBlank()) {
        "$localized, ${item.country}"
    } else {
        localized
    }
}


private fun buildPlaceSubtitle(item: GeocodingItem): String {
    val parts = mutableListOf<String>()
    item.state?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    return parts.joinToString(separator = " • ")
}

