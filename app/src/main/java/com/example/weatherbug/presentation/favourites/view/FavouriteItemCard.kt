package com.example.weatherbug.presentation.favourites.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.weatherbug.R
import com.example.weatherbug.data.models.FavouriteWeatherItem
import com.example.weatherbug.util.WeatherIconMapper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteItemCard(
    item:            FavouriteWeatherItem,
    tempUnitSymbol:  String,
    onSwipedToDelete: () -> Unit,
    onCardClick:     () -> Unit = {},
    modifier:        Modifier = Modifier
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipedToDelete()
            }
            false
        }
    )

    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue != SwipeToDismissBoxValue.Settled) {
            swipeState.reset()
        }
    }

    SwipeToDismissBox(
        state            = swipeState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // Red swipe-reveal background
            val progress  by animateFloatAsState(
                targetValue = if (swipeState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    swipeState.progress else 0f,
                label = "swipe_progress"
            )
            val bgColor by animateColorAsState(
                targetValue = if (progress > 0.05f)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                label = "bg_color"
            )
            val iconScale by animateFloatAsState(
                targetValue = (0.7f + progress * 0.6f).coerceIn(0.7f, 1.3f),
                label = "icon_scale"
            )

            Box(
                modifier          = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(end = 24.dp),
                contentAlignment  = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector        = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.favourites_delete_item),
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier
                        .scale(iconScale)
                        .size(26.dp)
                )
            }
        },
        modifier = modifier
    ) {
        FavouriteCard(
            item           = item,
            tempUnitSymbol = tempUnitSymbol,
            onClick        = onCardClick
        )
    }
}



@Composable
private fun FavouriteCard(
    item:           FavouriteWeatherItem,
    tempUnitSymbol: String,
    onClick:        () -> Unit = {},
    modifier:       Modifier = Modifier
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeatherIconMapper.getIcon(item.icon)?.let { iconRes ->
                androidx.compose.foundation.Image(
                    painter            = androidx.compose.ui.res.painterResource(iconRes),
                    contentDescription = item.description,
                    modifier           = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(6.dp)
                )
            } ?: Box(
                modifier         = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = item.cityName,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CountryChip(country = item.country)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text  = item.description.replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text  = "${item.temp.toInt()}$tempUnitSymbol",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CountryChip(country: String) {
    Box(
        modifier         = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text  = country,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun DeleteOneDialog(
    cityName:  String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.favourites_delete_one_title))
        },
        text = {
            Text(stringResource(R.string.favourites_delete_one_body, cityName))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors  = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text       = stringResource(R.string.favourites_delete_confirm),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.favourites_delete_cancel))
            }
        }
    )
}


@Composable
fun DeleteAllDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.favourites_delete_all_title))
        },
        text = {
            Text(stringResource(R.string.favourites_delete_all_body))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors  = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text       = stringResource(R.string.favourites_delete_all_confirm),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.favourites_delete_all_cancel))
            }
        }
    )
}