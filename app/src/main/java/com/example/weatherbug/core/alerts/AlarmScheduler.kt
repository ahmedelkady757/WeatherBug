package com.example.weatherbug.core.alerts

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.core.util.AppLogger

class AlarmScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alert: AlertItem) {
        val pendingIntent = AlarmReceiver.buildPendingIntent(
            context          = context,
            alertId          = alert.id,
            alarmType        = alert.alarmType,
            weatherCondition = alert.weatherCondition
        )


        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alert.startTime,
                pendingIntent
            )
            AppLogger.d("AlarmScheduler: scheduled EXACT alarm id=${alert.id} at ${alert.startTime}", "WB_ALERTS")
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alert.startTime,
                pendingIntent
            )
            AppLogger.d("AlarmScheduler: scheduled INEXACT alarm id=${alert.id} at ${alert.startTime}", "WB_ALERTS")
        }
    }

    fun cancel(alertId: Int, alarmType: String, weatherCondition: String = AlertItem.CONDITION_ANY) {
        val pendingIntent = AlarmReceiver.buildPendingIntent(
            context          = context,
            alertId          = alertId,
            alarmType        = alarmType,
            weatherCondition = weatherCondition
        )
        alarmManager.cancel(pendingIntent)
        AppLogger.d("AlarmScheduler: cancelled alarm id=$alertId", "WB_ALERTS")
    }
}
