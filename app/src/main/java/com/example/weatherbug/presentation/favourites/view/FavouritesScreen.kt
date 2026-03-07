package com.example.weatherbug.presentation.favourites.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.weatherbug.R
import com.example.weatherbug.data.datasource.local.AppDataStore
import com.example.weatherbug.presentation.favourites.viewmodel.FavouritesDialog
import com.example.weatherbug.presentation.favourites.viewmodel.FavouritesViewModel
import com.example.weatherbug.util.Constants
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    onAddFavourite:        () -> Unit,
    onOpenFavouriteDetail: (lat: Double, lon: Double, cityName: String) -> Unit,
    modifier:              Modifier = Modifier
) {
    val viewModel: FavouritesViewModel = koinViewModel()
    val dataStore: AppDataStore        = koinInject()

    val favourites   by viewModel.favourites.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()
    val tempUnit     by dataStore.tempUnitFlow.collectAsState(initial = Constants.UNIT_METRIC)

    val tempSymbol = when (tempUnit) {
        Constants.UNIT_IMPERIAL -> Constants.SYMBOL_FAHRENHEIT
        Constants.UNIT_STANDARD -> Constants.SYMBOL_KELVIN
        else                    -> Constants.SYMBOL_CELSIUS
    }


    when (val dialog = activeDialog) {
        is FavouritesDialog.DeleteOne -> {
            DeleteOneDialog(
                cityName  = dialog.item.cityName,
                onConfirm = { viewModel.confirmDeleteOne() },
                onDismiss = { viewModel.dismissDialog() }
            )
        }
        is FavouritesDialog.DeleteAll -> {
            DeleteAllDialog(
                onConfirm = { viewModel.confirmDeleteAll() },
                onDismiss = { viewModel.dismissDialog() }
            )
        }
        FavouritesDialog.None -> Unit
    }


    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(R.string.nav_favourites),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (favourites.isNotEmpty()) {
                        IconButton(onClick = { viewModel.requestDeleteAll() }) {
                            Icon(
                                imageVector        = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.favourites_delete_all_title),
                                tint               = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onAddFavourite,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector        = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.favourites_add)
                )
            }
        }
    ) { innerPadding ->

        if (favourites.isEmpty()) {
            FavouritesEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = favourites,
                    key   = { it.id }
                ) { item ->
                    FavouriteItemCard(
                        item             = item,
                        tempUnitSymbol   = tempSymbol,
                        onSwipedToDelete = { viewModel.requestDeleteOne(item) },
                        onCardClick      = {
                            onOpenFavouriteDetail(item.lat, item.lon, item.cityName)
                        },
                        modifier         = Modifier.fillMaxWidth()
                    )
                }

                // Extra space so FAB never covers the last card
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}


@Composable
private fun FavouritesEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier           = Modifier.size(72.dp),
                tint               = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text       = stringResource(R.string.favourites_empty_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text      = stringResource(R.string.favourites_empty_subtitle),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}