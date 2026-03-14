package com.example.weatherbug.core.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherbug.R
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.repo.WeatherRepo

import com.example.weatherbug.core.util.Constants
import com.example.weatherbug.core.util.ResponseState
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import kotlin.math.roundToInt

class WeatherAlertWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repo:      WeatherRepo   by inject()
    private val dataStore: IAppDataStore by inject()
    private val scheduler: AlarmScheduler by inject()

    override suspend fun doWork(): Result {
        val alertId          = inputData.getInt(KEY_ALERT_ID, -1)
        val alarmType        = inputData.getString(KEY_ALARM_TYPE)  ?: AlertItem.ALARM_TYPE_NOTIFICATION
        val condition        = inputData.getString(KEY_CONDITION)   ?: AlertItem.CONDITION_ANY



        val lat      = dataStore.savedLatFlow.first()
        val lon      = dataStore.savedLonFlow.first()
        val units    = dataStore.tempUnitFlow.first()
        val lang     = dataStore.effectiveLangFlow.first()
        val windUnit = dataStore.windUnitFlow.first()

        val weatherResult = repo.getCurrentWeather(lat, lon, units, lang)

        if (weatherResult is ResponseState.Success) {
            val w          = weatherResult.data
            val apiMain    = w.weather.firstOrNull()?.main ?: ""


            
            if (!AlertItem.matches(condition, apiMain)) {

                rescheduleForNextDay(alertId)
                return Result.success()
            }


            val tempSymbol = when (units) {
                Constants.UNIT_IMPERIAL -> "°F"
                Constants.UNIT_STANDARD -> "K"
                else                    -> "°C"
            }

            val temp      = w.main.temp.roundToInt()
            val feelsLike = w.main.feelsLike.roundToInt()
            val humidity  = w.main.humidity
            val desc      = w.weather.firstOrNull()
                              ?.description
                              ?.replaceFirstChar { it.uppercase() } ?: ""
            val cityName  = w.name

            val windMs = w.wind.speed
            val (windVal, windLabel) = when (windUnit) {
                Constants.WIND_UNIT_MPH -> Pair((windMs * 2.237).roundToInt(), "mph")
                Constants.WIND_UNIT_KMH -> Pair((windMs * 3.6).roundToInt(),   "km/h")
                else                    -> Pair(windMs.roundToInt(),            "m/s")
            }

            val title = context.getString(R.string.alert_notification_title, cityName)
            val body  = context.getString(
                R.string.alert_notification_body,
                temp, tempSymbol, feelsLike, tempSymbol,
                desc,
                humidity,
                windVal, windLabel
            )

            if (alarmType == AlertItem.ALARM_TYPE_ALARM) {
                AlarmSoundPlayer.play(context)
            }

            showNotification(
                alertId   = alertId,
                title     = title,
                body      = body,
                alarmType = alarmType
            )
        } else {
        }

        rescheduleForNextDay(alertId)

        return Result.success()
    }

    private suspend fun rescheduleForNextDay(alertId: Int) {
        if (alertId != -1) {
            val alerts = repo.getAllAlerts().first()
            val alert = alerts.find { it.id == alertId }
            if (alert != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = alert.startTime }
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val newStart = cal.timeInMillis
                
                val nextAlert = alert.copy(startTime = newStart, endTime = newStart)
                repo.insertAlert(nextAlert)
                scheduler.schedule(nextAlert)

            }
        }
    }


    private fun showNotification(
        alertId:   Int,
        title:     String,
        body:      String,
        alarmType: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.alert_channel_desc)
            }
            manager.createNotificationChannel(channel)
        }

        val notificationId = alertId.takeIf { it != -1 } ?: FALLBACK_NOTIFICATION_ID

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // ── clicking the notification opens the app ────────────────────────
        val contentIntent = Intent(context, com.example.weatherbug.MainActivity::class.java).apply {
            action = ACTION_FROM_NOTIFICATION
            flags  = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 20_000,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(contentPendingIntent)

        if (alarmType == AlertItem.ALARM_TYPE_ALARM) {
            val stopIntent = Intent(context, StopAlarmReceiver::class.java).apply {
                putExtra(StopAlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 10_000,   // unique request code offset
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.alert_stop_alarm),
                stopPendingIntent
            )
        }

        manager.notify(notificationId, builder.build())
    }

    companion object {
        const val CHANNEL_ID   = "weather_alerts_channel"
        const val KEY_ALERT_ID = "alert_id"
        const val KEY_ALARM_TYPE = "alarm_type"
        const val KEY_CONDITION  = "weather_condition"
        const val ACTION_FROM_NOTIFICATION = "com.example.weatherbug.ACTION_FROM_NOTIFICATION"
        private const val FALLBACK_NOTIFICATION_ID = 9999
    }
}
