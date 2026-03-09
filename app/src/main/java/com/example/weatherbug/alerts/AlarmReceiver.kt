package com.example.weatherbug.alerts

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherbug.data.models.AlertItem


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alertId   = intent.getIntExtra(EXTRA_ALERT_ID, -1)
        val alarmType = intent.getStringExtra(EXTRA_ALARM_TYPE)
            ?: AlertItem.ALARM_TYPE_NOTIFICATION

        val inputData = Data.Builder()
            .putInt(WeatherAlertWorker.KEY_ALERT_ID, alertId)
            .putString(WeatherAlertWorker.KEY_ALARM_TYPE, alarmType)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    companion object {
        const val EXTRA_ALERT_ID   = "extra_alert_id"
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"

        /** Builds the PendingIntent used when scheduling or cancelling an alarm. */
        fun buildPendingIntent(
            context:   Context,
            alertId:   Int,
            alarmType: String
        ): PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_ALERT_ID,   alertId)
                putExtra(EXTRA_ALARM_TYPE, alarmType)
            }
            return PendingIntent.getBroadcast(
                context,
                alertId,           // use alertId as request code — unique per alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
