package com.example.weatherbug.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherbug.R
import com.example.weatherbug.data.datasource.local.IAppDataStore
import com.example.weatherbug.data.models.AlertItem
import com.example.weatherbug.data.repo.WeatherRepo
import com.example.weatherbug.util.Constants
import com.example.weatherbug.util.ResponseState
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt


class WeatherAlertWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repo:      WeatherRepo   by inject()
    private val dataStore: IAppDataStore by inject()

    override suspend fun doWork(): Result {
        val alertId   = inputData.getInt(KEY_ALERT_ID,   -1)
        val alarmType = inputData.getString(KEY_ALARM_TYPE) ?: AlertItem.ALARM_TYPE_NOTIFICATION

        val lat      = dataStore.savedLatFlow.first()
        val lon      = dataStore.savedLonFlow.first()
        val units    = dataStore.tempUnitFlow.first()
        val lang     = dataStore.effectiveLangFlow.first()
        val windUnit = dataStore.windUnitFlow.first()

        val weatherResult = repo.getCurrentWeather(lat, lon, units, lang)

        if (weatherResult is ResponseState.Success) {
            val w = weatherResult.data

            val tempSymbol = when (units) {
                Constants.UNIT_IMPERIAL -> "°F"
                Constants.UNIT_STANDARD -> "K"
                else                    -> "°C"
            }

            val temp      = w.main.temp.roundToInt()
            val feelsLike = w.main.feelsLike.roundToInt()
            val humidity  = w.main.humidity
            val desc      = w.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
            val cityName  = w.name

            // Convert wind speed to the user-preferred unit
            val windMs    = w.wind.speed  // API always returns m/s
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

            showNotification(alertId, title, body)
        }

        if (alertId != -1) {
            repo.deleteAlertById(alertId)
        }

        return Result.success()
    }

    private fun showNotification(alertId: Int, title: String, body: String) {
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(alertId.takeIf { it != -1 } ?: FALLBACK_NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID              = "weather_alerts_channel"
        const val KEY_ALERT_ID            = "alert_id"
        const val KEY_ALARM_TYPE          = "alarm_type"
        private const val FALLBACK_NOTIFICATION_ID = 9999
    }
}
