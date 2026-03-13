package com.example.weatherbug.presentation.alerts.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import com.example.weatherbug.R
import com.example.weatherbug.data.models.AlertItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class PickerTarget { START, END }
private enum class PickerStep  { DATE, TIME, NONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertDialog(
    onConfirm: (startTime: Long, endTime: Long, alarmType: String, weatherCondition: String) -> Unit,
    onDismiss: () -> Unit
) {
    val fmt = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())

    var startTime        by remember { mutableLongStateOf(System.currentTimeMillis() + 60_000L) }
    var endTime          by remember { mutableLongStateOf(System.currentTimeMillis() + 3_600_000L) }
    var alarmType        by remember { mutableStateOf(AlertItem.ALARM_TYPE_NOTIFICATION) }
    var weatherCondition by remember { mutableStateOf(AlertItem.CONDITION_ANY) }

    var pickerTarget by remember { mutableStateOf(PickerTarget.START) }
    var pickerStep   by remember { mutableStateOf(PickerStep.NONE) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startTime)
    val initialCal      = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour   = initialCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCal.get(Calendar.MINUTE),
        is24Hour      = true
    )

    if (pickerStep == PickerStep.DATE) {
        DatePickerDialog(
            onDismissRequest = { pickerStep = PickerStep.NONE },
            confirmButton = {
                TextButton(onClick = { pickerStep = PickerStep.TIME }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { pickerStep = PickerStep.NONE }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (pickerStep == PickerStep.TIME) {
        Dialog(onDismissRequest = { pickerStep = PickerStep.NONE }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = if (pickerTarget == PickerTarget.START)
                                         stringResource(R.string.alerts_dialog_start)
                                     else stringResource(R.string.alerts_dialog_end),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { pickerStep = PickerStep.NONE }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        TextButton(onClick = {
                            val selectedDateMs = datePickerState.selectedDateMillis
                                ?: System.currentTimeMillis()
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = selectedDateMs
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE,      timePickerState.minute)
                                set(Calendar.SECOND,      0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            if (pickerTarget == PickerTarget.START) startTime = cal.timeInMillis
                            else                                     endTime   = cal.timeInMillis
                            pickerStep = PickerStep.NONE
                        }) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text       = stringResource(R.string.alerts_dialog_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text(
                    text  = stringResource(R.string.alerts_dialog_condition),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                val conditionLabels = mapOf(
                    AlertItem.CONDITION_ANY          to stringResource(R.string.condition_any),
                    AlertItem.CONDITION_RAIN         to stringResource(R.string.condition_rain),
                    AlertItem.CONDITION_SNOW         to stringResource(R.string.condition_snow),
                    AlertItem.CONDITION_THUNDERSTORM to stringResource(R.string.condition_thunderstorm),
                    AlertItem.CONDITION_CLEAR        to stringResource(R.string.condition_clear),
                    AlertItem.CONDITION_CLOUDS       to stringResource(R.string.condition_clouds),
                    AlertItem.CONDITION_FOG          to stringResource(R.string.condition_fog)
                )
                Row(
                    modifier              = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AlertItem.ALL_CONDITIONS.forEach { cond ->
                        FilterChip(
                            selected = weatherCondition == cond,
                            onClick  = { weatherCondition = cond },
                            label    = { Text(conditionLabels[cond] ?: cond) }
                        )
                    }
                }

                Text(
                    text  = stringResource(R.string.alerts_dialog_start),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick  = { pickerTarget = PickerTarget.START; pickerStep = PickerStep.DATE },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(fmt.format(startTime)) }

                Text(
                    text  = stringResource(R.string.alerts_dialog_end),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick  = { pickerTarget = PickerTarget.END; pickerStep = PickerStep.DATE },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(fmt.format(endTime)) }

                Text(
                    text  = stringResource(R.string.alerts_dialog_type),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = alarmType == AlertItem.ALARM_TYPE_NOTIFICATION,
                        onClick  = { alarmType = AlertItem.ALARM_TYPE_NOTIFICATION },
                        label    = { Text(stringResource(R.string.alerts_type_notification)) }
                    )
                    FilterChip(
                        selected = alarmType == AlertItem.ALARM_TYPE_ALARM,
                        onClick  = { alarmType = AlertItem.ALARM_TYPE_ALARM },
                        label    = { Text(stringResource(R.string.alerts_type_alarm)) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(startTime, endTime, alarmType, weatherCondition) },
                enabled = startTime > System.currentTimeMillis() && endTime > startTime
            ) { Text(stringResource(R.string.alerts_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.alerts_dialog_cancel))
            }
        }
    )
}
