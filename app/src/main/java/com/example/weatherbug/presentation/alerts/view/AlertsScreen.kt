package com.example.weatherbug.presentation.alerts.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherbug.R
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.presentation.alerts.viewmodel.AlertsDialog
import com.example.weatherbug.presentation.alerts.viewmodel.AlertsViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(modifier: Modifier = Modifier) {

    val viewModel: AlertsViewModel = koinViewModel()
    val alerts      by viewModel.alerts.collectAsState()
    val activeDialog by viewModel.dialog.collectAsState()

    Scaffold(
        modifier    = modifier,
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = stringResource(R.string.alerts_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (alerts.isNotEmpty()) {
                        IconButton(onClick = viewModel::requestDeleteAll) {
                            Icon(
                                imageVector        = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.alerts_delete_all)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::requestAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.alerts_add))
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (alerts.isEmpty()) {
                AlertsEmptyState()
            } else {
                LazyColumn(
                    contentPadding     = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        SwipeToDeleteAlertCard(
                            alert    = alert,
                            onDelete = { viewModel.requestDeleteOne(alert) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }


    when (val d = activeDialog) {
        is AlertsDialog.Add -> {
            AddAlertDialog(
                onConfirm = { start, end, type, condition ->
                    viewModel.confirmAdd(start, end, type, condition)
                },
                onDismiss = viewModel::dismissDialog
            )
        }

        is AlertsDialog.DeleteOne -> {
            AlertDialog(
                onDismissRequest = viewModel::dismissDialog,
                title = { Text(stringResource(R.string.alerts_delete_one_title), fontWeight = FontWeight.Bold) },
                text  = { Text(stringResource(R.string.alerts_delete_one_body)) },
                confirmButton = {
                    Button(onClick = viewModel::confirmDeleteOne) {
                        Text(stringResource(R.string.alerts_delete_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissDialog) {
                        Text(stringResource(R.string.alerts_delete_cancel))
                    }
                }
            )
        }

        is AlertsDialog.DeleteAll -> {
            AlertDialog(
                onDismissRequest = viewModel::dismissDialog,
                title = { Text(stringResource(R.string.alerts_delete_all_title), fontWeight = FontWeight.Bold) },
                text  = { Text(stringResource(R.string.alerts_delete_all_body)) },
                confirmButton = {
                    Button(onClick = viewModel::confirmDeleteAll) {
                        Text(stringResource(R.string.alerts_delete_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissDialog) {
                        Text(stringResource(R.string.alerts_delete_cancel))
                    }
                }
            )
        }

        is AlertsDialog.None -> Unit
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteAlertCard(
    alert:    AlertItem,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    // Reset if the delete was cancelled by the confirm dialog
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state            = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(300),
                label = "swipe_bg"
            )
            Box(
                modifier          = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(16.dp))
                    .padding(end = 20.dp),
                contentAlignment  = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(28.dp)
                )
            }
        }
    ) {
        AlertItemCard(alert = alert)
    }
}



@Composable
private fun AlertItemCard(alert: AlertItem) {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isAlarm = alert.alarmType == AlertItem.ALARM_TYPE_ALARM

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier        = Modifier.padding(16.dp),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            // Icon indicating type
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isAlarm)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector        = if (isAlarm) Icons.Default.Notifications
                                        else          Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint               = if (isAlarm) MaterialTheme.colorScheme.onErrorContainer
                                         else          MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = if (isAlarm) stringResource(R.string.alerts_type_alarm)
                                 else         stringResource(R.string.alerts_type_notification),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = conditionLabel(alert.weatherCondition),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = fmt.format(alert.startTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



@Composable
private fun AlertsEmptyState() {
    Column(
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = Icons.Outlined.Notifications,
            contentDescription = null,
            modifier           = Modifier.size(80.dp),
            tint               = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text       = stringResource(R.string.alerts_empty_title),
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = stringResource(R.string.alerts_empty_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
private fun conditionLabel(condition: String): String = when (condition) {
    AlertItem.CONDITION_RAIN         -> stringResource(R.string.condition_rain)
    AlertItem.CONDITION_SNOW         -> stringResource(R.string.condition_snow)
    AlertItem.CONDITION_THUNDERSTORM -> stringResource(R.string.condition_thunderstorm)
    AlertItem.CONDITION_CLEAR        -> stringResource(R.string.condition_clear)
    AlertItem.CONDITION_CLOUDS       -> stringResource(R.string.condition_clouds)
    AlertItem.CONDITION_FOG          -> stringResource(R.string.condition_fog)
    else                             -> stringResource(R.string.condition_any)
}
