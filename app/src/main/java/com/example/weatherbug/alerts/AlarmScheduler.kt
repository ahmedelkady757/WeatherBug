package com.example.weatherbug.alerts

import android.app.AlarmManager
import android.content.Context
import com.example.weatherbug.data.models.AlertItem


class AlarmScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alert: AlertItem) {
        val pendingIntent = AlarmReceiver.buildPendingIntent(
            context   = context,
            alertId   = alert.id,
            alarmType = alert.alarmType
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alert.startTime,
            pendingIntent
        )
    }

    fun cancel(alertId: Int, alarmType: String) {
        val pendingIntent = AlarmReceiver.buildPendingIntent(
            context   = context,
            alertId   = alertId,
            alarmType = alarmType
        )
        alarmManager.cancel(pendingIntent)
    }
}
