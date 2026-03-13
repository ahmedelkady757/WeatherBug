package com.example.weatherbug.presentation.alerts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbug.core.alerts.AlarmScheduler
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.repo.WeatherRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar


sealed class AlertsDialog {
    data object None                           : AlertsDialog()
    data object Add                            : AlertsDialog()
    data class  DeleteOne(val item: AlertItem) : AlertsDialog()
    data object DeleteAll                      : AlertsDialog()
}


class AlertsViewModel(
    private val repo:      WeatherRepo,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    val alerts: StateFlow<List<AlertItem>> = repo
        .getAllAlerts()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _dialog = MutableStateFlow<AlertsDialog>(AlertsDialog.None)
    val dialog: StateFlow<AlertsDialog> = _dialog.asStateFlow()

    fun requestAdd()                        { _dialog.value = AlertsDialog.Add }
    fun requestDeleteOne(item: AlertItem)   { _dialog.value = AlertsDialog.DeleteOne(item) }
    fun requestDeleteAll()                  { _dialog.value = AlertsDialog.DeleteAll }
    fun dismissDialog()                     { _dialog.value = AlertsDialog.None }

    fun confirmAdd(
        startTime:        Long,
        endTime:          Long,
        alarmType:        String,
        weatherCondition: String
    ) {
        viewModelScope.launch {
            val newCal = Calendar.getInstance().apply { timeInMillis = startTime }
            val newHour = newCal.get(Calendar.HOUR_OF_DAY)
            val newMin  = newCal.get(Calendar.MINUTE)

            // Prevent two alerts with the exact same time
            alerts.value.find {
                val cal = Calendar.getInstance().apply { timeInMillis = it.startTime }
                cal.get(Calendar.HOUR_OF_DAY) == newHour && cal.get(Calendar.MINUTE) == newMin
            }?.let { existing ->
                scheduler.cancel(existing.id, existing.alarmType, existing.weatherCondition)
                repo.deleteAlert(existing)
            }

            val draft = AlertItem(
                startTime        = startTime,
                endTime          = endTime,
                alarmType        = alarmType,
                weatherCondition = weatherCondition
            )
            val insertedId = repo.insertAlert(draft).toInt()
            val saved      = draft.copy(id = insertedId)
            scheduler.schedule(saved)
            _dialog.value = AlertsDialog.None
        }
    }

    fun confirmDeleteOne() {
        val state = _dialog.value as? AlertsDialog.DeleteOne ?: return
        viewModelScope.launch {
            scheduler.cancel(state.item.id, state.item.alarmType, state.item.weatherCondition)
            repo.deleteAlert(state.item)
            _dialog.value = AlertsDialog.None
        }
    }

    fun confirmDeleteAll() {
        viewModelScope.launch {
            alerts.value.forEach { alert ->
                scheduler.cancel(alert.id, alert.alarmType, alert.weatherCondition)
            }
            repo.deleteAllAlerts()
            _dialog.value = AlertsDialog.None
        }
    }
}
